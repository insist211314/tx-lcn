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
package com.codingapi.txlcn.logger.db;

import com.alibaba.fastjson.JSON;
import com.codingapi.txlcn.logger.exception.TxLoggerException;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Description: log-db数据库操作类
 * Company: CodingApi
 * Date: 2018/12/20
 *
 * @author codingapi
 */
@Slf4j
public class LogDbHelper implements DisposableBean {

    private HikariDataSource hikariDataSource;

    private QueryRunner queryRunner;

    public LogDbHelper(DataSource loggerSource) throws TxLoggerException {
//        log.info("log-db Properties: {}", JSON.toJSONString(logDbProperties));
//        if (logDbProperties.getDriverClassName() == null) {
//            throw new TxLoggerException("Init TxLogger error. see config [com.codingapi.txlcn.logger.db.LogDbProperties]");
//        }
//        hikariDataSource = new HikariDataSource(logDbProperties);
        queryRunner = new QueryRunner(loggerSource);
        log.info("log-db prepared.");
    }

    public int update(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = queryRunner.getDataSource().getConnection();
            conn.setAutoCommit(false);
            int i = queryRunner.update(conn, sql, params);
            conn.commit();
            return i;
        } catch (SQLException e) {
            log.error("update error! connection=" + conn.getClass().getName(), e);
            if (conn != null) {
                try{
                    conn.rollback();
                }catch (Exception ex){
                }
            }
            return 0;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DbUtils.close(conn);
                }
            }catch (Exception e){

            }
        }
    }


    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) {
        try {
            return queryRunner.query(sql, rsh, params);
        } catch (SQLException e) {
            log.error("query error", e);
            return null;
        }
    }

    public <T> T query(String sql, ScalarHandler<T> scalarHandler, Object... params) {
        try {
            return queryRunner.query(sql, scalarHandler, params);
        } catch (SQLException e) {
            log.error("query error", e);
            return null;
        }
    }

    @Override
    public void destroy() throws Exception {
        hikariDataSource.close();
        log.info("log hikariDataSource close.");
    }
}
