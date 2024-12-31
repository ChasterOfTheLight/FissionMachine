package com.fission.machine.generator.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.fission.machine.generator.dao.GeneratorDao;
import com.fission.machine.generator.utils.GenUtils;
import com.fission.machine.generator.utils.PageUtils;
import com.fission.machine.generator.utils.Query;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器.
 *
 * @author devil
 */
@Service
public class SysGeneratorService {
    
    @Autowired
    private GeneratorDao generatorDao;
    
    /**
     * 分页查询表.
     *
     * @param query 分页参数
     * @return PageUtils
     */
    public PageUtils queryList(Query query) {
        Page<?> page = PageHelper.startPage(query.getPage(), query.getLimit());
        List<Map<String, Object>> list = generatorDao.queryList(query);
        
        return new PageUtils(list, (int) page.getTotal(), query.getLimit(), query.getPage());
    }
    
    /**
     * 查询表.
     *
     * @param tableName 表名
     * @return Map
     */
    public Map<String, String> queryTable(String tableName) {
        return generatorDao.queryTable(tableName);
    }
    
    /**
     * 查询列.
     *
     * @param tableName 表名
     * @return 列数组
     */
    public List<Map<String, String>> queryColumns(String tableName) {
        return generatorDao.queryColumns(tableName);
    }
    
    /**
     * 生成代码.
     *
     * @param tableNames 表名
     * @return byte[]
     */
    public byte[] generatorCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        
        for (String tableName : tableNames) {
            //查询表信息
            Map<String, String> table = queryTable(tableName);
            //查询列信息
            List<Map<String, String>> columns = queryColumns(tableName);
            //生成代码
            GenUtils.generatorCode(table, columns, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }
}
