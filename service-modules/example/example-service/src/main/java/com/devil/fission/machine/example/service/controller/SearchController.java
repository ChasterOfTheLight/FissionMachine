package com.devil.fission.machine.example.service.controller;

import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.service.es.entity.Document;
import com.devil.fission.machine.example.service.es.mapper.DocumentMapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.core.conditions.index.LambdaEsIndexWrapper;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索接口.
 *
 * @author Devil
 * @date Created in 2024/5/11 9:04
 */
@Slf4j
@Api(value = "运营用户表相关接口", tags = {"WEB端-运营用户表相关接口"})
@RequestMapping("/search")
@RestController
public class SearchController {
    
    private final DocumentMapper documentMapper;
    
    public SearchController(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }
    
    @PostMapping(value = "/indexCreate", produces = {"application/json"})
    public Response<Boolean> indexCreate() {
        // 复杂场景使用
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 此处简单起见 索引名称须保持和实体类名称一致,字母小写 后面章节会教大家更如何灵活配置和使用索引
        wrapper.indexName("fission_document_202405111621");
        
        // 此处将文章标题映射为keyword类型(不支持分词),文档内容映射为text类型(支持分词查询)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD, 2.0f)
                .mapping(Document::getContent, FieldType.TEXT, Analyzer.IK_SMART, Analyzer.IK_MAX_WORD);
        
        // 设置分片及副本信息,可缺省
        wrapper.settings(3, 2);
        
        // 设置别名信息,可缺省
        String aliasName = "FissionDocument";
        wrapper.createAlias(aliasName);
        
        // 设置父子信息,若无父子文档关系则无需设置
//        wrapper.join("joinField", "document", "comment");
        
        // 创建索引
        boolean isOk = documentMapper.createIndex(wrapper);
        return Response.success(isOk);
    }
    
    @PostMapping(value = "/indexDataInsert", produces = {"application/json"})
    public Response<Boolean> indexDataInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setId("1788506275212845058");
        document.setTitle("老汉");
        document.setContent("人才");
        documentMapper.setCurrentActiveIndex("FissionDocument");
        Integer count = documentMapper.insert(document);
        log.info("文档数据插入: {}", count);
        return Response.success(true);
    }
    
    @PostMapping(value = "/indexSearch", produces = {"application/json"})
    public Response<Boolean> indexSearch() {
        // 测试根据条件查询
        String title = "老汉";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title);
        wrapper.limit(1);
        documentMapper.setCurrentActiveIndex("FissionDocument");
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        return Response.success(true);
    }
    
    @PostMapping(value = "/indexDataUpdate", produces = {"application/json"})
    public Response<Boolean> indexDataUpdate() {
        String id = "1788506275212845058";
        String title1 = "隔壁老王";
        Document document1 = new Document();
        document1.setId(id);
        document1.setTitle(title1);
        documentMapper.setCurrentActiveIndex("FissionDocument");
        Integer count = documentMapper.updateById(document1);
        log.info("文档数据更新: {}", count);
        return Response.success(true);
    }
    
    @PostMapping(value = "/indexDataDelete", produces = {"application/json"})
    public Response<Boolean> indexDataDelete() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "隔壁老王";
        wrapper.eq(Document::getTitle, title);
        documentMapper.setCurrentActiveIndex("FissionDocument");
        int successCount = documentMapper.delete(wrapper);
        log.info("文档数据删除: {}", successCount);
        return Response.success(true);
    }
    
}
