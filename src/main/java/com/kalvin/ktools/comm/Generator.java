package com.kalvin.ktools.comm;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;


/**
 * 【作用】代码生成器<br>
 * 【说明】（无）
 * @author Kalvin
 * @Date 2018/1/15 10:11
 */
public class Generator {

    private static String packageName = "com.kalvin.ktools"; // 生成的包名
    private static String[] tableNames = {"kt_traffic_records", "kt_traffic_statistics"};   // 表名
    private static String tablePrefix = "kt_";    // 配置了会自动去掉表名的前缀
    private static boolean serviceNameStartWithI = false;  //user -> UserService, 设置成true: user -> IUserService
    private static String author = "Kalvin";    // 作者
    private static String outputDir = "D://genCode";   // 代码生成的路径目录
    private static String dbUrl = "jdbc:mysql://localhost:3306/ktools?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&tinyInt1isBit=false";
    private static String dbUsername = "root";
    private static String dbPassword = "root";

    public static void main(String[] args) {
        generateByTables(serviceNameStartWithI, packageName, tableNames);
    }

    private static void generateByTables(boolean serviceNameStartWithI, String packageName, String... tableNames) {
        GlobalConfig config = new GlobalConfig();
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        /*new MySqlTypeConvert() {
            *//**
             * 自定义数据库表字段类型转换
             * @param fieldType
             * @return
             *//*
                    *//*@Override
                    public DbColumnType processTypeConvert(String fieldType) {
//                        System.out.println("转换类型：" + fieldType);
                        return super.processTypeConvert(fieldType);
                    }*//*
        }*/
        dataSourceConfig.setDbType(DbType.MYSQL).setUrl(dbUrl).setUsername(dbUsername).setPassword(dbPassword)
                .setDriverName("com.mysql.cj.jdbc.Driver").setTypeConvert(new ITypeConvert() {
            @Override
            public IColumnType processTypeConvert(GlobalConfig globalConfig, String s) {
                if (s.contains("tinyint(1)")) {
                    return DbColumnType.INTEGER;
                }
                return new MySqlTypeConvert().processTypeConvert(globalConfig, s);
            }
        });
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig
                .setCapitalMode(true)
                .setEntityLombokModel(false)
//                .setDbColumnUnderline(true)
                .setTablePrefix(tablePrefix)
                .setNaming(NamingStrategy.underline_to_camel)
                .setInclude(tableNames)//修改替换成你需要的表名，多个表名传数组
//                .setSuperEntityClass("com.kkucar.kkyy.common.base.BasePo")
                .setRestControllerStyle(true);
        config.setActiveRecord(false)
                .setAuthor(author)
                .setOutputDir(outputDir)
                .setFileOverride(true)
                .setEnableCache(false)
                .setBaseResultMap(true)
                .setBaseColumnList(true);
        TemplateConfig tc = new TemplateConfig();
//        tc.setController("");   // 不生成controller层
        if (!serviceNameStartWithI) {
            config.setServiceName("%sService");
            config.setMapperName("%sDao");
        }
        new AutoGenerator().setGlobalConfig(config)
                .setTemplate(tc)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(
                        new PackageConfig()
                                .setParent(packageName)
                                .setEntity("entity")
                                .setMapper("dao")
                                .setController("controller")
                                .setService("service")
                                .setServiceImpl("service.impl")
                                .setXml("templates/mapper")
                ).execute();
    }
}