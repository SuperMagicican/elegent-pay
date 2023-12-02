package cn.elegent.pay.alipay;

import cn.elegent.pay.CallBackService;
import cn.elegent.pay.annotation.TradePlatform;
import cn.elegent.pay.config.CallbackConfig;
import cn.elegent.pay.constant.PayConstant;
import cn.elegent.pay.constant.Platform;
import cn.elegent.pay.constant.TradeType;
import cn.elegent.pay.core.ElegentTrade;
import cn.elegent.pay.dto.*;
import cn.elegent.pay.exceptions.TradeException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@TradePlatform(Platform.ALI)
@Slf4j
public class AlipayElegentTrade implements ElegentTrade {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private CallbackConfig callbackConfig;

    @Autowired
    private CallBackService callBackService;


    private String getPayNotifyUrl(){
        return callbackConfig.getDomain()+ PayConstant.CALLBACK_PATH+ PayConstant.NOTIFY +"/"+ Platform.ALI;
    }

    private String getRefundNotifyUrl(){
        return callbackConfig.getDomain()+ PayConstant.CALLBACK_PATH+ PayConstant.REFUND_NOTIFY +"/"+ Platform.ALI;
    }


    @Override
    public PayResponse requestPay(PayRequest payRequest, String tradeType) throws TradeException {
        if(TradeType.NATIVE.equals( tradeType )){
            return createNativeOrder(payRequest);
        }
        if(TradeType.JSAPI.equals( tradeType )){
            return createJsApiOrder(payRequest);
        }
        if(TradeType.H5.equals( tradeType )){
            return createH5Order(payRequest);
        }
        if(TradeType.APP.equals( tradeType )){
            return createAPPOrder(payRequest);
        }
        return createNativeOrder(payRequest);
    }


