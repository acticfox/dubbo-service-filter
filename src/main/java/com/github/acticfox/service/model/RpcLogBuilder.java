/*
 * Copyright 2018 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.service.model;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.acticfox.common.api.result.ResultDTO;
import com.github.acticfox.service.constants.RequestContextContants;
import com.github.acticfox.service.util.IPUtil;
import com.github.acticfox.service.util.LogSecurityUtil;

/**
 * 类RpcLogBuilder.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 30, 2018 5:52:46 PM
 */
public class RpcLogBuilder {

    private static final Logger log = LoggerFactory.getLogger(RpcLogBuilder.class);

    public static RpcLog build(Invocation invocation, AppResponse result, Throwable exception) {
        Method method = null;
        try {
            method = invocation.getInvoker().getInterface().getMethod(invocation.getMethodName(),
                invocation.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e1) {
            log.error("获取method失败", e1.getMessage());
        }
        String requestId = invocation.getAttachment(RequestContextContants.REQUEST_ID);
        String requestIP = invocation.getAttachment(RequestContextContants.REQUEST_IP);
        RpcLog rpcLog = new RpcLog();
        rpcLog.setTraceId(TraceContext.traceId());
        rpcLog.setRequestId(requestId);
        rpcLog.setRequestIp(requestIP);
        rpcLog.setLocalAddress(IPUtil.getLocalIp());
        rpcLog.setServiceName(invocation.getInvoker().getInterface().getName());
        rpcLog.setMethodName(method.getName());

        Object[] argsSource = invocation.getArguments();
        Object[] argsTarget = null;
        try {
            argsTarget = (Object[])SerializationUtils.clone(argsSource);
        } catch (Exception e) {
            // ignore
            invocation.getAttachments().put("methodArgs", ArrayUtils.toString(argsSource));
            log.error("参数对象未序列化", e);
        }

        Object[] params = LogSecurityUtil.transformParameter(method, argsTarget);
        rpcLog.setParam(params);

        long startTime = Long.valueOf(invocation.getAttachment("start"));
        rpcLog.setStartTime(new Date(startTime));
        rpcLog.setUsedTime(Long.valueOf(invocation.getAttachment("end")) - startTime);

        Object obj = null;
        try {
            String jsonStr = null;
            obj = result.getValue();
            if (obj != null) {
                if (obj instanceof ResultDTO) {
                    ResultDTO simpleRes = (ResultDTO)obj;
                    rpcLog.setErrorCode(simpleRes.getCode());
                    rpcLog.setErrorMsg(simpleRes.getMessage());
                    jsonStr = LogSecurityUtil.formatJsonString(simpleRes);
                } else {
                    jsonStr = LogSecurityUtil.formatJsonString(obj);
                }
            }
            rpcLog.setResult(jsonStr);
        } catch (Exception e1) {
            log.error("解析DUBBO返回结果:{} 异常", JSON.toJSONString(obj), e1);
        }
        if (exception != null) {
            rpcLog.setSuccess(false);
            rpcLog.setExceptionMsg(exception.getMessage());
        } else {
            rpcLog.setSuccess(true);
        }

        return rpcLog;
    }

}
