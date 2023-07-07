package com.devil.fission.machine.common.util;

/**
 * 雪花id单例.
 *
 * @author devil
 * @date Created in 2022/12/13 13:55
 */
public enum IdGeneratorEnum {
    
    /**
     * 实例
     */
    INSTANCE;
    
    private IdGenerator idGenerator;
    
    IdGeneratorEnum() {
        String workIdEnv = "ID_GEN_WORK_ID";
        String dataCenterIdEnv = "ID_GEN_DATA_CENTER_ID";
        long workId = System.getProperty(workIdEnv) != null ? Long.valueOf(System.getProperty(workIdEnv)) : 1L;
        long dataCenterId = System.getProperty(dataCenterIdEnv) != null ? Long.valueOf(System.getProperty(dataCenterIdEnv)) : 1L;
        idGenerator = new IdGenerator(workId, dataCenterId);
    }
    
    /**
     * @return the idGenerator
     */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }
    
}
