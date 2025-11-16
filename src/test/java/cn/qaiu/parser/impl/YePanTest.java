package cn.qaiu.parser.impl;

import cn.qaiu.entity.ShareLinkInfo;
import cn.qaiu.parser.ParserCreate;
import io.vertx.core.MultiMap;
import org.junit.Test;

import java.util.HashMap;

public class YePanTest {


    public static void main(String[] args) {
        ParserCreate parserCreate = ParserCreate.fromShareUrl("https://www.123pan.com/s/VzVA-cQ7bd.html");
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjM4NjUwMDcsImlhdCI6MTc2MzI2MDIwNywiaWQiOjE4MTUyNjg2NjUsIm1haWwiOiIiLCJuaWNrbmFtZSI6IjE1Njg3ODg2NDAwIiwic3VwcGVyIjpmYWxzZSwidXNlcm5hbWUiOjE1Njg3ODg2NDAwLCJ2IjowfQ.rbsmv2EB9cPvJH91pkVsoWylzeh8pvdFyt-oIT9H9NU");
        parserCreate.getShareLinkInfo().getOtherParam().put("auths", multiMap);
        String s = parserCreate.createTool().parseSync();
        System.out.println(s);
    }
}
