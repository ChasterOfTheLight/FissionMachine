package com.devil.fission.machine.example.service.es.entity;

import com.devil.fission.machine.example.service.es.setting.FissionSettingProvider;
import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.Settings;
import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.easyes.annotation.rely.RefreshPolicy;

/**
 * es document 实体.
 *
 * @author Devil
 * @date Created in 2024/5/10 17:47
 */
@Data
@Settings(shardsNum = 3, replicasNum = 2, settingsProvider = FissionSettingProvider.class)
@IndexName(value = "fission_document_202405111621", aliasName = "FissionDocument", refreshPolicy = RefreshPolicy.IMMEDIATE)
public class Document {
    
    /**
     * es中的唯一id.
     */
    @IndexId(type = IdType.CUSTOMIZE)
    private String id;
    
    /**
     * 文档标题.
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String title;
    
    /**
     * 文档内容.
     */
    private String content;
}

