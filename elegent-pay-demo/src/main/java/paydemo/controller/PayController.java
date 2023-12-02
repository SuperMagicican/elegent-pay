package paydemo.controller;

import cn.elegent.pay.ElegentPay;
import cn.elegent.pay.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private ElegentPay elegentPay;

    /**
     * 创建订单
     * @return
     */
    @GetMapping("/requestPay/{tradeType}/{platform}")
    public PayResponse requestPay(@PathVariable("tradeType") String tradeType,@PathVariable("platform") String platform){
        PayRequest payRequest = new PayRequest();
        payRequest.setTotalFee(100);//金额
        payRequest.setOrderSn(System.currentTimeMillis()+"");//订单号
        payRequest.setBody("elegent");// 商品名称
        payRequest.setOpenid("oJ9WJ5MhIS-hiwuUX0GmsHDzqTyQ");
        PayResponse payResponse = elegentPay.requestPay(payRequest,tradeType, platform);
        return payResponse;
    }

    /**
     * 关闭订单
     * @param orderNo
     * @return
     */
    @GetMapping("/closeOrder/{orderNo}/{platform}")
    public Boolean closeOrder(@PathVariable("orderNo") String orderNo,@PathVariable("platform") String platform){
        Boolean aBoolean = elegentPay.closePay(orderNo,platform);
        return aBoolean;
    }

    /**
     * 退款
     * @param orderNo
     * @return
     */
    @GetMapping("/refund/{orderNo}/{platform}")
    public boolean refund(@PathVariable("orderNo") String orderNo,@PathVariable("platform") String platform){
        RefundRequest refundRequest=new RefundRequest();
        refundRequest.setTotalFee(100);
        refundRequest.setRefundAmount(100);
        refundRequest.setOrderSn(orderNo);
        refundRequest.setRequestNo(System.currentTimeMillis()+"");
        Boolean refund = elegentPay.refund(refundRequest,platform);
        return refund;
    }

    /**
     * 查询订单
     * @param orderNo
     * @return
     */
    @GetMapping("/query/{orderNo}/{platform}")
    public QueryResponse query(@PathVariable("orderNo") String orderNo,@PathVariable("platform") String platform){
        QueryResponse queryResponse = elegentPay.queryTradingOrderNo(orderNo,platform);
        return queryResponse;
    }

    /**
     * 查询退款订单
     * @param orderNo
     * @return
     */
    @GetMapping("/queryRefund/{orderNo}/{platform}")
    public QueryRefundResponse queryRefund(@PathVariable("orderNo") String orderNo,@PathVariable("platform") String platform){
        QueryRefundResponse queryRefundResponse = elegentPay.queryRefundTrading(orderNo,platform);
        return queryRefundResponse;
    }


}