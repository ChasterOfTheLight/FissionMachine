package com.devil.fission.machine.example.service.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 回调实体.
 *
 * @author Devil
 * @date Created in 2024/8/20 上午10:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackPojo implements Serializable {
    
    /**
     * 项目编号.
     */
    @JsonProperty(value = "pro_no")
    private String proNo;
    
    /**
     * 项目名称.
     */
    @JsonProperty(value = "pro_name")
    private String projectName;
    
    /**
     * 所属集团.
     */
    @JsonProperty(value = "pro_group")
    private String projectGroup;
    
    /**
     * 项目部门.
     */
    @JsonProperty(value = "pro_dept")
    private String projectDept;
    
    /**
     * 所属行业.
     */
    @JsonProperty(value = "pro_industry")
    private String projectIndustry;
    
    /**
     * 企业（项目）简介.
     */
    @JsonProperty(value = "pro_desc")
    private String projectDesc;
    
    /**
     * 项目开发状态.
     */
    @JsonProperty(value = "pro_state")
    private String projectState;
    
    /**
     * 项目所属城市.
     */
    @JsonProperty(value = "pro_address")
    private String projectAddress;
    
    /**
     * 原项目编号.
     */
    @JsonProperty(value = "old_pro_no")
    private String oldProjectNo;
    
    /**
     * 项目运营状态.
     */
    @JsonProperty(value = "project_developing_status")
    private String projectDevelopingStatus;
    
    /**
     * 项目发布状态.
     */
    @JsonProperty(value = "project_publish_status")
    private String projectPublishStatus;
    
    /**
     * 项目阶段.
     */
    @JsonProperty(value = "project_phase")
    private String projectPhase;
    
    /**
     * 项目类型.
     */
    @JsonProperty(value = "project_type")
    private String projectType;
    
    /**
     * 项目来源.
     */
    @JsonProperty(value = "project_source")
    private String projectSource;
    
    /**
     * 项目赛道分类.
     */
    @JsonProperty(value = "track_classification")
    private String projectTrackClassification;
    
    /**
     * 外部推荐类型.
     */
    @JsonProperty(value = "external_recommendation_types")
    private String projectExternalRecommendationTypes;
    
    /**
     * 国际项目.
     */
    @JsonProperty("international_project")
    private String internationalProject;
    
    /**
     * 备案流程编号.
     */
    @JsonProperty("filing_process_number")
    private String filingProcessNumber;
    
    /**
     * 开发开始时间.
     */
    @JsonProperty("start_date_of_development")
    private Date startDateOfDevelopment;
    
    /**
     * 备案时间.
     */
    @JsonProperty("filing_date")
    private Date filingDate;
    
    /**
     * 推荐时间.
     */
    @JsonProperty("recommender_date")
    private Date recommenderDate;
    
    /**
     * 分配时间.
     */
    @JsonProperty("allocation_date")
    private Date allocationDate;
    
    /**
     * 首次上会时间.
     */
    @JsonProperty("first_meeting_time")
    private Date firstMeetingTime;
    
    /**
     * 终止时间.
     */
    @JsonProperty("termination_date")
    private Date terminationDate;
    
    /**
     * 计划模版类型.
     */
    @JsonProperty("plan_template_type")
    private String planTemplateType;
    
    /**
     * 联营类型.
     */
    @JsonProperty("joint_venture_type")
    private String jointVentureType;
    
    /**
     * 项目数据来源.
     */
    @JsonProperty("data_source")
    private int projectDataSource;
    
}
