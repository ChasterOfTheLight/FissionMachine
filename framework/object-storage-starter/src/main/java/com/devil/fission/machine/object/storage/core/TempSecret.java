package com.devil.fission.machine.object.storage.core;

/**
 * 生成临时秘钥.
 *
 * @author Devil
 * @date Created in 2023/3/22 11:04
 */
public interface TempSecret<T> {
    
    /**
     * 生成上传临时秘钥.
     *
     * @param path 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子：a.jpg 或者 a/* 或者 *。
     *             如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围
     */
    T generateUploadTempSecret(String path);
    
}
