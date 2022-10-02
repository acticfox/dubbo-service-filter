/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.github.acticfox.service.constants.RequestContextContants;
import com.github.acticfox.service.util.RequestIdUtils;

/**
 * 类RpcServiceFilter.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 29, 2018 2:18:40 PM
 */
@Activate(group = {CommonConstants.CONSUMER}, order = 1000001)
public class RpcConsumerFilter implements Filter, Filter.Listener {

    private static final Logger log = LoggerFactory.getLogger(RpcConsumerFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String requestId = RpcContext.getContext().getAttachment(RequestContextContants.REQUEST_ID);
        String requestIP = RpcContext.getContext().getAttachment(RequestContextContants.REQUEST_IP);
        if (StringUtils.isBlank(requestId)) {
            requestId = RequestIdUtils.generateRequestId();
            RpcContext.getContext().setAttachment(RequestContextContants.REQUEST_ID, requestId);
            invocation.setAttachment(RequestContextContants.REQUEST_ID, requestId);
        }
        MDC.put(RequestContextContants.REQUEST_ID, RequestIdUtils.formatRequestId(requestId));
        if (StringUtils.isBlank(requestIP)) {
            requestIP = RpcContext.getContext().getRemoteHost();
            RpcContext.getContext().setAttachment(RequestContextContants.REQUEST_IP, requestIP);
            invocation.setAttachment(RequestContextContants.REQUEST_IP, requestIP);
        }
        MDC.put(RequestContextContants.REQUEST_IP, RequestIdUtils.formatRequestIp(requestIP));

        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {

    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // TODO Auto-generated method stub

    }

}
