package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.entity.Live;
import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.mappers.LinkMapper;
import com.akaxedx.newqqbot.tools.CozeWorkflowExecutor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SJZPlugin extends BotPlugin {

    public static final  Logger logger = LoggerFactory.getLogger(SJZPlugin.class);
    @Value("${blog.host}")
    private String host;
    @Value("${blog.http-port}")
    private String httpPort;


    @Resource
    private LinkMapper linkMapper;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getMessage().equals("孩子们，开播了") || event.getMessage().equals("孩子们,开播了")) {
            QueryWrapper<Live> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Live::getUserId, event.getSender().getUserId());
            Live live = linkMapper.selectOne(queryWrapper);
            if (live != null) {
                bot.sendGroupMsg(event.getGroupId(), live.getLink(), false);
            } else {
                bot.sendGroupMsg(event.getGroupId(), "没有配置直播间，请联系管理员进行配置", false);
            }
        }
        if (event.getMessage().startsWith("[CQ:json")) {
            logger.info(event.getMessage());
        }
        if (event.getMessage().equals("战备图")) {
            try {
                String addr = host + ":" +httpPort;
                bot.sendGroupMsg(event.getGroupId(),"请稍等，战备图预计10s内回复",false);
                Map<String, Object> sjzParams = new HashMap<>();
                sjzParams.put("send_id", event.getGroupId());
                sjzParams.put("host", String.format(SendContent.SEND_TO_GROUP, addr));
                sjzParams.put("type", SendContent.READY);
                String sjzResponse = CozeWorkflowExecutor.executeWorkflow(
                        SendContent.WORK_FLOW_ID,
                        sjzParams
                );
            } catch (IOException e) {
                logger.info("获取失败");
                bot.sendGroupMsg(event.getGroupId(),"获取战备图失败，请重试 ",false);

            }
        }
        if (event.getMessage().equals("功能菜单")) {
            bot.sendGroupMsg(event.getGroupId(), SendContent.MENU,false);
        }
        return MESSAGE_IGNORE;
    }
}