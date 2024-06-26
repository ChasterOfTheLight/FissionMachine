package com.devil.fission.machine.message.sms.emay;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.AesUtils;
import com.devil.fission.machine.common.util.GzipUtils;
import com.devil.fission.machine.common.util.HttpClientUtils;
import com.devil.fission.machine.message.AbstractMessageService;
import com.devil.fission.machine.message.MessageChannelEnum;
import com.devil.fission.machine.message.MessageTypeEnum;
import com.devil.fission.machine.message.SendMessageDto;
import com.devil.fission.machine.message.SendMessageResult;
import com.devil.fission.machine.message.sms.emay.config.EmaySmsProperties;
import com.devil.fission.machine.message.sms.emay.request.SmsSingleRequest;
import com.devil.fission.machine.message.sms.emay.response.SmsResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 亿美短信消息服务.
 *
 * @author Devil
 * @date Created in 2024/6/25 上午11:19
 */
@Slf4j
public class EmaySmsMessageService extends AbstractMessageService {
    
    private final EmaySmsProperties emaySmsProperties;
    
    public EmaySmsMessageService(EmaySmsProperties emaySmsProperties) {
        this.emaySmsProperties = emaySmsProperties;
    }
    
    @Override
    protected SendMessageResult sendChannelMessage(SendMessageDto sendMessageDto) {
        // 发送短信
        CloseableHttpClient httpClient = HttpClientUtils.getConnection();
        HttpPost httpPost = new HttpPost();
        try {
            String host = emaySmsProperties.getHost();
            // 单条发送
            String url = host + "/inter/sendSingleSMS";
            URIBuilder builder = new URIBuilder(url);
            httpPost.setURI(builder.build());
            
            String appId = emaySmsProperties.getAppId();
            // 设置请求头
            httpPost.setHeader("appId", appId);
            String encoding = "UTF-8";
            httpPost.setHeader("encode", encoding);
            httpPost.setHeader("gzip", "on");
            
            String mobile = sendMessageDto.getSendTarget();
            String msgId = sendMessageDto.getMsgId();
            String content = sendMessageDto.getMsgContent();
            String extendCode = sendMessageDto.getMsgExtra();
            // 设置请求体
            SmsSingleRequest smsSingleRequest = new SmsSingleRequest();
            smsSingleRequest.setContent(content);
            smsSingleRequest.setCustomSmsId(msgId);
            // 设置透传字段
            smsSingleRequest.setExtendedCode(extendCode);
            smsSingleRequest.setMobile(mobile);
            Gson gson = new Gson();
            String requestJson = gson.toJson(smsSingleRequest);
            // gzip压缩
            byte[] requestByteArray = GzipUtils.compress(requestJson.getBytes(encoding));
            // aes加密
            String algorithm = "AES/ECB/PKCS5Padding";
            String secretKey = emaySmsProperties.getSecretKey();
            requestByteArray = AesUtils.encrypt(requestByteArray, secretKey, algorithm, false);
            // 塞入请求body
            httpPost.setEntity(new ByteArrayEntity(requestByteArray));
            // 调用http线程池发送请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 打印响应结果
            if (statusCode == HttpStatus.SC_OK) {
                String emayResponseCode = httpResponse.getFirstHeader("result").getValue();
                String success = "SUCCESS";
                if (success.equals(emayResponseCode)) {
                    byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
                    // 解密
                    responseByteArray = AesUtils.decrypt2ByteArray(responseByteArray, secretKey, algorithm, false);
                    // 解压缩
                    responseByteArray = GzipUtils.uncompress(responseByteArray);
                    // 转换json
                    String responseJson = new String(responseByteArray, encoding);
                    SmsResponse smsResponse = gson.fromJson(responseJson, SmsResponse.class);
                    log.info("发送亿美单条短信成功，亿美响应: {} 发送手机号:{} 发送请求: {}", smsResponse.toString(), mobile, requestJson);
                    return SendMessageResult.builder().sendSuccess(true).build();
                } else {
                    log.error("发送亿美单条短信失败，亿美响应: {} 发送手机号:{}", emayResponseCode, mobile);
                    return SendMessageResult.builder().sendSuccess(false).failReason(emayResponseCode).build();
                }
            } else {
                log.error("发送亿美单条短信失败，请求码:{} 发送手机号:{} 发送请求: {}", statusCode, mobile, requestJson);
                return SendMessageResult.builder().sendSuccess(false).failReason("异常码：" + statusCode).build();
            }
        } catch (Exception e) {
            throw new ServiceException(ResponseCode.FAIL, "亿美短信发送异常", e);
        } finally {
            httpPost.releaseConnection();
        }
    }
    
    @Override
    protected MessageTypeEnum getMessageType() {
        return MessageTypeEnum.SMS;
    }
    
    @Override
    protected MessageChannelEnum getMessageChannel() {
        return MessageChannelEnum.SMS_EMAY;
    }
}
