package cn.elegent.pay.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static String readToStr(String fileName){
        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        byte[] buffer = new byte[1024];

        String str;
        try {
            int length;
            while((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }

            str = os.toString("UTF-8");
        } catch (IOException var5) {
            throw new IllegalArgumentException("无效的密钥", var5);
        }
        return str;
    }

}
