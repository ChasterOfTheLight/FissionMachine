SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `user_id`         bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_name`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
    `user_password`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '密码',
    `is_enabled`      tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用 1是0 否；默认1',
    `last_login_ip`   varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '最后登录IP',
    `last_login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
    `created_by` bigint                                                       NOT NULL COMMENT '创建人ID',
    `created_time`    datetime                                                     NOT NULL COMMENT '创建时间',
    `updated_by` bigint                                                       NOT NULL COMMENT '修改人ID',
    `updated_time`    datetime                                                     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`user_id`) USING BTREE,
    UNIQUE INDEX `uk_user_name`(`user_name`) USING BTREE COMMENT '用户账号唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '运营用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES (1, '18800000000', 'e10adc3949ba59abbe56e057f20f883e', 1, '', NULL, 1, '2022-12-12 10:29:43', 1, '2022-12-12 10:29:46');

SET
FOREIGN_KEY_CHECKS = 1;