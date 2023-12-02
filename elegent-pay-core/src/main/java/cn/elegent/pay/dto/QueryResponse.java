package cn.elegent.pay.dto;

import lombok.Data;

import java.util.Map;

/**
 * 查询响应对象
 */
@Data
public class QueryResponse {

    private String openid;//用户id

    private String trade_state;//交易状态

    private String order_sn;//订单号

    private String transaction_id;//交易单号

    private int total;//金额

    private Map expand;//扩展（全部的返回数据）

}
