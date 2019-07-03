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
package com.codingapi.txlcn.tc.corelog.aspect;

import com.alibaba.fastjson.JSONObject;
import com.codingapi.txlcn.tc.corelog.H2DbHelper;
import com.codingapi.txlcn.tc.corelog.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Description: H2数据库操作
 * Company: CodingApi
 * Date: 2018/12/19
 *
 * @author codingapi
 */
@Slf4j
@Component
public class AspectLogHelper implements LogHelper {

    @Autowired(required = false)
    private H2DbHelper h2DbHelper;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public AspectLogHelper() {
    }

    @Override
    public void init() {
        if(h2DbHelper!=null) {
            h2DbHelper.update("CREATE TABLE IF NOT EXISTS TXLCN_LOG " +
                    "(" +
                    "ID VARCHAR(32) NOT NULL, " +
                    "UNIT_ID VARCHAR(32) NOT NULL," +
                    "GROUP_ID VARCHAR(64) NOT NULL," +
                    "METHOD_STR VARCHAR(512) NOT NULL ," +
                    "BYTES BLOB NOT NULL," +
                    "GROUP_ID_HASH BIGINT NOT NULL," +
                    "UNIT_ID_HASH BIGINT NOT NULL," +
                    "TIME VARCHAR(32) NOT NULL, " +
                    "PRIMARY KEY(ID) )");
        }
        log.info("AspectLogHelper.init 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
    }


    public boolean save(AspectLog txLog) {
        boolean result = true;
        if(h2DbHelper!=null) {
            String insertSql = "INSERT INTO TXLCN_LOG(ID,UNIT_ID,GROUP_ID,BYTES,METHOD_STR,GROUP_ID_HASH,UNIT_ID_HASH,TIME) VALUES(?,?,?,?,?,?,?,?)";
            result = h2DbHelper.update(insertSql, UUID.randomUUID().toString().replace("-", ""), txLog.getUnitId(), txLog.getGroupId(), txLog.getBytes(), txLog.getMethodStr(), txLog.getGroupId().hashCode(), txLog.getUnitId().hashCode(), format.format(new Date(txLog.getTime()))) > 0;
        }
        log.info("AspectLogHelper.save 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"" + "  AspectLog=" + JSONObject.toJSONString(txLog));
        return result;
    }

    public boolean deleteAll() {
        boolean result = true;
        if(h2DbHelper!=null) {
            String sql = "DELETE FROM TXLCN_LOG";
            result = h2DbHelper.update(sql) > 0;
        }
        log.info("AspectLogHelper.deleteAll 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    public void trancute() {
        if(h2DbHelper!=null) {
            String sql = "TRUNCATE TABLE TXLCN_LOG";
            h2DbHelper.update(sql);
        }
        log.info("AspectLogHelper.trancute 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
    }

    public boolean delete(long id) {
        boolean result = true;
        if(h2DbHelper!=null) {
            String sql = "DELETE FROM TXLCN_LOG WHERE ID = ?";
            result = h2DbHelper.update(sql, id) > 0;
        }
        log.info("AspectLogHelper.delete 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    public boolean delete(long groupIdHash, long unitIdHash) {
        return true;
//        boolean result = true;
//        if(h2DbHelper!=null) {
//            String sql = "DELETE FROM TXLCN_LOG WHERE GROUP_ID_HASH = ? and UNIT_ID_HASH = ?";
//            result = h2DbHelper.update(sql, groupIdHash, unitIdHash) > 0;
//        }
//        log.info("AspectLogHelper.delete 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
//        return result;
    }

    public boolean delete(String groupId) {
        return true;
//        boolean result = true;
//        if(h2DbHelper!=null) {
//            String sql = "DELETE FROM TXLCN_LOG WHERE GROUP_ID = ?";
//            result = h2DbHelper.update(sql, groupId) > 0;
//        }
//        log.info("AspectLogHelper.delete 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
//        return result;
    }

    public List<AspectLog> findAll() {
        List<AspectLog> result = null;
        if(h2DbHelper!=null) {
            String sql = "SELECT * FROM TXLCN_LOG";
            result = h2DbHelper.query(sql, resultSet -> {
                List<AspectLog> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(fill(resultSet));
                }
                return list;
            });
        }
        log.info("AspectLogHelper.findAll 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    public long count() {
        long result = 0;
        if(h2DbHelper!=null) {
            String sql = "SELECT count(*) FROM TXLCN_LOG";
            return h2DbHelper.query(sql, new ScalarHandler<Long>());
        }
        log.info("AspectLogHelper.count 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    public AspectLog getTxLog(String groupId, String unitId) {
        AspectLog result = null;
        if(h2DbHelper!=null) {
            String sql = "SELECT * FROM TXLCN_LOG WHERE GROUP_ID = ? and UNIT_ID = ?";
            result = h2DbHelper.query(sql, resultSetHandler, groupId, unitId);
        }
        log.info("AspectLogHelper.getTxLog 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    public AspectLog getTxLog(long id) {
        AspectLog result = null;
        if(h2DbHelper!=null) {
            String sql = "SELECT * FROM TXLCN_LOG WHERE ID = ?";
            result = h2DbHelper.query(sql, resultSetHandler, id);
        }
        log.info("AspectLogHelper.getTxLog 执行完毕! " + h2DbHelper==null?" h2DbHelper is null!":"");
        return result;
    }

    private final ResultSetHandler<AspectLog> resultSetHandler = resultSet -> {
        if (resultSet.next()) {
            return fill(resultSet);
        }
        return null;
    };


    private AspectLog fill(ResultSet resultSet) throws SQLException {
        AspectLog txLog = new AspectLog();
        txLog.setBytes(resultSet.getBytes("BYTES"));
        txLog.setGroupId(resultSet.getString("GROUP_ID"));
        txLog.setMethodStr(resultSet.getString("METHOD_STR"));
        txLog.setTime(resultSet.getLong("TIME"));
        txLog.setUnitId(resultSet.getString("UNIT_ID"));
        txLog.setGroupIdHash(resultSet.getLong("GROUP_ID_HASH"));
        txLog.setUnitIdHash(resultSet.getLong("UNIT_ID_HASH"));
        txLog.setId(resultSet.getLong("ID"));
        return txLog;
    }

}
