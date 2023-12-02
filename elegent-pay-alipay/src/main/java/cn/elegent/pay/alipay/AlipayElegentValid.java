package cn.elegent.pay.alipay;

import cn.elegent.pay.annotation.TradePlatform;
import cn.elegent.pay.constant.Platform;
import cn.elegent.pay.core.ElegentValid;
import cn.elegent.pay.dto.ValidResponse;
import cn.elegent.pay.exceptions.TradeException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@TradePlatform(Platform.ALI)
@Slf4j
public class AlipayElegentValid implements ElegentValid {


    @Autowired
    private AlipayConfig alipayConfig;


    @Override
    public ValidResponse validPay(HttpEntity<String> httpEntity, HttpServletRequest httpRequest) throws TradeException {
        ValidResponse validResponse=new ValidResponse();
        try {
            Map<String, String> params = getParams(httpRequest);
            //获取支付宝POST过来反馈信息，将异步通知中收到的待验证所有参数都存放到map中
            //String body = httpEntity.getBody();
            //调用SDK验证签名
            //公钥验签示例代码
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getPublicKey(), "utf-8", alipayConfig.getSignType());
            if (signVerified) {
                validResponse.setValid(true);
                validResponse.setOrderSn((String) params.get("out_trade_no"));
                return validResponse;
            } else {
                validResponse.setValid(false);
                validResponse.setOrderSn( (String) params.get("out_trade_no") );
                return validResponse;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new TradeException("验签异常");
        }
    }

    @Override
    public ValidResponse validRefund(HttpEntity<String> httpEntity, HttpServletRequest httpRequest) throws TradeException {
        ValidResponse validResponse=new ValidResponse();

        try {
            //获取支付宝POST过来反馈信息，将异步通知中收到的待验证所有参数都存放到map中
            Map<String, String> params = getParams(httpRequest);
            //调用SDK验证签名
            //公钥验签示例代码
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getPublicKey(), "utf-8", alipayConfig.getSignType());
            if (signVerified) {
                validResponse.setValid(true);
                validResponse.setOrderSn( (String) params.get("out_trade_no") );
                return validResponse;
            } else {
                validResponse.setValid(false);
                validResponse.setOrderSn( (String) params.get("out_trade_no") );
                return validResponse;
            }
        }catch (Exception e){
            e.printStackTrace();
            validResponse.setValid(false);
            return validResponse;
        }
    }

    private Map<String,String> getParams(HttpServletRequest httpServletRequest){
        Map<String,String> params = new HashMap< String , String >();
        Map requestParams = httpServletRequest.getParameterMap();

        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String [])requestParams.get(name);
            String valueStr = "";
            for(int i = 0;i < values.length;i ++ ){
                valueStr =  (i==values.length-1)?valueStr + values [i]:valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put (name,valueStr);
        }
        log.info("params：{}",params);
        return params;
    }

    @Override
    public String successResult() {
        return AlipayConstant.SUCCESS;
    }

    @Override
    public String failResult() {
        return AlipayConstant.FAIL;
    }
}
