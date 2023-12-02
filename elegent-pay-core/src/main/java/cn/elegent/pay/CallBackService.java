package cn.elegent.pay;

/**
 * 业务回调处理接口
 */
public interface CallBackService {

    /**
     * 成功支付--处理业务逻辑
     * @param orderSn 订单号
     */
    void successPay(String orderSn);

    /**
     * 失败支付-处理业务逻辑
     * @param orderSn 订单号
     */
    void failPay(String orderSn);

    /**
     * 退款成功-处理业务逻辑
     * @param orderSn 订单号
     */
    void successRefund(String orderSn);

    /**
     * 退款失败-处理业务逻辑
     * @param orderSn 订单号
     */
    void failRefund(String orderSn);

}
