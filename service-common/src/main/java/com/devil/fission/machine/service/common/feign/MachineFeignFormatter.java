package com.devil.fission.machine.service.common.feign;

import com.devil.fission.machine.common.util.DateUtils;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

import java.util.Date;

/**
 * feign自定义格式化.
 *
 * @author devil
 * @date Created in 2022/12/7 16:34
 */
public class MachineFeignFormatter implements FeignFormatterRegistrar {
    
    @Override
    public void registerFormatters(FormatterRegistry formatterRegistry) {
        formatterRegistry.addConverter(Date.class, String.class, new FeignDate2StringConverter());
    }
    
    private static class FeignDate2StringConverter implements Converter<Date, String> {
        
        @Override
        public String convert(Date date) {
            return DateUtils.fastDateTimeFormat(date);
        }
    }
    
}
