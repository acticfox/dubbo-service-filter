/*
 * Copyright 2018 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.service.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.github.acticfox.service.model.RpcLog;
import com.github.acticfox.service.model.RpcLogBuilder;
import com.github.acticfox.service.util.WaitingEnqueuePolicy;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 类RpcLog.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 29, 2018 4:17:17 PM
 */
public class RpcLogHelper {

    private static final Logger logger = LoggerFactory.getLogger("rpcTraceLogger");

    private static final int PARAM_LENGTH = 500 * 1000;

    private static final int RESULT_LENGTH = 1000 * 1000;

    private static ThreadFactory threadFactory =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("RpcLogEvent").build();

    private static ExecutorService executorService = new ThreadPoolExecutor(50, 50, 0L, TimeUnit.MILLISECONDS,
        new SynchronousQueue<Runnable>(true), threadFactory, new WaitingEnqueuePolicy());

    private static ValueFilter valueFilter = new ValueFilter() {
        @Override
        public Object process(Object object, String name, Object value) {
            if (name.equals("param") && value != null) {
                int paramLength = JSON.toJSONString(value).length();
                if (paramLength > PARAM_LENGTH) {
                    value = "Sorry, RpcLog param is too big to output! full size is: " + paramLength;
                }
            }
            if (name.equals("result") && value != null) {
                int resultLength = value.toString().length();
                if (resultLength > RESULT_LENGTH) {
                    value = "Sorry, RpcLog result is too big to output! full size is: " + resultLength;
                }
            }
            return value;
        }

    };

    public static void writeRpcLog(Invocation invocation, AppResponse rpcResult) {
        final long threadId = Thread.currentThread().getId();
        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RpcLog rpcLog = RpcLogBuilder.build(invocation, rpcResult, rpcResult.getException());
                        String rpcLogJson = JSON.toJSONString(rpcLog, valueFilter);
                        logger.info(rpcLogJson);
                    } finally {
                    }
                }
            });
        } catch (Exception e) {
            logger.error("async write rpclog error", e);
        }

    }

}
