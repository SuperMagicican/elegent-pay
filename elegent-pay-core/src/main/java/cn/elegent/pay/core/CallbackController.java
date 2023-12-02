package cn.elegent.pay.core;

import cn.elegent.pay.CallBackService;
import cn.elegent.pay.constant.PayConstant;
import cn.elegent.pay.dto.ValidResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@Slf4j
@RequestMapping(PayConstant.CALLBACK_PATH)
public class CallbackController {

    @Autowired
    private CallBackService callBackService;


    @RequestMapping( PayConstant.NOTIFY + "/{platform}")
    public String notify(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response, @PathVariable("platform") String platform){

        ElegentValid elegentValid = ElegentLoader.getElegentValid(platform);  //获取验证器
        try {
            ValidResponse validResponse = elegentValid.validPay(httpEntity, request);//验证支付通知
            String orderSn =validResponse.getOrderSn(); //订单号
            if(validResponse.isValid()){  //返回码成功
                callBackService.successPay(orderSn);
                //返回成功消费
                return elegentValid.successResult();
            }else{
                callBackService.failPay(orderSn);
                return elegentValid.failResult();
            }
        }catch (Exception e){
            log.error("支付回调处理失败",e);
            //微信返回的状态非正常
            return elegentValid.failResult();
        }
    }



    @RequestMapping( PayConstant.REFUND_NOTIFY + "/{platform}")
    public String refundNotify(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response,@PathVariable("platform") String platform) {

        ElegentValid elegentValid = ElegentLoader.getElegentValid(platform);  //获取验证器

        try {
            ValidResponse validResponse = elegentValid.validRefund(httpEntity, request);
            String orderSn = validResponse.getOrderSn();
            //订单号
            if (validResponse.isValid()) {  //返回码成功
                callBackService.successRefund(orderSn);
                //返回成功消费
                return elegentValid.successResult();
            } else {
                callBackService.failRefund(orderSn);
                return elegentValid.failResult();
            }
        } catch (Exception e) {
            log.error("退款回调处理失败", e);
            //微信返回的状态非正常
            return elegentValid.failResult();
        }

    }


}
