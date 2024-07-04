package com.devil.fission.machine.service.common.config;

import com.devil.fission.machine.common.response.ResponseCode;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * swagger配置.
 *
 * @author devil
 * @date Created in 2022/3/29 14:15
 */
@Configuration(proxyBeanMethods = false)
@EnableOpenApi
@ConfigurationProperties(prefix = "swagger")
public class ServiceSwaggerConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceSwaggerConfig.class);
    
    /**
     * 是否开启swagger，生产环境一般关闭，所以这里定义一个变量.
     */
    private boolean enable;
    
    /**
     * 项目应用名.
     */
    private String applicationName;
    
    /**
     * 项目版本信息.
     */
    private String applicationVersion;
    
    /**
     * 项目描述信息.
     */
    private String applicationDescription;
    
    @Bean
    public Docket docket() {
        LOGGER.info("Init Swagger Docket =========================== enable： {}", enable);
        
        // 处理全局响应码
        List<Response> responseList = new ArrayList<>();
        Arrays.stream(ResponseCode.values())
                .forEach(resultCode -> responseList.add(new ResponseBuilder().code(String.valueOf(resultCode.getCode())).description(resultCode.getMsg()).build()));
        
        return new Docket(DocumentationType.SWAGGER_2).pathMapping("/")
                // 定义是否开启swagger，false为关闭，可以通过变量控制，线上关闭
                .enable(enable)
                //配置api文档元信息
                .apiInfo(apiInfo())
                // 选择哪些接口作为swagger的doc发布
                .select()
                //apis() 控制哪些接口暴露给swagger，
                // RequestHandlerSelectors.any() 所有都暴露
                // RequestHandlerSelectors.basePackage("net.xdclass.*")  指定包位置
                // withMethodAnnotation(ApiOperation.class)标记有这个注解 ApiOperation
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)).paths(PathSelectors.any()).build().globalRequestParameters(getGlobalRequestParameters())
                .globalResponses(HttpMethod.GET, responseList).globalResponses(HttpMethod.POST, responseList).globalResponses(HttpMethod.PUT, responseList)
                .globalResponses(HttpMethod.DELETE, responseList);
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(applicationName).description(applicationDescription).contact(new Contact("接口文档", "www.devil.com", "devil@devil.com"))
                .version(applicationVersion).build();
    }
    
    /**
     * 生成全局通用参数.
     */
    private List<RequestParameter> getGlobalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder().name("Authorization").description("令牌token请求头").required(false).in(ParameterType.HEADER)
                .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).required(false).build());
        return parameters;
    }
    
    /**
     * to enable.
     */
    public Boolean getEnable() {
        return enable;
    }
    
    /**
     * enable : to enable to set.
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
    
    /**
     * the applicationName.
     */
    public String getApplicationName() {
        return applicationName;
    }
    
    /**
     * applicationName : the applicationName to set.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    /**
     * the applicationVersion.
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }
    
    /**
     * applicationVersion : the applicationVersion to set.
     */
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
    
    /**
     * the applicationDescription.
     */
    public String getApplicationDescription() {
        return applicationDescription;
    }
    
    /**
     * applicationDescription : the applicationDescription to set.
     */
    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }
}