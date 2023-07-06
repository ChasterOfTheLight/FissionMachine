package com.devil.fission.common.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * 生成文件工具类.
 *
 * @author devil
 * @date Created in 2022/12/6 10:08
 */
public class FileUtils {
    
    /**
     * 创建临时文件.
     *
     * @param inputStream 输入文件流
     * @param name        文件名
     * @param ext         扩展名
     * @param tmpDirFile  临时文件夹目录
     */
    public static File createTmpFile(InputStream inputStream, String name, String ext, File tmpDirFile) throws IOException {
        File resultFile = File.createTempFile(name, '.' + ext, tmpDirFile);
        resultFile.deleteOnExit();
        copyToFile(inputStream, resultFile);
        return resultFile;
    }
    
    private static void copyToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream in = source;
                OutputStream out = org.apache.commons.io.FileUtils.openOutputStream(destination)) {
            IOUtils.copy(in, out);
        }
    }
    
    /**
     * 文件流生成base64.
     *
     * @param in 文件流
     * @return base64编码
     */
    public static String imageToBase64ByStream(InputStream in) throws IOException {
        byte[] data;
        // 读取图片字节数组
        try {
            data = new byte[in.available()];
            in.read(data);
            // 返回Base64编码过的字节数组字符串
            return Base64.getEncoder().encodeToString(data);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
}
