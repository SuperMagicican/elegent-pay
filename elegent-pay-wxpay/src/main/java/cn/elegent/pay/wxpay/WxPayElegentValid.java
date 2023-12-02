package cn.elegent.pay.wxpay;

import cn.elegent.pay.annotation.TradePlatform;
import cn.elegent.pay.constant.Platform;
import cn.elegent.pay.core.ElegentValid;
import cn.elegent.pay.dto.ValidResponse;
import cn.elegent.pay.exceptions.TradeException;
import com.alibaba.fastjson.JSON;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
@TradePlatform(Platform.WX)
public class WxPayElegentValid implements ElegentValid {


    @Autowired
    private WxpayConfig wxpayConfig;

    @Override
    public ValidResponse validPay(HttpEntity<String> httpEntity, HttpServletRequest httpServletRequest) throws TradeException {
        ValidResponse validResponse=new ValidResponse();

        try {
            //获取请求头
            HttpHeaders headers = httpEntity.getHeaders();
            //构建微信请求数据对象
            NotificationRequest request = new NotificationRequest.Builder()
                    .withSerialNumber(headers.getFirst("Wechatpay-Serial")) //证书序列号（微信平台）
                    .withNonce(headers.getFirst("Wechatpay-Nonce"))  //随机串
                    .withTimestamp(headers.getFirst("Wechatpay-Timestamp")) //时间戳
                    .withSignature(headers.getFirst("Wechatpay-Signature")) //签名字符串
                    .withBody(httpEntity.getBody())
                    .build();


            //微信通知的业务处理
            //验证签名，确保请求来自微信
            Map jsonData = null;
            try {
                //确保在管理器中存在自动更新的商户证书
                CertificatesManager certificatesManager = CertificatesManager.getInstance();
                Verifier verifier = certificatesManager.getVerifier(wxpayConfig.getMchId());

                //验签和解析请求数据
                NotificationHandler notificationHandler = new NotificationHandler(verifier, wxpayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
                Notification notification = notificationHandler.parse(request);

                if (!"TRANSACTION.SUCCESS".equals(notification.getEventType())) {
                    validResponse.setValid(false);
                    return validResponse;
                }
                //获取解密后的数据
                jsonData =  JSON.parseObject(notification.getDecryptData(),Map.class   );
                log.info("解密后的数据为："+jsonData);
            } catch (Exception e) {
                throw new TradeException("验签失败");
            }
            if (!"SUCCESS".equals(jsonData.get("trade_state"))) {
                validResponse.setValid(false);
                return validResponse;
            }
            validResponse.setValid(true);
            validResponse.setOrderSn(  (String) jsonData.get("out_trade_no")  ); //订单号
            return validResponse;
        } catch (Exception e) {
            validResponse.setValid(false);
            return validResponse;
        }
    }

    @Override
    public ValidResponse validRefund(HttpEntity<String> httpEntity, HttpServletRequest httpServletRequest) throws TradeException {

        ValidResponse validResponse=new ValidResponse();
        try {
            //获取请求头
            HttpHeaders headers = httpEntity.getHeaders();

            //构建微信请求数据对象
            NotificationRequest request = new NotificationRequest.Builder()
                    .withSerialNumber(headers.getFirst("Wechatpay-Serial")) //证书序列号（微信平台）
                    .withNonce(headers.getFirst("Wechatpay-Nonce"))  //随机串
                    .withTimestamp(headers.getFirst("Wechatpay-Timestamp")) //时间戳
                    .withSignature(headers.getFirst("Wechatpay-Signature")) //签名字符串
                    .withBody(httpEntity.getBody())
                    .build();

            //微信通知的业务处理
            Map jsonData = null;
            //验证签名，确保请求来自微信
            try {
                //确保在管理器中存在自动更新的商户证书
                CertificatesManager certificatesManager = CertificatesManager.getInstance();
                Verifier verifier = certificatesManager.getVerifier(wxpayConfig.getMchId());

                //验签和解析请求数据
                NotificationHandler notificationHandler = new NotificationHandler(verifier, wxpayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
                Notification notification = notificationHandler.parse(request);

                if (!"REFUND.SUCCESS".equals(notification.getEventType())) {
                    //非成功请求直接返回，理论上都是成功的请求
                    validResponse.setValid(false);
                    return validResponse;
                }
                //获取解密后的数据
                jsonData = JSON.parseObject( notification.getDecryptData(),Map.class  );


            } catch (Exception e) {
                throw new TradeException("验签失败");
            }
            if (!"SUCCESS".equals(jsonData.get("refund_status"))) {
                //非成功请求直接返回，理论上都是成功的请求
                validResponse.setValid(false);
                return validResponse;
            }

            //交易单号
            validResponse.setValid(true);
            validResponse.setOrderSn(  (String) jsonData.get("out_trade_no")  ); //订单号
            return validResponse;
        } catch (Exception e) {
            //非成功请求直接返回，理论上都是成功的请求
            validResponse.setValid(false);
            return validResponse;
        }
    }

    @Override
    public String successResult() {
        return  JSON.toJSONString( WxpayConstant.SUCCESS ) ;
    }

    @Override
    public String failResult() {
        return JSON.toJSONString(  WxpayConstant.FAIL);
    }
}
