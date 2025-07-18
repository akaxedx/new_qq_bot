package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.tools.BaseReplyConstant;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class AtPlugin extends BotPlugin {
    private final static Logger logger = LoggerFactory.getLogger(AtPlugin.class);

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String reply = "";
        if (event.getMessage().equals("上号")) {
            if (event.getGroupId().equals(923796903L)) {
                reply = "[CQ:at,qq=1939689768] " +
                        "[CQ:at,qq=2712792122] " +
                        "[CQ:at,qq=1365957941] " +
                        "[CQ:at,qq=1394938659] " +
                        "[CQ:at,qq=3530180056] " +
                        "[CQ:at,qq=1050931105] " +
                        "英雄集结，上号征战";
            }
            if (event.getGroupId().equals(912990547L)) {
                reply = "[CQ:at,qq=2223951483] " +
                        "[CQ:at,qq=1365957941] " +
                        "[CQ:at,qq=1836115798] " +
                        "[CQ:at,qq=625888757] " +
                        "英雄集结，上号征战";
            }
            bot.sendGroupMsg(event.getGroupId(),reply, false);
        }

        String[] msgs = event.getMessage().split("\\[CQ:at,qq=|]");
        logger.info(Arrays.toString(msgs));
        if (msgs[0].startsWith("轰炸")) {
            reply = "[CQ:at,qq=%s] %s";
            if (msgs[0].equals("轰炸")) {
                for (int i = 0; i < 3; i++) {
                    bot.sendGroupMsg(event.getGroupId(), String.format(reply,msgs[1],msgs[2]),false);
                }
            } else {
                try {
                    for (int i = 0; i < (Math.min(Integer.parseInt(msgs[0].split("轰炸")[1]), 10)); i++) {
                        bot.sendGroupMsg(event.getGroupId(), String.format(reply,msgs[1],msgs[2]),false);
                    }
                } catch (RuntimeException e) {
                    bot.sendGroupMsg(event.getGroupId(), BaseReplyConstant.ERROR,false);
                }
            }
        }

        return MESSAGE_IGNORE;
    }
}
