/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.model;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import lombok.Data;

/**
 * 类RpcLog.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy 2018年11月16日 下午2:05:59
 */
@JSONType(orders = {"traceId", "requestId", "errorCode", "errorMsg", "serviceName", "methodName", "success", "usedTime",
    "exceptionMsg", "localAddress", "remoteAddress", "requestIp", "startTime", "param", "result"})
@Data
public class RpcLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前应用的IP */
    private String localAddress;
    /** 对方应用的IP */
    private String remoteAddress;
    /** 业务线名称 **/
    private String serviceName;
    /** 执行操作 **/
    private String methodName;
    /** 请求参数 **/
    private Object[] param;
    /** 执行是否成功 **/
    private boolean success;
    /** 执行返回结果JSON **/
    @JSONField(jsonDirect = true)
    private String result;
    /** 错误代码 **/
    private String errorCode;
    /** 错误消息 **/
    private String errorMsg;
    /** 异常信息 */
    private String exceptionMsg;
    /** 启动时间，毫秒 */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date startTime;
    /** 耗时，毫秒 **/
    private long usedTime;

    /** skywalking traceId **/
    private String traceId;
    /**
     * 前端请求ID
     */
    private String requestId;
    /**
     * 前端请求IP
     */
    private String requestIp;

}
