package cn.elegent.pay.dto;

import lombok.Data;

@Data
public class QueryRefundResponse extends QueryResponse{


    private String refund_id;  //微信支付退款单号

    private String out_refund_no;//商户退款单号

    private String channel;//退款渠道

    private String user_received_account;//退款账号

    private String success_time;//退款成功时间

    private String status;//退款状态

    private int refund;//退款金额


}
