package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.tools.CozeWorkflowExecutor;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Component
public class AtPlugin extends BotPlugin {
    private final static Logger logger = LoggerFactory.getLogger(AtPlugin.class);
    @Value("${blog.host}")
    private String host;
    @Value("${blog.http-port}")
    private String httpPort;


    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if(!event.getMessage().equals(SendContent.NOTICE_SJZ)) {
            return MESSAGE_IGNORE;
        }
        try {
            String addr = host + ":" +httpPort;
            bot.sendGroupMsg(event.getGroupId(),"正在尝试获取密码",false);
            Map<String, Object> sjzParams = new HashMap<>();
            sjzParams.put("send_id", event.getGroupId());
            sjzParams.put("host", String.format(SendContent.SEND_TO_GROUP, addr));
            sjzParams.put("type", SendContent.NOTICE_SJZ);
            String sjzResponse = CozeWorkflowExecutor.executeWorkflow(
                    SendContent.WORK_FLOW_ID,
                    sjzParams
            );
        } catch (IOException e) {
            logger.info("获取失败");
            bot.sendGroupMsg(event.getGroupId(),"获取密码失败，请重试 ",false);

        }
        return MESSAGE_IGNORE;
    }
}
