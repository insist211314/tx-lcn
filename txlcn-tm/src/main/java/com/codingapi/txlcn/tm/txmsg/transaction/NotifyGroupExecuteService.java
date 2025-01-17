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
package com.codingapi.txlcn.tm.txmsg.transaction;

import com.alibaba.fastjson.JSONObject;
import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.common.exception.TxManagerException;
import com.codingapi.txlcn.logger.TxLogger;
import com.codingapi.txlcn.tm.core.DTXContext;
import com.codingapi.txlcn.tm.core.DTXContextRegistry;
import com.codingapi.txlcn.tm.core.TransactionManager;
import com.codingapi.txlcn.tm.core.storage.TransactionUnit;
import com.codingapi.txlcn.tm.txmsg.RpcExecuteService;
import com.codingapi.txlcn.tm.txmsg.TransactionCmd;
import com.codingapi.txlcn.txmsg.RpcClient;
import com.codingapi.txlcn.txmsg.netty.bean.SocketManager;
import com.codingapi.txlcn.txmsg.params.NotifyGroupParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Date: 2018/12/11
 *
 * @author ujued
 */
@Slf4j
@Service("rpc_notify-group")
public class NotifyGroupExecuteService implements RpcExecuteService {

    private static final TxLogger txLogger = TxLogger.newLogger(NotifyGroupExecuteService.class);

    private final TransactionManager transactionManager;

    private final DTXContextRegistry dtxContextRegistry;

    private final RpcClient rpcClient;

    @Autowired
    public NotifyGroupExecuteService(TransactionManager transactionManager, DTXContextRegistry dtxContextRegistry,RpcClient rpcClient) {
        this.transactionManager = transactionManager;
        this.dtxContextRegistry = dtxContextRegistry;
        this.rpcClient = rpcClient;
    }

    @Override
    public Serializable execute(TransactionCmd transactionCmd) throws TxManagerException {
        try {
            DTXContext dtxContext = dtxContextRegistry.get(transactionCmd.getGroupId());
            // 解析参数
            NotifyGroupParams notifyGroupParams = transactionCmd.getMsg().loadBean(NotifyGroupParams.class);
            int commitState = notifyGroupParams.getState();
            // 获取事务状态（当手动回滚时会先设置状态）
            int transactionState = transactionManager.transactionStateFromFastStorage(transactionCmd.getGroupId());
            if (transactionState == 0) {
                commitState = 0;
                log.warn("执行时间过长,超时！ groupId=" + transactionCmd.getGroupId());
            }

            // 系统日志
            txLogger.txTrace(
                    transactionCmd.getGroupId(), "", "notify group state: {}", notifyGroupParams.getState());

            if (commitState == 1) {
                List<TransactionUnit> transactionUnits = dtxContext.transactionUnits();
                List<String> modList = SocketManager.getInstance().getChannels().stream().map(c -> c.remoteAddress().toString()).map(key -> rpcClient.getModId(key)).collect(Collectors.toList());
                List<TransactionUnit> errUnitList = new ArrayList<>();
                for(TransactionUnit unit : transactionUnits){
                    if(!modList.contains(unit.getModId())){
                        errUnitList.add(unit);
                    }
                }
                if(errUnitList.size()==0){
                    transactionManager.commit(dtxContext);
                }else{
                    transactionManager.rollback(dtxContext);
                    commitState = 0;
                    log.warn("部分参与者服务异常！ groupId=" + transactionCmd.getGroupId() + "  errUnitList=" + JSONObject.toJSONString(errUnitList));
                }
            } else if (commitState == 0) {
                transactionManager.rollback(dtxContext);
            }
            if (transactionState == 0) {
                txLogger.txTrace(transactionCmd.getGroupId(), "", "mandatory rollback for user.");
            }
            return commitState;
        } catch (TransactionException e) {
            throw new TxManagerException(e);
        } finally {
            transactionManager.close(transactionCmd.getGroupId());
            // 系统日志
            txLogger.txTrace(transactionCmd.getGroupId(), "", "notify group successfully.");
        }
    }
}
