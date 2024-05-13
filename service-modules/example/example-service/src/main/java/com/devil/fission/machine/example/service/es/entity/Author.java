package com.devil.fission.machine.example.service.es.entity;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.rely.FieldType;

/**
 * Author.
 *
 * @author Devil
 * @date Created in 2024/5/10 17:52
 */
@Data
public class Author {
    /**
     * 作者id
     */
    @IndexId
    private String authorId;
    /**
     * 作者姓名
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String authorName;
}
