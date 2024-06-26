package com.devil.fission.machine.message.sms.aliyun;

import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.message.AbstractMessageService;
import com.devil.fission.machine.message.MessageChannelEnum;
import com.devil.fission.machine.message.MessageTypeEnum;
import com.devil.fission.machine.message.SendMessageDto;
import com.devil.fission.machine.message.SendMessageResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云短信服务.
 *
 * @author Devil
 * @date Created in 2024/6/26 上午10:53
 */
@Slf4j
public class AliyunSmsMessageService extends AbstractMessageService {
    
    private final Client client;
    
    public AliyunSmsMessageService(Client client) {
        this.client = client;
    }
    
    @Override
    protected SendMessageResult sendChannelMessage(SendMessageDto sendMessageDto) {
        if (StringUtils.isEmpty(sendMessageDto.getSignName())) {
            throw new ServiceException(ResponseCode.FAIL, "阿里云短信签名不能为空");
        }
        if (StringUtils.isEmpty(sendMessageDto.getTemplateId())) {
            throw new ServiceException(ResponseCode.FAIL, "阿里云短信模板id不能为空");
        }
        if (sendMessageDto.getTemplateParams() == null || sendMessageDto.getTemplateParams().isEmpty()) {
            throw new ServiceException(ResponseCode.FAIL, "阿里云短信模板参数不能为空");
        }
        // 实现阿里云短信模板发送逻辑
        SendSmsRequest sendSmsRequest = new SendSmsRequest().setSignName(sendMessageDto.getSignName()).setTemplateCode(sendMessageDto.getTemplateId())
                .setTemplateParam(JSONUtil.toJsonStr(sendMessageDto.getTemplateParams())).setPhoneNumbers(sendMessageDto.getSendTarget());
        RuntimeOptions runtime = new RuntimeOptions();
        SendSmsResponse response;
        try {
            response = client.sendSmsWithOptions(sendSmsRequest, runtime);
            String successCode = "OK";
            String aliyunResponseCode = response.getBody().getCode();
            if (aliyunResponseCode != null && !successCode.equals(aliyunResponseCode)) {
                return SendMessageResult.builder().sendSuccess(false).failReason(aliyunResponseCode).build();
            }
            return SendMessageResult.builder().sendSuccess(true).build();
        } catch (Exception e) {
            throw new ServiceException(ResponseCode.FAIL, "阿里云短信发送失败", e);
        }
    }
    
    @Override
    protected MessageTypeEnum getMessageType() {
        return MessageTypeEnum.SMS;
    }
    
    @Override
    protected MessageChannelEnum getMessageChannel() {
        return MessageChannelEnum.SMS_ALIYUN;
    }
}
