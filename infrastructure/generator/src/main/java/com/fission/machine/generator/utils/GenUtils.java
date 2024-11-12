package com.fission.machine.generator.utils;

import com.fission.machine.generator.entity.ColumnEntity;
import com.fission.machine.generator.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类.
 *
 * @author devil
 * @date Created in 2022/4/27 10:15
 */
public class GenUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GenUtils.class);
    
    private static Configuration config;
    
    static {
        try {
            config = new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new GeneratorException("获取配置文件失败，", e);
        }
    }
    
    /**
     * 获取模版信息.
     */
    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<>(40);
        
        // api
        templates.add("template/api/client/ServiceClient.java.vm");
        templates.add("template/api/vo/QueryVo.java.vm");
        
        // service
        templates.add("template/service/entity/Entity.java.vm");
        templates.add("template/service/param/DeleteParam.java.vm");
        templates.add("template/service/param/InfoQueryParam.java.vm");
        templates.add("template/service/param/InsertParam.java.vm");
        templates.add("template/service/param/PageQueryParam.java.vm");
        templates.add("template/service/param/UpdateParam.java.vm");
        templates.add("template/service/mapper/Mapper.java.vm");
        templates.add("template/service/resource/Mapper.xml.vm");
        templates.add("template/service/service/Service.java.vm");
        templates.add("template/service/service/ServiceImpl.java.vm");
        templates.add("template/service/controller/WebConsumerController.java.vm");
        templates.add("template/service/controller/AppConsumerController.java.vm");
        
        return templates;
    }
    
    /**
     * 生成代码.
     */
    public static void generatorCode(Map<String, String> table, List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        boolean hasBigDecimal = false;
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        String tableComment = table.get("tableComment");
        if (tableComment.endsWith("表")) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }
        tableEntity.setComments(tableComment);
        //表名转换成Java类名
        String tableName = tableEntity.getTableName();
        String className = tableToJava(tableName, config.getString("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));
        
        //列信息
        List<ColumnEntity> columnEntities = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));
            
            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));
            
            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            
            columnEntities.add(columnEntity);
        }
        tableEntity.setColumns(columnEntities);
        
        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }
        
        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = config.getString("mainPath");
        mainPath = StringUtils.isBlank(mainPath) ? "com.fission" : mainPath;
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);
        
        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            
            try {
                //添加到zip
                LOGGER.info("开始渲染模板：" + template);
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), tableEntity.getClassname(), tableName)));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
                LOGGER.info("结束渲染模板");
            } catch (IOException e) {
                throw new GeneratorException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }
    
    /**
     * 列名转换成Java属性名.
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[] {'_'}).replace("_", "");
    }
    
    /**
     * 表名转换成Java类名.
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "");
        }
        return columnToJava(tableName);
    }
    
    /**
     * 获取文件名.
     */
    public static String getFileName(String template, String className, String classname, String tableName) {
        String generatorPath = "generator";
        
        // api
        if (template.contains("ServiceClient.java.vm")) {
            return generatorPath + File.separator + "api" + File.separator + "client" + File.separator + className + "ServiceClient.java";
        }
        if (template.contains("QueryVo.java.vm")) {
            return generatorPath + File.separator + "api" + File.separator + "vo" + File.separator + className + "QueryVo.java";
        }
        
        // service
        if (template.contains("Entity.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "entity" + File.separator + className + "Entity.java";
        }
        if (template.contains("DeleteParam.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "param" + File.separator + className + "DeleteParam.java";
        }
        if (template.contains("InfoQueryParam.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "param" + File.separator + className + "InfoQueryParam.java";
        }
        if (template.contains("InsertParam.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "param" + File.separator + className + "InsertParam.java";
        }
        if (template.contains("PageQueryParam.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "param" + File.separator + className + "PageQueryParam.java";
        }
        if (template.contains("UpdateParam.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "param" + File.separator + className + "UpdateParam.java";
        }
        if (template.contains("Mapper.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "mapper" + File.separator + className + "Mapper.java";
        }
        if (template.contains("Mapper.xml.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "resource" + File.separator + className + "Mapper.xml";
        }
        if (template.contains("Service.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "service" + File.separator + "I" + className + "Service.java";
        }
        if (template.contains("ServiceImpl.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "service" + File.separator + "impl" + File.separator + className
                    + "ServiceImpl.java";
        }
        if (template.contains("WebConsumerController.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "controller" + File.separator + className + "WebController.java";
        }
        if (template.contains("AppConsumerController.java.vm")) {
            return generatorPath + File.separator + "service" + File.separator + "controller" + File.separator + className + "AppController.java";
        }
        
        return null;
    }
}
