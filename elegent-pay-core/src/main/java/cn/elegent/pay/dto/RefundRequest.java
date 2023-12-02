package cn.elegent.pay.dto;

import lombok.Data;

@Data
public class RefundRequest {

    private int totalFee; //订单金额

    private int refundAmount; //退款金额

    private String orderSn; //订单号

    private String requestNo; //退款请求号，做退款幂等性校验，当部分退款时必须给出

}
