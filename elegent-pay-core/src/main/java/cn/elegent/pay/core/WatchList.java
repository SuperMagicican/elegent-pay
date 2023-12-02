package cn.elegent.pay.core;

import cn.elegent.pay.dto.WatchDTO;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Collections.*;

/**
 * 监听列表
 */
public class WatchList {



    public static CopyOnWriteArraySet<WatchDTO> payList=new CopyOnWriteArraySet<>(); //支付中列表

    public static CopyOnWriteArraySet<WatchDTO> refundList=new CopyOnWriteArraySet<>();  //退款中列表

}
