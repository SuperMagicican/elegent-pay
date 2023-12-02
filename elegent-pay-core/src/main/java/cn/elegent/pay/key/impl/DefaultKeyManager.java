package cn.elegent.pay.key.impl;
import cn.elegent.pay.key.KeyManager;
import cn.elegent.pay.util.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认的key管理器-文件管理器
 */
public class DefaultKeyManager implements KeyManager {


    @Override
    public String getKey(String name) {
        return FileUtil.readToStr(name);
    }


}
