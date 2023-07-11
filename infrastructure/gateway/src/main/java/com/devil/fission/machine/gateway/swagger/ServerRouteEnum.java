package com.devil.fission.machine.gateway.swagger;

/**
 * 服务路由枚举.
 *
 * @author devil
 * @date Created in 2022/12/26 14:25
 */
public enum ServerRouteEnum {
    
    /**
     * 路由信息.
     */
    EXAMPLE_ROUTE("example", "示例服务接口");
    
    private String routeId;
    
    private String swaggerInfo;
    
    ServerRouteEnum(String routeId, String swaggerInfo) {
        this.routeId = routeId;
        this.swaggerInfo = swaggerInfo;
    }
    
    /**
     * 根据路由id获取swagger信息.
     *
     * @param routId 路由id
     * @return swagger信息
     */
    public static String getSwaggerInfoByRoutId(String routId) {
        for (ServerRouteEnum routeEnum : ServerRouteEnum.values()) {
            if (routId.equals(routeEnum.getRouteId())) {
                return routeEnum.getSwaggerInfo();
            }
        }
        return null;
    }
    
    /**
     * the routeId.
     */
    public String getRouteId() {
        return routeId;
    }
    
    /**
     * routeId : the routeId to set.
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    
    /**
     * the swaggerInfo.
     */
    public String getSwaggerInfo() {
        return swaggerInfo;
    }
    
    /**
     * swaggerInfo : the swaggerInfo to set.
     */
    public void setSwaggerInfo(String swaggerInfo) {
        this.swaggerInfo = swaggerInfo;
    }
}
