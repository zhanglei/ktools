package com.kalvin.ktools.comm.job;

import com.kalvin.ktools.comm.kit.Shakedown12306Kit;
import com.kalvin.ktools.dto.User12306TicketOrderDTO;
import com.kalvin.ktools.service.Ticket12306OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 定时任务
 *
 * 第1位，表示秒，取值0-59
 * 第2位，表示分，取值0-59
 * 第3位，表示小时，取值0-23
 * 第4位，日期天/日，取值1-31
 * 第5位，日期月份，取值1-12
 * 第6位，星期，取值1-7，星期一，星期二...，注：不是第1周，第二周的意思。另外：1表示星期天，2表示星期一。
 * 第7位，年份，可以留空，取值1970-2099
 *
 * (*)星号：可以理解为每的意思，每秒，每分，每天，每月，每年...
 * (?)问号：问号只能出现在日期和星期这两个位置，表示这个位置的值不确定，每天3点执行，所以第六位星期的位置，我们是不需要关注的，就是不确定的值。同时：日期和星期是两个相互排斥的元素，通过问号来表明不指定值。比如，1月10日，比如是星期1，如果在星期的位置是另指定星期二，就前后冲突矛盾了。
 * (-)减号：表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12
 * (,)逗号：表达一个列表值，如在星期字段中使用“1,2,4”，则表示星期一，星期二，星期四
 * (/)斜杠：如：x/y，x是开始值，y是步长，比如在第一位（秒） 0/15就是，从0秒开始，每15秒，最后就是0，15，30，45，60    另：*\/y，等同于0/y
 *
 * 例子：
 * 0 0 3 * * ?     每天3点执行
 * 0 5 3 * * ?     每天3点5分执行
 * 0 5 3 ? * *     每天3点5分执行，与上面作用相同
 * 0 5/10 3 * * ?  每天3点的 5分，15分，25分，35分，45分，55分这几个时间点执行
 * 0 10 3 ? * 1    每周星期天，3点10分 执行，注：1表示星期天
 * 0 10 3 ? * 1#3  每个月的第三个星期，星期天 执行，#号只能出现在星期的位置
 */
@Component
@EnableScheduling
public class Jobs {

    private final static Logger LOGGER = LoggerFactory.getLogger(Jobs.class);

    @Autowired
    private Ticket12306OrderService ticket12306OrderService;

    /**
     * 每天06:00启动所有抢票订单
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void startRobTickets() {
        LOGGER.info("每天06:00启动所有抢票订单...");
        ticket12306OrderService.updateTicketStatusStartAllVail();
    }

    /**
     * 检查抢票订单：每分钟检查一遍
     * 1、订单是否有效
     * 2、抢票状态是否正在进行
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void checkTicketOrder() {
        LOGGER.info("开始检查订单...");
        List<User12306TicketOrderDTO> ticketOrders = ticket12306OrderService.getAllValidAndStopTicketOrder();
        int size = ticketOrders.size();
        int maxPool = 10;   // 设置最大线程数10个
        if (size == 0) {
            return;
        }

        // 启动抢票状态已停止的订单
        ExecutorService executorService = Executors.newFixedThreadPool(size < maxPool ? size : maxPool);
        ticketOrders.forEach(o -> executorService.submit(() -> {
            LOGGER.info("线程{}正在启动抢票订单{}", Thread.currentThread().getName(), o.getId());

            // 更新抢票状态为抢票中
            ticket12306OrderService.updateTicketStatusStart(o.getId());

            Shakedown12306Kit.newInstance()
                .initUser(o.getUsername(), o.getPassword())
                .initQueryInfo(o.getTrainDate(), o.getFromStation(), o.getToStation(), o.getTrainNum(), o.getSeatType())
                .run();

            LOGGER.info("线程{}已成功启动抢票订单{}", Thread.currentThread().getName(), o.getId());
            try {
                // 隔一分钟启动
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

    }

    /**
     * 每天23:00停止所有抢票订单
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void stopTakingTickets() {
        LOGGER.info("每天23:00停止所有抢票订单...");
        ticket12306OrderService.updateTicketStatusStopAllVail();
    }

}
