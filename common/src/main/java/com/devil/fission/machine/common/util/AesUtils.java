package com.devil.fission.machine.common.util;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

/**
 * AES工具类.
 *
 * @author Devil
 * @date Created in 2019-01-25 10:38
 */
public class AesUtils {

    /**
     * 初始向量（实现的是 AES-128，因此方法传入的 key 需为长度为 16 的字符串）.
     */
    private static final String IV_STRING = "AES--INIT-VECTOR";

    /**
     * 参数分别代表 算法名称/加密模式/数据填充方式.
     */
    public static final String DEFAULT_ALGORITHMSTR = "AES/CBC/PKCS5Padding";
    
    /**
     * 加密.
     *
     * @param contentByteArray 待加密内容字节数组
     * @param encryptKey 加密key
     * @param algorithm 加密算法
     * @param useBase64 是否使用base64
     */
    public static byte[] encrypt(byte[] contentByteArray, String encryptKey, String algorithm, boolean useBase64) {
        try {
            // 注意，为了能与 iOS 统一
            // 这里的 key 不可以使用 KeyGenerator、SecureRandom、SecretKey 生成
            if (StringUtils.isEmpty(algorithm)) {
                algorithm = DEFAULT_ALGORITHMSTR;
            }
            Cipher cipher = Cipher.getInstance(algorithm);
            if (isCbcAlgorithm(algorithm)) {
                byte[] initParam = IV_STRING.getBytes();
                IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"), ivParameterSpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
            }
            byte[] b = cipher.doFinal(contentByteArray);
            if (useBase64) {
                // 采用base64算法进行转码,避免出现中文乱码
                return Base64.getEncoder().encode(b);
            } else {
                return b;
            }
        } catch (Exception e) {
            throw new ServiceException(ResponseCode.FAIL, "AES加密失败", e);
        }
    }
    
    /**
     * 加密.
     *
     * @param content 待加密内容
     * @param encryptKey 加密key
     * @param algorithm 加密算法
     * @param useBase64 是否使用base64
     */
    public static byte[] encrypt(String content, String encryptKey, String algorithm, boolean useBase64) {
        return encrypt(content.getBytes(StandardCharsets.UTF_8), encryptKey, algorithm, useBase64);
    }
    
    public static String encrypt2String(String content, String encryptKey, String algorithm, boolean useBase64) {
        return new String(encrypt(content.getBytes(StandardCharsets.UTF_8), encryptKey, algorithm, useBase64));
    }
    
    /**
     * 解密.
     *
     * @param encryptedByteArray 待解密的字符数组
     * @param decryptKey 解密key 需要与加密key一致
     * @param algorithm 解密算法
     * @param useBase64 是否使用base64
     */
    public static byte[] decrypt2ByteArray(byte[] encryptedByteArray, String decryptKey, String algorithm, boolean useBase64) {
        try {
            if (StringUtils.isEmpty(algorithm)) {
                algorithm = DEFAULT_ALGORITHMSTR;
            }
            Cipher cipher = Cipher.getInstance(algorithm);
            if (isCbcAlgorithm(algorithm)) {
                byte[] initParam = IV_STRING.getBytes();
                IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"), ivParameterSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
            }
            byte[] decryptBytes;
            if (useBase64) {
                // 采用base64算法进行转码,避免出现中文乱码
                byte[] encryptBytes = Base64.getDecoder().decode(encryptedByteArray);
                decryptBytes = cipher.doFinal(encryptBytes);
            } else {
                decryptBytes = cipher.doFinal(encryptedByteArray);
            }
            return decryptBytes;
        } catch (Exception e) {
            throw new ServiceException(ResponseCode.FAIL, "AES解密失败", e);
        }
    }
    
    /**
     * 解密.
     *
     * @param encryptedByteArray 待解密的字符数组
     * @param decryptKey 解密key 需要与加密key一致
     * @param algorithm 解密算法
     * @param useBase64 是否使用base64
     */
    public static String decrypt(byte[] encryptedByteArray, String decryptKey, String algorithm, boolean useBase64) {
        return new String(decrypt2ByteArray(encryptedByteArray, decryptKey, algorithm, useBase64));
    }
    
    /**
     * 简单判断是否是CBC加密模式.
     */
    public static boolean isCbcAlgorithm(String algorithm) {
        return StringUtils.isNotBlank(algorithm) && algorithm.contains("CBC");
    }

    /**
     * 创建指定位数的随机字符串.
     *
     * @param length 表示生成字符串的长度
     * @return 字符串
     */
    public static String randomAesKey(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}

