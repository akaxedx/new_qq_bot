package com.akaxedx.newqqbot.plugins;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class AtPlugin extends BotPlugin {

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String reply = "";
        if (event.getMessage().equals("上号")) {
            reply =
                    "英雄集结，上号征战";
            bot.sendGroupMsg(event.getGroupId(),reply, false);
        }

        return MESSAGE_IGNORE;
    }
}
