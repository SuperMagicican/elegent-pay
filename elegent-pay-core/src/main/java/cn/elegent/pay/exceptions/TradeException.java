package cn.elegent.pay.exceptions;

import lombok.Getter;
import lombok.Setter;

/**
 * 交易SDK提供的总的异常
 */
@Getter
@Setter
public class TradeException extends RuntimeException{
    private String code;
    private String msg;

    public TradeException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public TradeException(String msg) {
        super(msg);
    }
}
