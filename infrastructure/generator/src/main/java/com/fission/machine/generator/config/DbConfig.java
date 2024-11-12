package com.fission.machine.generator.config;

import com.fission.machine.generator.dao.GeneratorDao;
import com.fission.machine.generator.dao.MySqlGeneratorDao;
import com.fission.machine.generator.utils.GeneratorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 数据库配置.
 *
 * @author devil
 */
@Configuration
public class DbConfig {
    
    @Value("${mall.database: mysql}")
    private String database;
    
    @Autowired
    private MySqlGeneratorDao mySqlGeneratorDao;
    
    @Bean
    @Primary
    public GeneratorDao getGeneratorDao() {
        if ("mysql".equalsIgnoreCase(database)) {
            return mySqlGeneratorDao;
        } else {
            throw new GeneratorException("不支持当前数据库：" + database);
        }
    }
}
