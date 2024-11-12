package com.fission.machine.generator.utils;

/**
 * 自定义异常.
 *
 * @author devil
 * @date Created in 2022/4/27 10:16
 */
public class GeneratorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String msg;
    
    private int code = 500;
    
    public GeneratorException(String msg) {
        super(msg);
        this.msg = msg;
    }
    
    public GeneratorException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }
    
    public GeneratorException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    
    public GeneratorException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
}
