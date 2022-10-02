package com.github.acticfox.service.util;

import java.util.UUID;

/**
 * @author kfy
 * @date 2022/09/20
 */
public class RequestIdUtils {
    /**
     * requestId的最大长度
     */
    public static final int MAX_LENGTH = 32;

    /**
     * 生成requestId
     *
     * @return
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String formatRequestId(String requestId) {
        return "requestId:" + requestId;
    }

    public static String formatRequestIp(String requestIp) {
        return "requestIp:" + requestIp;
    }

}