    private PayResponse createNativeOrder(PayRequest payRequest) throws TradeException {
        try {
            AlipayClient alipayClient = getAliHttpClient();
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

            request.setNotifyUrl(getPayNotifyUrl());

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payRequest.getOrderSn());
            //转换
            //String totalFee= BigDecimal.valueOf(payRequest.getTotalFee()).divide(new BigDecimal(100)  ).toString();
            bizContent.put("total_amount", fenToYuan(payRequest.getTotalFee()));
            bizContent.put("subject", payRequest.getBody());
            request.setBizContent(bizContent.toString());
            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                PayResponse payResponse =new PayResponse();
                payResponse.setSuccess(true);
                payResponse.setCode_url(response.getQrCode()); //本地支付二维码
                payResponse.setOrder_sn(payRequest.getOrderSn());
                return payResponse;
            } else {
                log.error("调用失败");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new TradeException("订单创建失败,订单号："+ payRequest.getOrderSn());
        }
    }


    private PayResponse createJsApiOrder(PayRequest payRequest) throws TradeException {
        AlipayClient alipayClient = getAliHttpClient();
        try {
            AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
            request.setNotifyUrl(getPayNotifyUrl());
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payRequest.getOrderSn());
            bizContent.put("total_amount", fenToYuan( payRequest.getTotalFee() ));
            bizContent.put("subject", payRequest.getBody());
            bizContent.put("buyer_id", payRequest.getOpenid());
            bizContent.put("timeout_express", "10m");
            request.setBizContent(bizContent.toString());
            AlipayTradeCreateResponse  response = alipayClient.sdkExecute(request);
            if (response.isSuccess()) {
                PayResponse payResponse =new PayResponse();
                payResponse.setSuccess(true);
                payResponse.setPrepay_id(response.getTradeNo());
                payResponse.setOrder_sn(response.getOutTradeNo());
                return payResponse;
            } else {
                log.error("调用失败");
                return null;
            }
        }catch (Exception e){
            throw new TradeException("订单创建失败,订单号："+ payRequest.getOrderSn());
        }
    }

    private PayResponse createH5Order(PayRequest payRequest) throws TradeException {
        AlipayClient alipayClient = getAliHttpClient();
        try {
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setNotifyUrl(getPayNotifyUrl());
            request.setReturnUrl("");
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payRequest.getOrderSn());
            bizContent.put("total_amount", fenToYuan( payRequest.getTotalFee() ));
            bizContent.put("subject", payRequest.getBody());
            bizContent.put("product_code", "QUICK_WAP_WAY");

            request.setBizContent(bizContent.toString());
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                PayResponse payResponse =new PayResponse();
                payResponse.setSuccess(true);
                payResponse.setPrepay_id(response.getTradeNo());
                payResponse.setOrder_sn(response.getOutTradeNo());
                return payResponse;
            } else {
                log.error("调用失败");
                return null;
            }
        }catch (Exception e){
            throw new TradeException("订单创建失败,订单号："+ payRequest.getOrderSn());
        }
    }


    private PayResponse createAPPOrder(PayRequest payRequest) throws TradeException {
        AlipayClient alipayClient = getAliHttpClient();
        try {
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            request.setNotifyUrl(getPayNotifyUrl());
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", payRequest.getOrderSn());
            bizContent.put("total_amount", fenToYuan( payRequest.getTotalFee() ));
            bizContent.put("subject", payRequest.getBody());
            bizContent.put("product_code", "QUICK_MSECURITY_PAY");

            request.setBizContent(bizContent.toString());
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            if (response.isSuccess()) {
                PayResponse payResponse =new PayResponse();
                payResponse.setSuccess(true);
                payResponse.setPrepay_id(response.getTradeNo());
                payResponse.setOrder_sn(response.getOutTradeNo());
                return payResponse;
            } else {
                log.error("调用失败");
                return null;
            }
        }catch (Exception e){
            throw new TradeException("订单创建失败,订单号："+ payRequest.getOrderSn());
        }
    }



    @Override
    public Boolean closePay(String orderSn) throws TradeException {
        try {
            AlipayClient alipayClient = getAliHttpClient();
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("trade_no", orderSn);
            request.setBizContent(bizContent.toString());
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("调用成功");
                return true;
            } else {
                log.error("调用失败");
                return false;
            }
        }catch (Exception e){
            throw new TradeException("订单关闭失败,订单号："+orderSn);
        }
    }

    @Override
    public Boolean refund(RefundRequest refundRequest) throws TradeException {
        try {
            AlipayClient alipayClient = getAliHttpClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setNotifyUrl(getRefundNotifyUrl()); //退款回调
            JSONObject bizContent = new JSONObject();
            bizContent.put("refund_amount", fenToYuan(refundRequest.getRefundAmount() ));
            bizContent.put("out_trade_no", refundRequest.getOrderSn());
            //退款请求号，做幂等性校验
            if(refundRequest.getRequestNo()!=null){
                bizContent.put("out_request_no", refundRequest.getRequestNo());
            }else{
                bizContent.put("out_request_no", refundRequest.getOrderSn());
            }
            request.setBizContent(bizContent.toString());
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                if("Y".equals(response.getFundChange())) {
                    log.info("退款成功{}",refundRequest.getOrderSn());
                    callBackService.successRefund(refundRequest.getOrderSn());
                    return true;
                }else{
                    //退款失败
                    log.error("退款失败{}",refundRequest.getOrderSn());
                    callBackService.failRefund(refundRequest.getOrderSn());
                    return false;
                }
            } else {
                log.error("退款调用失败{}",refundRequest.getOrderSn());
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new TradeException("订单退款失败,订单号："+ refundRequest.getOrderSn());
        }
    }

    @Override
    public QueryResponse queryTradingOrderNo(String orderSn) throws TradeException {
        AlipayClient alipayClient = getAliHttpClient();
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderSn);
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            QueryResponse queryResponse=new QueryResponse();
            queryResponse.setOrder_sn(orderSn );
            if (response.isSuccess()) {
                queryResponse.setTransaction_id( response.getTradeNo() );
                queryResponse.setTrade_state(   AlipayConstant.TRADE_STATE.get(  response.getTradeStatus()) );//交易状态

                //int total = BigDecimal.valueOf(Double.valueOf(response.getTotalAmount())).multiply(new BigDecimal(100)).intValue();
                queryResponse.setTotal(  yuanToFen(response.getTotalAmount())   ); //总金额

                //int buyer_pay_amount = BigDecimal.valueOf(Double.valueOf(response.getBuyerPayAmount())).multiply(new BigDecimal(100)).intValue();
                //queryResponse.setPayer_total( yuanToFen(response.getBuyerPayAmount()) );//支付金额

                queryResponse.setOpenid( response.getBuyerUserId());
                Map map = JSON.parseObject(response.getBody(), Map.class  ) ;
                queryResponse.setExpand(map);//全部数据
                return queryResponse;
            } else {
                queryResponse.setTrade_state("NOTPAY");
                return queryResponse;
            }
        }catch (Exception e){
            e.printStackTrace();
            //throw new TradeException("订单查询失败,订单号："+orderSn);
            return null;
        }
    }


    @Override
    public QueryRefundResponse queryRefundTrading(String orderSn) throws TradeException {
        try {
            AlipayClient alipayClient = getAliHttpClient();
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_request_no", orderSn);
            request.setBizContent(bizContent.toString());
            AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("调用成功");
                Map map = JSON.parseObject(response.getBody(), Map.class   );

                QueryRefundResponse queryRefundResponse=new QueryRefundResponse();

                queryRefundResponse.setOrder_sn( (String) map.get("out_trade_no") );
                queryRefundResponse.setTransaction_id(   (String) map.get("trade_no") );
                queryRefundResponse.setTotal( (Integer) map.get("total_amount")  ); //总金额
                //queryRefundResponse.setPayer_total( (Integer) map.get("total_amount")  );//支付金额
                queryRefundResponse.setRefund((Integer) map.get("refund_amount")   ); //退款金额
                queryRefundResponse.setRefund_id((String) map.get("trade_no")  );  //退款单号
                queryRefundResponse.setOut_refund_no( (String) map.get("out_request_no")    );//退款订单号
                //queryRefundResponse.setChannel(  (String) map.get("channel")  );  //通道
                //queryRefundResponse.setUser_received_account(  (String) map.get("user_received_account") ); //账号
                queryRefundResponse.setStatus(   (String) map.get("refund_status")  ); //状态
                queryRefundResponse.setSuccess_time(  (String) map.get("gmt_refund_pay")  );
                //queryRefundResponse.setCreate_time(  (String) map.get("gmt_refund_pay") );

                queryRefundResponse.setExpand(map);
                return queryRefundResponse;

            } else {
                log.error("调用失败");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            //throw new TradeException("退款订单查询失败,订单号："+ orderSn);
            return null;
        }
    }

    @Override
    public String getOpenid(String code) {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
                alipayConfig.getAppId(), alipayConfig.getPrivateKey(), "json", "utf-8", alipayConfig.getPublicKey(), alipayConfig.getSignType());
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setCode(code);
        request.setGrantType("authorization_code");
        try {
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
            return  oauthTokenResponse.getUserId();
        } catch (AlipayApiException e) {
            //处理异常
            e.printStackTrace();
            return "";
        }
    }


    private AlipayClient getAliHttpClient(){
        try {
            com.alipay.api.AlipayConfig alipayConfig = new com.alipay.api.AlipayConfig();
            alipayConfig.setServerUrl(AlipayConstant.domain);
            alipayConfig.setAppId(this.alipayConfig.getAppId());
            alipayConfig.setPrivateKey(this.alipayConfig.getPrivateKey());
            alipayConfig.setFormat("json");
            alipayConfig.setCharset("utf-8");
            alipayConfig.setAlipayPublicKey(this.alipayConfig.getPublicKey());
            alipayConfig.setSignType(this.alipayConfig.getSignType());
            //构造client
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            return alipayClient;
        }catch (Exception e){
            e.printStackTrace();
            throw new TradeException("微信支付--初始化，校验系统参数失败");
        }

    }

    private String fenToYuan(int fen){
        //转换为元
        return BigDecimal.valueOf(fen).divide(new BigDecimal(100)  ).toString();
    }

    private int yuanToFen(String yuan){
        return BigDecimal.valueOf(Double.valueOf(yuan)).multiply(new BigDecimal(100)).intValue();
    }


}