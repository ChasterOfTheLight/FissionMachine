package com.devil.fission.machine.object.storage.util;

import com.devil.fission.machine.object.storage.core.StorableRequest;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FileMd5Util.
 *
 * @author devil
 * @date Created in 2023/8/22 17:23
 */
public class FileMd5Util {
    /**
     * @param data 文件输入流
     * @return MD5加密十六进制字符串
     * @throws IOException 流读取失败
     */
    public static String md5Stream(InputStream data) throws IOException {
        return DigestUtils.md5Hex(data);
    }

    /**
     * md加密
     *
     * @param inputStream 文件输入流
     * @param filename    上传的文件的名字
     * @return StorableRequest
     * @throws IOException
     */
    public static StorableRequest getStorableRequest(InputStream inputStream, String filename) throws IOException {
        // 创建一个输出流来缓存，防止多次读取导致数据读取错误
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        // 用以md5加密
        InputStream inSr4Md5 = new ByteArrayInputStream(baos.toByteArray());
        // 用以上传云服务流
        InputStream inSt4Oss = new ByteArrayInputStream(baos.toByteArray());
        // 获得后缀
        String fileSuffix = filename.substring(filename.lastIndexOf("."));
        // 获取文件名之前的相对路径 如果开启会导致不同文件夹上传重复文件 先不开启
        String fileUri = "";
        // 获取一个md加密
        String md5OfFileName = md5Stream(inSr4Md5);
        // 拼接成完整的文件名。
        String objectName = fileUri + md5OfFileName + fileSuffix;
        // 拼接url，这个就是地址
        return new StorableRequest(inSt4Oss, filename, objectName);
    }

}
