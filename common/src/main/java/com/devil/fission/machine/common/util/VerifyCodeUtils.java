package com.devil.fission.machine.common.util;

import cn.hutool.core.util.RandomUtil;

/**
 * 验证码工具类.
 *
 * @author devil
 * @date Created in 2022/12/6 9:53
 */
public class VerifyCodeUtils {
    
    /**
     * 获取验证码.
     *
     * @param n 验证码字符数
     */
    public static String getCode(int n) {
        int maxCodeLen = 50;
        if (n <= 0 || n >= maxCodeLen) {
            return null;
        }
        return RandomUtil.randomNumbers(n);
    }
    
}
