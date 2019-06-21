/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.codingapi.txlcn.common.util.SpringUtils;
import com.codingapi.txlcn.logger.db.LogDbHelper;
import com.codingapi.txlcn.logger.db.LogDbProperties;
import com.codingapi.txlcn.logger.db.LoggerDataSource;
import com.codingapi.txlcn.logger.exception.TxLoggerException;
import com.codingapi.txlcn.logger.helper.MysqlLoggerHelper;
import com.codingapi.txlcn.logger.helper.TxLcnLogDbHelper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Description:
 * Company: CodingApi
 * Date: 2018/12/26
 *
 * @author codingapi
 */
@ComponentScan
@Configuration

public class TxLoggerConfiguration {




    @Data
    @Configuration
    @ConfigurationProperties(prefix = "tx-lcn.logger")
    @ConditionalOnProperty(name = "tx-lcn.logger.enabled", havingValue = "true")
    class LoggerEnabledTrueConfig {

        private boolean enabled = false;
        private boolean onlyError = true;
        private String driverClassName;
        private String jdbcUrl;
        private String username;
        private String password;


        @Bean
        public LogDbHelper logDbHelper(@Qualifier("loggerSource") DataSource loggerSource,@Qualifier("logDbProperties") LogDbProperties logDbProperties) throws TxLoggerException {
            logDbProperties.setEnabled(enabled);
            logDbProperties.setOnlyError(onlyError);
            return new LogDbHelper(loggerSource);
        }

        @Bean(
            destroyMethod = "close", initMethod = "init"
        )
        public DruidDataSource loggerSource() {
            LoggerDataSource datasource = new LoggerDataSource();
            datasource.setUrl(jdbcUrl);
            datasource.setUsername(username);
            datasource.setPassword(password);
            datasource.setDriverClassName(driverClassName);
            datasource.setInitialSize(5);
            datasource.setMaxActive(20);
            WallConfig wallConfig = new WallConfig();
            wallConfig.setMultiStatementAllow(true);
            return datasource;
        }


    }

    @Bean
    public LogDbProperties logDbProperties(){
        LogDbProperties properties = new LogDbProperties();
        return properties;
    }


    @Bean
    @ConditionalOnMissingBean
    public TxLcnLogDbHelper txLcnLoggerHelper() {
        return new MysqlLoggerHelper();
    }

    @Bean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }
}
