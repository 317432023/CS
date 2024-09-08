package com.laolang.cs.server.authen.test;

import cn.hutool.json.JSONUtil;
import com.comm.pojo.R;
import com.laolang.cs.server.authen.SubsysTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * GetInfoToolTest
 *
 * @since 2023/9/15 10:39
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 同时启动web容器。否则当集成了websocket时，若没启动web容器会导致单元测试报错： javax.websocket.server.ServerContainer not available
@RunWith(SpringRunner.class)
public class SubsysToolTest {
    @Test
    public void testGetInfo() {
        String token = "1c7a7870-d31c-410f-842c-b3de4a4d47c2@m100002";
        String tenantId = "FT1@6";
        R r = SubsysTool.getInfo(token, tenantId);
        System.out.println(JSONUtil.toJsonStr(r));
    }
}
