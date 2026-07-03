package com.github.liyibo1110.saleproduct.base.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 用户上下文，使用TransmittableThreadLocal支持线程池场景下的上下文透传。
 * @author liyibo
 * @date 2026-07-02 11:00
 */
public class UserContext {

    private static final TransmittableThreadLocal<Long> USER_ID_TL = new TransmittableThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_TL.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_TL.get();
    }

    public static void clear() {
        USER_ID_TL.remove();
    }
}
