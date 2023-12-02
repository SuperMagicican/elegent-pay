package cn.elegent.pay.wxpay;

import cn.elegent.pay.annotation.TradePlatform;
import cn.elegent.pay.config.CallbackConfig;
import cn.elegent.pay.constant.PayConstant;
import cn.elegent.pay.constant.Platform;
import cn.elegent.pay.core.ElegentTrade;
import cn.elegent.pay.core.WatchList;
import cn.elegent.pay.dto.*;
import cn.elegent.pay.exceptions.TradeException;
import cn.elegent.pay.util.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@TradePlatform(Platform.WX)
public class WxPayElegentTrade implements ElegentTrade {

    @Autowired
    private WxpayConfig wxpayConfig;

    @Autowired
    private CallbackConfig callbackConfig;

    /**
     * 创建微信支付订单方法
     * 这里参考官网代码：
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml   Native下单
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_1.shtml   JSAPI 下单
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_3_1.shtml   H5下单
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_2_1.shtml   APP下单
     *  payRequest 支付请求
     *  支付响应
     */
    @Override
    public PayResponse requestPay(PayRequest payRequest,String tradeType)throws TradeException {
        PayResponse payResponse = new PayResponse(); //返回结果
        try {
            // 请求body参数构建
            Map<String, Object> params = new HashMap<String, Object>() {
                {
                    put("mchid", wxpayConfig.getMchId());
                    put("appid", wxpayConfig.getAppId());
                    put("notify_url", callbackConfig.getDomain()+ PayConstant.CALLBACK_PATH + PayConstant.NOTIFY +"/"+ Platform.WX );
                    put("out_trade_no", payRequest.getOrderSn());
                    put("amount", new HashMap<String, Object>() {
                        {
                            put("total", payRequest.getTotalFee());//金额，单位：分
                            put("currency", "CNY");//人民币
                        }
                    });
                    put("description", payRequest.getBody());
                }
            };

            if("h5".equals(tradeType)){ //h5
                params.put("scene_info", new HashMap<String, Object>() {
                    {
                        put("payer_client_ip", "127.0.0.1");
                        put("h5_info", new HashMap<String, Object>() {
                            {
                                put("type", "Wap");
                            }
                        });
                    }
                });
            }
            if ("jsapi".equals(tradeType)) {  //如果是小程序支付
                params.put("payer", new HashMap<String, Object>() {
                    {
                        put("openid", payRequest.getOpenid());
                    }
                });
            }


            String url = WxpayConstant.createOrder + tradeType; //创建订单
            log.info("elegent-pay 请求参数{}",params);
            Map<String, String> map = postApiTemplate(url, params);

            if("SUCCESS".equals( map.get("code") )){
                payResponse.setOrder_sn(payRequest.getOrderSn());
                payResponse.setSuccess(true);
                payResponse.setCode_url(map.get("code_url"));
                payResponse.setMessage(map.get("message"));

                payResponse.setPrepay_id(map.get("prepay_id"));
                payResponse.setH5_url( map.get("h5_url") );

                payResponse.setExpand(map); //全部数据

                if("jsapi".equals(tradeType)){//如果是小程序，需要封装到Expand
                    Map<String, String> data=new HashMap<>();
                    String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
                    String nonceStr = IdUtil.simpleUUID();
                    String packages = "prepay_id=" + payResponse.getPrepay_id();
                    String privateKey = FileUtil.readToStr("wxpay_private.key");
                    String paySign = this.createPaySign(wxpayConfig.getAppId(), timeStamp, nonceStr, packages, privateKey);
                    data.put("appId", wxpayConfig.getAppId());// appid
                    data.put("timeStamp", timeStamp);// 时间戳
                    data.put("nonceStr", nonceStr);// 随机字符串
                    data.put("package","prepay_id="+payResponse.getPrepay_id());
                    data.put("signType", "RSA");// 签名类型，默认为RSA，仅支持RSA
                    data.put("paySign", paySign);// 签名
                    data.put("orderNo",payRequest.getOrderSn());
                    payResponse.setJsapiData(data);
                }
                log.info("createOrder: {}", payResponse);

            }else{
                payResponse.setSuccess(false);
                payResponse.setMessage( map.get("message") );
            }
            return payResponse;
        }catch (Exception e){
            e.printStackTrace();
            payResponse.setSuccess(false);
        }
        return payResponse;
    }



