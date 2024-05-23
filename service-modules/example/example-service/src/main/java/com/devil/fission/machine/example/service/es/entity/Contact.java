package com.devil.fission.machine.example.service.es.entity;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.rely.FieldType;

/**
 * Contact.
 *
 * @author Devil
 * @date Created in 2024/5/10 17:54
 */
@Data
public class Contact {
    
    /**
     * 联系人id.
     */
    @IndexId
    private String contactId;
    
    /**
     * 地址.
     */
    @IndexField(fieldType = FieldType.TEXT)
    private String address;
}
