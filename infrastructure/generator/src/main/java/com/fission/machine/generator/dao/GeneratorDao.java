package com.fission.machine.generator.dao;

import java.util.List;
import java.util.Map;

/**
 * 数据库接口.
 *
 * @author devil
 * @since 2018-07-24
 */
public interface GeneratorDao {
    
    List<Map<String, Object>> queryList(Map<String, Object> map);
    
    Map<String, String> queryTable(String tableName);
    
    List<Map<String, String>> queryColumns(String tableName);
}
