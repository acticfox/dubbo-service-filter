/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.common;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.acticfox.common.api.exception.AbstractBizException;
import com.github.acticfox.common.api.exception.BusinessException;
import com.github.acticfox.common.api.exception.RetriableException;
import com.github.acticfox.common.api.result.ResultDTO;
import com.github.acticfox.service.constants.ExceptionEnumType;

/**
 * 类RpcHandlerExceptionResolver.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 29, 2018 3:14:46 PM
 */
public class HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(HandlerExceptionResolver.class);

    public static ResultDTO<?> resolveException(Invocation invocation, AppResponse rpcResult) {
        ResultDTO<?> resultDto = null;
        Object oriResp = rpcResult.getException();
        if (oriResp instanceof BusinessException) {
            BusinessException ex1 = (BusinessException)oriResp;
            log.warn("业务错误,错误码:{},错误描述:{}", ex1.getErrCode(), ex1.getMessage(), ex1);
            resultDto = buildErrorResult(invocation, ex1);
        } else if (oriResp instanceof RetriableException) {
            RetriableException ex1 = (RetriableException)oriResp;
            log.warn("业务可重试错误,错误码:{},错误描述:{}", ex1.getErrCode(), ex1.getMessage(), ex1);
            resultDto = buildErrorResult(invocation, ex1);
        } else if (oriResp instanceof IllegalArgumentException) {
            IllegalArgumentException ex1 = (IllegalArgumentException)oriResp;
            log.error("内部参数错误,错误描述:{}", ex1.getMessage(), ex1);
            resultDto = buildErrorResult(invocation, ExceptionEnumType.PARAMETER_ILLEGALL);
        } else if (oriResp instanceof TimeoutException) {
            log.error("网络超时错误:{}", oriResp);
            resultDto = buildErrorResult(invocation, ExceptionEnumType.NETWORK_IO_ERROR);
        } else if (oriResp instanceof Exception) {
            Exception ex1 = (Exception)oriResp;
            log.error("系统错误:{}", ex1.getMessage(), ex1);
            resultDto = buildErrorResult(invocation, ExceptionEnumType.SYSTEM_ERROR);
        }

        return resultDto;
    }

    public static ResultDTO<?> buildErrorResult(Invocation invocation, ExceptionEnumType defaultExceptionEnum) {

        return buildErrorResult(invocation, defaultExceptionEnum.getErrCode(), defaultExceptionEnum.getErrMsg(),
            defaultExceptionEnum.isRetriable());
    }

    private static ResultDTO<?> buildErrorResult(Invocation invocation, AbstractBizException exception) {
        String errCode = exception.getErrCode();
        String errMsg = exception.getMessage();
        Object[] errMsgArgs = exception.getErrorArgs();
        boolean retriable = exception instanceof RetriableException ? true : false;
        if (errMsgArgs != null && errMsgArgs.length > 0) {
            errMsg = String.format(errMsg, errMsgArgs);
        }

        return buildErrorResult(invocation, errCode, errMsg, retriable);
    }

    public static ResultDTO<?> buildErrorResult(Invocation invocation, String errCode, String errMsg,
        boolean retriable) {
        Method method = null;
        try {
            method = invocation.getInvoker().getInterface().getMethod(invocation.getMethodName(),
                invocation.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e1) {
            log.error("获取method失败", e1.getMessage());
        }
        Class<?> clz = method.getReturnType();
        ResultDTO<?> resultDto = null;
        if (clz == ResultDTO.class) {
            resultDto = ResultDTO.buildFailedResult(errCode, errMsg);
            resultDto.setRetriable(retriable);
        } else {
            // 返回结果对象非ResultDTO，抛出异常
            throw new RuntimeException(errCode + " : " + errMsg);
        }
        resultDto.setRetriable(retriable);

        return resultDto;
    }

}
