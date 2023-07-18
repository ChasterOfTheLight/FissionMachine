package com.devil.fission.machine.object.storage.core;

/**
 * 对象存储错误枚举.
 *
 * @author Devil
 * @date Created in 2023/3/22 16:25
 */
public enum StorageErrorCode {
    
    /**
     * 文件已存在.
     */
    FILE_EXIST(90001, "文件已存在"),
    
    /**
     * 文件不存在.
     */
    FILE_NOT_EXIST(90002, "文件不存在"),
    
    /**
     * 创建本地文件失败.
     */
    LOCAL_CREATE_FILE_FAIL(91001, "创建本地文件失败"),
    
    /**
     * 写入本地文件失败.
     */
    LOCAL_WRITE_FILE_FAIL(91002, "写入本地文件失败"),
    
    /**
     * 删除本地文件失败.
     */
    LOCAL_DELETE_FILE_FAIL(91003, "删除本地文件失败"),
    
    /**
     * 创建oss文件失败.
     */
    OSS_CREATE_FILE_FAIL(92001, "创建oss文件失败"),
    
    /**
     * 获取oss文件失败.
     */
    OSS_GET_FILE_FAIL(92002, "获取oss文件失败"),
    
    /**
     * 写入oss文件失败.
     */
    OSS_PUT_FILE_FAIL(92003, "写入oss文件失败"),
    
    /**
     * 删除oss文件失败.
     */
    OSS_DELETE_FILE_FAIL(92004, "删除oss文件失败"),
    
    /**
     * 设置oss权限失败.
     */
    OSS_SET_PERMISSION_FAIL(92005, "设置oss权限失败"),
    
    /**
     * 判断oss文件是否存在失败.
     */
    OSS_FILE_IS_EXIST_FAIL(92006, "判断oss文件是否存在失败"),
    
    /**
     * 生成oss临时秘钥失败.
     */
    OSS_TEMP_SECRET_FAIL(92007, "生成oss临时秘钥失败"),
    
    /**
     * 创建cos文件失败.
     */
    COS_CREATE_FILE_FAIL(93001, "创建cos文件失败"),
    
    /**
     * 获取cos文件失败.
     */
    COS_GET_FILE_FAIL(93002, "获取cos文件失败"),
    
    /**
     * 写入cos文件失败.
     */
    COS_PUT_FILE_FAIL(93003, "写入cos文件失败"),
    
    /**
     * 删除cos文件失败.
     */
    COS_DELETE_FILE_FAIL(93004, "删除cos文件失败"),
    
    /**
     * 设置cos权限失败.
     */
    COS_SET_PERMISSION_FAIL(93005, "设置cos权限失败"),
    
    /**
     * 判断cos文件是否存在失败.
     */
    COS_FILE_IS_EXIST_FAIL(93006, "判断cos文件是否存在失败"),
    
    /**
     * 生成cos临时秘钥失败.
     */
    COS_TEMP_SECRET_FAIL(93007, "生成cos临时秘钥失败");
    
    private int errCode;
    
    private String errMsg;
    
    StorageErrorCode(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    
    /**
     * get the errCode .
     */
    public int getErrCode() {
        return errCode;
    }
    
    /**
     * the errCode to set.
     */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
    
    /**
     * get the errMsg .
     */
    public String getErrMsg() {
        return errMsg;
    }
    
    /**
     * the errMsg to set.
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
