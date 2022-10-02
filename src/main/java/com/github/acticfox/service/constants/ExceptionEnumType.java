/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.constants;

import org.apache.commons.lang3.StringUtils;

import com.github.acticfox.common.api.enums.ExceptionCodeEnum;

/**
 * 类ExceptionEnumType.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy 2018年11月19日 下午8:44:02
 */
public enum ExceptionEnumType implements ExceptionCodeEnum {

    /**
     * 参数非法
     */
    PARAMETER_ILLEGALL("PARAMETER.ILLEGALL", "参数错误", false),

    /**
     * IO异常
     */
    NETWORK_IO_ERROR("NETWORK.IO.ERROR", "IO异常", true),

    // HSF超时
    HSF_TIMEOUT("HSF.TIMEOUT", "DUBBO调用超时异常", true),

    // HSF异常
    HSF_ERROR("HSF.ERROR", "DUBBO层错误异常", true),

    // 系统错误
    SYSTEM_ERROR("SYSTEM.ERROR", "系统异常", false),

    // 未知异常
    UNKONW_ERROR("UNKONW.ERROR", "未知异常", false);

    private String errCode;

    private String errDesc;

    private boolean allowRetry;

    ExceptionEnumType(String errCode, String errDesc, boolean allowRetry) {
        this.errCode = errCode;
        this.errDesc = errDesc;
        this.allowRetry = allowRetry;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    public void moreDesc(String desc) {
        if (StringUtils.isBlank(desc)) {
            return;
        }
        this.errDesc = desc;
    }

    @Override
    public String getErrMsg() {
        return errDesc;
    }

    public boolean isAllowRetry() {
        return allowRetry;
    }

    @Override
    public boolean isRetriable() {
        return allowRetry;
    }

}
