//package com.codingapi.txlcn.logger;
//
//import com.alibaba.druid.filter.Filter;
//import com.alibaba.druid.pool.DruidDataSource;
//import com.alibaba.druid.wall.WallConfig;
//import com.alibaba.druid.wall.WallFilter;
//import com.codingapi.txlcn.logger.db.LogDbProperties;
//import com.codingapi.txlcn.logger.db.LoggerDataSource;
//import lombok.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//@Data
//@Configuration
//@ConfigurationProperties(prefix = "tx-lcn.logger")
//public class LoggerDataSourceConfig {
//
//    private boolean enabled = false;
//
//    private boolean onlyError = true;
//
//    private String driverClassName;
//    private String jdbcUrl;
//    private String username;
//    private String password;
//
//
//    @Bean
//    public LogDbProperties logDbProperties(){
//        LogDbProperties properties = new LogDbProperties();
//        properties.setEnabled(enabled);
//        properties.setOnlyError(onlyError);
//        return properties;
//    }
//
//    @Bean(
//            destroyMethod = "close",
//            initMethod = "init"
//    )
//    public DruidDataSource loggerSource() {
//        LoggerDataSource datasource = new LoggerDataSource();
//
//        datasource.setUrl(jdbcUrl);
//        datasource.setUsername(username);
//        datasource.setPassword(password);
//        datasource.setDriverClassName(driverClassName);
//        datasource.setInitialSize(5);
//        datasource.setMaxActive(20);
//        WallConfig wallConfig = new WallConfig();
//        wallConfig.setMultiStatementAllow(true);
//        List<Filter> list = datasource.getProxyFilters();
//        if (list != null && !list.isEmpty()) {
//            Iterator iterator = list.iterator();
//
//            while(iterator.hasNext()) {
//                Filter filter = (Filter)iterator.next();
//                if (filter instanceof WallFilter) {
//                    ((WallFilter)filter).setConfig(wallConfig);
//                    break;
//                }
//            }
//        } else {
//            WallFilter wallFilter = new WallFilter();
//            wallFilter.setConfig(wallConfig);
//            List<Filter> filters1 = new ArrayList();
//            filters1.add(wallFilter);
//            datasource.setProxyFilters(filters1);
//        }
//
//        return datasource;
//    }
//}
