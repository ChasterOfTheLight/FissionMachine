package com.devil.fission.machine.message.sms.tencent;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.message.AbstractMessageService;
import com.devil.fission.machine.message.MessageChannelEnum;
import com.devil.fission.machine.message.MessageTypeEnum;
import com.devil.fission.machine.message.SendMessageDto;
import com.devil.fission.machine.message.SendMessageResult;
import com.devil.fission.machine.message.sms.tencent.config.TencentProperties;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云短信服务.
 *
 * @author Devil
 * @date Created in 2024/6/26 下午1:51
 */
@Slf4j
public class TencentSmsMessageService extends AbstractMessageService {
    
    private final TencentProperties tencentProperties;
    
    private final SmsClient client;
    
    public TencentSmsMessageService(TencentProperties tencentProperties, SmsClient client) {
        this.tencentProperties = tencentProperties;
        this.client = client;
    }
    
    @Override
    protected SendMessageResult sendChannelMessage(SendMessageDto sendMessageDto) {
        SendSmsRequest smsRequest = new SendSmsRequest();
        /* 填充请求参数,这里request对象的成员变量即对应接口的入参
         * 您可以通过官网接口文档或跳转到request对象的定义处查看请求参数的定义
         * 基本类型的设置:
         * 帮助链接：
         * 短信控制台: https://console.cloud.tencent.com/smsv2
         * 腾讯云短信小助手: https://cloud.tencent.com/document/product/382/3773#.E6.8A.80.E6.9C.AF.E4.BA.A4.E6.B5.81 */
        
        /* 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId，示例如1400006666 */
        // 应用 ID 可前往 [短信控制台](https://console.cloud.tencent.com/smsv2/app-manage) 查看
        String sdkAppId = tencentProperties.getSdkAppId();
        smsRequest.setSmsSdkAppId(sdkAppId);
        
        /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名 */
        // 签名信息可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-sign) 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-sign) 的签名管理查看
        String signName = sendMessageDto.getSignName();
        smsRequest.setSignName(signName);
        
        /* 模板 ID: 必须填写已审核通过的模板 ID */
        // 模板 ID 可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-template) 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-template) 的正文模板管理查看
        String templateId = sendMessageDto.getTemplateId();
        smsRequest.setTemplateId(templateId);
        
        /* 模板参数: 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致，若无模板参数，则设置为空 */
        if (sendMessageDto.getTemplateParams() != null && !sendMessageDto.getTemplateParams().isEmpty()) {
            String[] templateParamSet = sendMessageDto.getTemplateParams().values().toArray(new String[0]);
            smsRequest.setTemplateParamSet(templateParamSet);
        }
        
        /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
         * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
        String[] phoneNumberSet = new String[] {sendMessageDto.getSendTarget()};
        smsRequest.setPhoneNumberSet(phoneNumberSet);
        
        /* 用户的 session 内容（无需要可忽略）: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
        String sessionContext = sendMessageDto.getMsgExtra();
        smsRequest.setSessionContext(sessionContext);
        
        /* 短信码号扩展号（无需要可忽略）: 默认未开通，如需开通请联系 [腾讯云短信小助手] */
        String extendCode = "";
        smsRequest.setExtendCode(extendCode);
        
        /* 国内短信无需填写该项；国际/港澳台短信已申请独立 SenderId 需要填写该字段，默认使用公共 SenderId，无需填写该字段。注：月度使用量达到指定量级可申请独立 SenderId 使用，详情请联系 [腾讯云短信小助手](https://cloud.tencent.com/document/product/382/3773#.E6.8A.80.E6.9C.AF.E4.BA.A4.E6.B5.81)。*/
        String senderId = "";
        smsRequest.setSenderId(senderId);
        
        /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
         * 返回的 smsResponse 是一个 SendSmsResponse 类的实例，与请求对象对应 */
        try {
            SendSmsResponse smsResponse = client.SendSms(smsRequest);
            SendStatus[] sendStatusSet = smsResponse.getSendStatusSet();
            if (sendStatusSet != null && sendStatusSet.length > 0) {
                SendStatus sendStatus = sendStatusSet[0];
                String successCode = "Ok";
                if (sendStatus.getCode() != null && sendStatus.getCode().equals(successCode)) {
                    return SendMessageResult.builder().sendSuccess(true).build();
                }
            }
            String errorResponse = SendSmsResponse.toJsonString(smsResponse);
            log.error("短信发送失败：{}", errorResponse);
            return SendMessageResult.builder().sendSuccess(false).failReason(errorResponse).build();
        } catch (TencentCloudSDKException e) {
            throw new ServiceException(ResponseCode.FAIL, "腾讯云短信发送异常", e);
        }
        
    }
    
    @Override
    protected MessageTypeEnum getMessageType() {
        return MessageTypeEnum.SMS;
    }
    
    @Override
    protected MessageChannelEnum getMessageChannel() {
        return MessageChannelEnum.SMS_TENCENT;
    }
}
