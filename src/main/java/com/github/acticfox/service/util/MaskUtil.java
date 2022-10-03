/*
 * Copyright 2014 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.service.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 类MaskUtil.java的实现描述：
 * 
 * <pre>
 * 身份证号、姓名、手机号、卡号等敏感信息进行掩码展示
 * </pre>
 * 
 * @author fanyong.kfy 2014年10月25日 下午9:44:58
 */
public class MaskUtil {

    /**
     * 手机号显示首3末4位，中间用*号隐藏代替，如：138****4213
     *
     * @param mobile
     * @return
     */
    public static String displayMobile(String mobile) {
        if (StringUtils.isBlank(mobile) || mobile.length() <= 8) {
            return mobile;
        }

        return wordMask(mobile, 3, 4, "*");
    }

    /**
     * 电话号码显示区号及末4位，中间用*号隐藏代替，如：010****4213
     *
     * @param telephone
     * @return
     */
    public static String displayTelephone(String telephone) {
        if (StringUtils.isBlank(telephone)) {
            return telephone;
        }
        String result;
        if (telephone.length() > 8) {
            if (telephone.contains("-")) {
                String[] temp = telephone.split("-");
                result = temp[0] + "****" + temp[1].substring(temp[1].length() - 4, temp[1].length());
            } else {
                result = telephone.substring(0, 3) + "****"
                    + telephone.substring(telephone.length() - 4, telephone.length());
            }
        } else {
            result = "****" + telephone.substring(telephone.length() - 4, telephone.length());
        }

        return result;
    }

    /**
     * 身份证号显示首6末4位，中间用4个*号隐藏代替，如：421002****1012
     *
     * @param idCard
     * @return
     */
    public static String displayIDCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 11) {
            return idCard;
        }

        return wordMask(idCard, 6, 4, "*");
    }

    /**
     * 银行卡显示首6末4位，中间用4个*号隐藏代替，如：622202****4123
     *
     * @param cardNo
     * @return
     */
    public static String displayBankCard(String cardNo) {
        if (StringUtils.isBlank(cardNo) || cardNo.length() < 11) {
            return cardNo;
        }

        return wordMask(cardNo, 6, 4, "*");
    }

    /**
     * 邮箱像是前两位及最后一位字符，及@后邮箱域名信息，如：ye****y@163.com
     *
     * @param email
     * @return
     */
    public static String displayEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }
        String[] temp = email.split("@");

        return wordMask(temp[0], 1, 1, "*") + "@" + temp[1];
    }

    /**
     * 三个字掩码，如：张晓明 如：张*明 两个字掩码，如：小明 如：*明 多个字掩码，如：张小明明 如：张**明
     *
     * @param name
     * @return
     */
    public static String displayName(String name) {
        if (StringUtils.isBlank(name) || name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return "*" + name.substring(1, 2);
        }

        return wordMask(name, 1, 1, "*");
    }

    /**
     * 对字符串进行脱敏处理
     *
     * @param word 被脱敏的字符
     * @param startLength 被保留的开始长度 前余n位
     * @param endLength 被保留的结束长度 后余n位
     * @param pad 填充字符
     */
    public static String wordMask(String word, int startLength, int endLength, String pad) {

        if (startLength + endLength > word.length()) {
            return org.apache.commons.lang3.StringUtils.leftPad("", word.length() - 1, pad);
        }

        String startStr = word.substring(0, startLength);

        String endStr = word.substring(word.length() - endLength, word.length());

        return startStr + StringUtils.leftPad("", word.length() - startLength - endLength, pad) + endStr;

    }

}