    @Override
    public Boolean closePay(String orderSn) throws TradeException {
        // 请求body参数构建
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("mchid", wxpayConfig.getMchId());
        String url = WxpayConstant.closeOrder.replaceAll("\\{out_trade_no\\}",orderSn);
        Map map = postApiTemplate(url, params);
        if("SUCCESS".equals(  map.get("code"))){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Boolean refund(RefundRequest refundRequest) {
        // 请求body参数构建
        // 请求body参数构建
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notify_url", callbackConfig.getDomain()+ PayConstant.CALLBACK_PATH +PayConstant.REFUND_NOTIFY +"/"+ Platform.WX    );  //回调地址
        params.put("out_trade_no", refundRequest.getOrderSn());//订单编号
        //out_refund_no
        params.put("out_refund_no", refundRequest.getRequestNo());//退款申请单编号  （多次退款需要不一样才行）
        params.put("amount", new HashMap<String, Object>() {
            {
                put("refund",refundRequest.getRefundAmount());//退款金额
                put("total", refundRequest.getTotalFee());//原金额，单位：分
                put("currency", "CNY");//人民币
            }
        });
        String url = WxpayConstant.refund;  //创建订单
        Map map = postApiTemplate(url, params);
        if("SUCCESS".equals(  map.get("code"))){

            return true;
        }else{
            return false;
        }
    }


    @Override
    public QueryResponse queryTradingOrderNo(String orderSn) throws TradeException {
        // 请求body参数构建
        Map<String, String> params = new HashMap<String, String>();
        params.put("mchid", wxpayConfig.getMchId());
        String url = WxpayConstant.queryOrderNo+orderSn;;
        try {
            Map map = getApiTemplate(url, params);
            QueryResponse queryResponse=new QueryResponse();
            queryResponse.setOrder_sn((String)map.get("out_trade_no")  ); //订单号
            queryResponse.setTransaction_id((String)map.get("transaction_id") ); //交易单类型
            queryResponse.setTrade_state(   (String) map.get("trade_state") );//交易状态
            Map amount = (Map)map.get("amount");
            if(amount!=null){
                queryResponse.setTotal( (Integer) amount.get("total")  ); //总金额
            }
            Map payer= (Map)map.get("payer");
            if(payer!=null){
                queryResponse.setOpenid(  (String)payer.get("openid")  );
            }
            queryResponse.setExpand(map);//全部数据
            return queryResponse;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public QueryRefundResponse queryRefundTrading(String out_refund_no) throws TradeException {

        // 请求body参数构建
        Map<String, String> params = new HashMap<String, String>();
        String url = WxpayConstant.queryRufundOrderNo + out_refund_no;
        try {
            Map map = getApiTemplate(url, params);
            QueryRefundResponse queryRefundResponse=new QueryRefundResponse();
            queryRefundResponse.setOrder_sn( (String) map.get("out_trade_no") );
            queryRefundResponse.setTransaction_id(   (String) map.get("transaction_id") );
            Map amount = (Map)map.get("amount");
            queryRefundResponse.setTotal( (Integer) amount.get("total")  ); //总金额
            queryRefundResponse.setRefund((Integer) amount.get("payer_refund")   ); //退款金额
            queryRefundResponse.setRefund_id((String) map.get("refund_id")  );  //退款单号
            queryRefundResponse.setOut_refund_no( (String) map.get("out_refund_no")    );//退款订单号

            queryRefundResponse.setChannel(  (String) map.get("channel")  );  //通道
            queryRefundResponse.setUser_received_account(  (String) map.get("user_received_account") ); //账号
            queryRefundResponse.setStatus(   (String) map.get("status")  ); //状态
            queryRefundResponse.setSuccess_time(  (String) map.get("success_time")  );
            queryRefundResponse.setExpand(map);
            return queryRefundResponse;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getOpenid(String code) {
        String getOpenIdUrl = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid="+wxpayConfig.getAppId()
                +"&secret="+wxpayConfig.getAppSecret()
                +"&js_code="+code+"&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String respResult = restTemplate.getForObject(getOpenIdUrl,String.class);
        log.info("获取openid的url:{},respResult：{}",getOpenIdUrl,respResult);
        if( respResult==null || "".equals(respResult)  ) return "";
        try{

            Map<String,String> map = JSON.parseObject(respResult, Map.class);
            String errorCode = map.get("errcode") ;
            if(errorCode!=null && !"".equals(errorCode)){
                int errorCodeInt = Integer.valueOf(errorCode).intValue();

                log.info("获取openid的errorCode,{}",errorCodeInt);
                if(errorCodeInt != 0) return "";
            }
            return map.get("openid");
        }catch (Exception ex){
            ex.printStackTrace();
            return "";
        }
    }


    private Map getApiTemplate(String url, Map<String, String> params) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(url);
        //添加参数
        for (String key : params.keySet()) {
            uriBuilder.addParameter(key, params.get(key));
        }
        //完成签名并执行请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.addHeader("Accept", "application/json");
        CloseableHttpClient httpClient = getWxHttpClient();
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return responseTemplate(response);
    }


    private Map responseTemplate(CloseableHttpResponse response) {
        Map result = null;
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                log.info("success,return body = " + EntityUtils.toString(response.getEntity()));
                result = JSON.parseObject(EntityUtils.toString(response.getEntity()), Map.class);
                result.put("code", "SUCCESS");
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("success");
                result = new HashMap<String, Object>() {
                    {
                        put("code", "SUCCESS");
                    }
                };
            } else {
                String returnBody = EntityUtils.toString(response.getEntity());
                log.error("failed,resp code = " + statusCode + ",return body = " + returnBody);
                //throw new TradeException("创建本地支付订单失败！"+payDTO.getOrderSn());
                Map<String, String> map = JSON.parseObject(returnBody, Map.class);
                result = new HashMap<String, Object>() {
                    {
                        put("code", "FAIL");
                        put("message", map.get("message"));
                    }
                };
            }
        } catch (Exception e) {
            result = new HashMap<String, Object>() {
                {
                    put("code", "FAIL");
                    put("message", e.getMessage());
                }
            };
        } finally {
            closeConnect(response);
            return result;
        }
    }

    private Map postApiTemplate(String url, Map<String, Object> params) {
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(JSON.toJSONString(params));
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            //完成签名并执行请求
            CloseableHttpClient httpClient = getWxHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return responseTemplate( response );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 获取微信HTTP连接
     *
     * @return
     */
    private CloseableHttpClient getWxHttpClient() {
        try {
            //这里对秘钥进行加密使用的完全是官网上的代码
            //TODO 参考官网代码 https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_6_2.shtml
            // 加载商户私钥（privateKey：私钥字符串）
            String key = FileUtil.readToStr("wxpay_private.key");

            PrivateKey merchantPrivateKey = PemUtil
                    .loadPrivateKey(new ByteArrayInputStream(key.getBytes("utf-8")));
            // 加载平台证书（mchId：商户号,mchSerialNo：商户证书序列号,apiV3Key：V3密钥）
            //PrivateKey merchantPrivateKey = keyManager.getPrivateKey("wxpay_private.key");//读取私钥
            PrivateKeySigner privateKeySigner = new PrivateKeySigner(wxpayConfig.getMchSerialNo(), merchantPrivateKey);
            WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(
                    wxpayConfig.getMchId(), privateKeySigner);
            // 向证书管理器增加需要自动更新平台证书的商户信息
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            certificatesManager.putMerchant(wxpayConfig.getMchId(), wechatPay2Credentials, wxpayConfig.getApiV3Key().getBytes("utf-8"));
            // 初始化httpClient
            return WechatPayHttpClientBuilder.create()
                    .withMerchant(wxpayConfig.getMchId(), wxpayConfig.getMchSerialNo(), merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(certificatesManager.getVerifier(wxpayConfig.getMchId()))).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("微信支付--初始化，校验系统参数失败");
        }

    }


    /**
     * 关闭资源
     */
    private void closeConnect(CloseableHttpResponse response) {
        try {
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("资源回收出错");
        }
    }


    /**
     *  创建支付签名
     * @param appid
     * @param timeStamp
     * @param nonceStr
     * @param packages
     * @param privateKey
     * @return
     * @throws Exception
     */
    private String createPaySign(String appid, String timeStamp, String nonceStr, String packages,String  privateKey) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        // 加载商户私钥
        PrivateKey key = PemUtil
                .loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes(CharsetUtil.CHARSET_UTF_8)));
        sign.initSign(key);
        String message = StrUtil.format("{}\n{}\n{}\n{}\n",
                appid,
                timeStamp,
                nonceStr,
                packages);
        sign.update(message.getBytes());
        return Base64.getEncoder().encodeToString(sign.sign());
    }


}
