package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.tools.inter.BaseUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class DingPlugin extends BotPlugin {

    Logger logger = LoggerFactory.getLogger(DingPlugin.class);

    @Autowired
    private BaseUtil baseUtil;
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String reply = "";
        reply = baseUtil.doThis(event);
        if (null == reply) {
            return MESSAGE_IGNORE;
        }
        bot.sendPrivateMsg(event.getUserId(), reply, false);
        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String reply = "";
        reply = baseUtil.doThis(event);
        if (null == reply) {
            return MESSAGE_IGNORE;
        }
        bot.sendGroupMsg(event.getGroupId(), reply, false);
        return MESSAGE_IGNORE;
    }

}
