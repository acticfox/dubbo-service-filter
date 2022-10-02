/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.filter;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.validation.Validation;
import org.apache.dubbo.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.fastjson.JSON;
import com.github.acticfox.common.api.annotation.RpcValidator;
import com.github.acticfox.common.api.annotation.rpclog.IgnoreRpcLog;
import com.github.acticfox.common.api.result.ResultDTO;
import com.github.acticfox.service.common.HandlerExceptionResolver;
import com.github.acticfox.service.common.RpcLogHelper;
import com.github.acticfox.service.constants.ExceptionEnumType;
import com.github.acticfox.service.constants.RequestContextContants;
import com.github.acticfox.service.util.RequestIdUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * 类RpcServiceFilter.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 29, 2018 2:18:40 PM
 */
@Activate(group = {CommonConstants.PROVIDER}, order = 1000000)
public class RpcProviderFilter implements Filter, Filter.Listener {

    private static final Logger log = LoggerFactory.getLogger(RpcProviderFilter.class);

    private Validation validation;

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

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

        long startMilliSecond = System.currentTimeMillis();
        invocation.setAttachment("start", String.valueOf(startMilliSecond));
        Method method = null;
        try {
            method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e1) {
            log.error("获取method失败", e1.getMessage());
        }
        Class<?> clz = method.getReturnType();
        if (clz != ResultDTO.class) {
            return invoker.invoke(invocation);
        }
        RpcValidator clzRpcValidator = invoker.getInterface().getAnnotation(RpcValidator.class);
        RpcValidator methodRpcValidator = method.getAnnotation(RpcValidator.class);
        boolean validationFlag = false;
        if (clzRpcValidator != null && clzRpcValidator.validation()) {
            validationFlag = true;
        }
        if (methodRpcValidator != null) {
            validationFlag = methodRpcValidator.validation() ? true : false;
        }

        if (validation != null && validationFlag) {
            try {
                Validator validator = validation.getValidator(invoker.getUrl());
                if (validator != null) {
                    validator.validate(invocation.getMethodName(), invocation.getParameterTypes(),
                        invocation.getArguments());
                }
            } catch (RpcException e) {
                throw e;
            } catch (ConstraintViolationException e) {
                Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
                if (constraintViolations.size() > 0) {
                    // 不用关心所有的校验失败情况，关心一个就够了
                    String errMsg = constraintViolations.iterator().next().getMessage();
                    log.error("参数校验未知错误,参数:{}", JSON.toJSONString(invocation.getArguments()), errMsg);
                    return AsyncRpcResult.newDefaultAsyncResult(
                        ResultDTO.buildFailedResult(ExceptionEnumType.PARAMETER_ILLEGALL.getErrCode(), errMsg),
                        invocation);
                }

            } catch (Throwable t) {
                return AsyncRpcResult.newDefaultAsyncResult(
                    ResultDTO.buildFailedResult(ExceptionEnumType.SYSTEM_ERROR.getErrCode(), t.getMessage()),
                    invocation);
            }
        }

        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        Method method = null;
        try {
            method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e1) {
            log.error("获取method失败", e1.getMessage());
        }
        Class<?> clz = method.getReturnType();
        if (clz != ResultDTO.class) {
            return;
        }

        long endMilliSecond = System.currentTimeMillis();
        invocation.setAttachment("end", String.valueOf(endMilliSecond));
        if (result.hasException()) { // 判断抛出的异常是否为业务异常
            ResultDTO<?> resultDTO = HandlerExceptionResolver.resolveException(invocation, (AppResponse)result);
            result.setException(null);
            result.setValue(resultDTO);
        }
        IgnoreRpcLog clzIgnoreRpcLog = invoker.getInterface().getAnnotation(IgnoreRpcLog.class);
        IgnoreRpcLog methodIgnoreRpcLog = method.getAnnotation(IgnoreRpcLog.class);
        boolean ignoreRpcLog = clzIgnoreRpcLog != null || methodIgnoreRpcLog != null ? true : false;
        // 写RpcLog日志
        if (!ignoreRpcLog) {
            RpcLogHelper.writeRpcLog(invocation, (AppResponse)result);
        }
        MDC.remove(RequestContextContants.REQUEST_ID);
        MDC.remove(RequestContextContants.REQUEST_IP);
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // TODO Auto-generated method stub

    }

}
