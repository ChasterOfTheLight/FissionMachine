package com.devil.fission.common.util;

import com.devil.fission.common.exception.ServiceException;
import com.devil.fission.common.response.ResponseCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip工具.
 *
 * @author devil
 * @date Created in 2023/5/8 14:07
 */
public class GzipUtils {
	
	public static final String GZIP_ENCODE_UTF_8 = "UTF-8";
	
	/**
	 * 数据压缩.
	 */
	public static byte[] compress(byte[] bytes) {
		ByteArrayOutputStream out = null;
		GZIPOutputStream gos = null;
		try {
			out = new ByteArrayOutputStream();
			gos = new GZIPOutputStream(out);
			gos.write(bytes);
			gos.finish();
			gos.flush();
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.FAIL, "gzip数据压缩失败", e);
		} finally {
			try {
				if (gos != null) {
					gos.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				throw new ServiceException(ResponseCode.FAIL, "gzip数据压缩失败", e);
			}
		}
		return out.toByteArray();
	}
	
	/**
	 * 数据解压.
	 */
	public static byte[] decompress(byte[] bytes) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			GZIPInputStream gin = new GZIPInputStream(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int count;
			byte data[] = new byte[1024];
			while ((count = gin.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			out.flush();
			out.close();
			gin.close();
			return out.toByteArray();
		} catch (Exception e) {
			throw new ServiceException(ResponseCode.FAIL, "gzip数据解压失败", e);
		}
	}
	
}
