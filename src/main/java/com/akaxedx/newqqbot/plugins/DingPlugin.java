package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.tools.DingUtil.DingUtilImpl;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class DingPlugin extends BotPlugin {

    Logger logger = LoggerFactory.getLogger(DingPlugin.class);

    @Autowired
    private DingUtilImpl dingUtil;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String reply = "";
        reply = dingUtil.doThis(event, bot);
        if (null == reply) {
            return MESSAGE_IGNORE;
        }
        bot.sendGroupMsg(event.getGroupId(), event.getUserId(),reply, false);
        return MESSAGE_IGNORE;
    }

}
