package com.devil.fission.machine.gateway.swagger;

import com.devil.fission.machine.common.util.StringUtils;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger资源提供.
 *
 * @author devil
 * @date Created in 2022/12/26 14:26
 */
@Component
@Primary
public class SwaggerProvider implements SwaggerResourcesProvider {
    
    public static final String API_URI = "/v2/api-docs";
    
    private final RouteLocator routeLocator;
    
    private final GatewayProperties gatewayProperties;
    
    public SwaggerProvider(RouteLocator routeLocator, GatewayProperties gatewayProperties) {
        this.routeLocator = routeLocator;
        this.gatewayProperties = gatewayProperties;
    }
    
    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        // 取出gateway的route
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        // 结合配置的route-路径(Path)，和route过滤，只获取在枚举中说明的route节点
        gatewayProperties.getRoutes().stream().filter(routeDefinition -> routes.contains(routeDefinition.getId()))
                .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
                        // 目前只处理Path断言  Header或其他路由需要另行扩展
                        .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName())).forEach(predicateDefinition -> {
                            String routeId = routeDefinition.getId();
                            String swaggerInfo = ServerRouteEnum.getSwaggerInfoByRoutId(routeId);
                            if (StringUtils.isNotEmpty(swaggerInfo)) {
                                resources.add(swaggerResource(swaggerInfo,
                                        predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI)));
                            }
                        }));
        return resources;
    }
    
    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("3.0");
        return swaggerResource;
    }
    
}