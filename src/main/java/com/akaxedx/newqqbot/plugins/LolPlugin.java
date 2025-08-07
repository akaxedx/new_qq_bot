package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.entity.Lol;
import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.mappers.LolMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LolPlugin extends BotPlugin {

    @Resource
    LolMapper lolMapper;
    String AtTemplate = "[CQ:at,qq=%s]";
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (!event.getMessage().equals(SendContent.GAME)) {
            return MESSAGE_IGNORE;
        }
        StringBuilder reply = new StringBuilder();
        String groupId = String.valueOf(event.getGroupId());
        QueryWrapper<Lol> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Lol::getGroupId, groupId);
        List<Lol> lols = lolMapper.selectList(queryWrapper);
        for (Lol lol : lols) {
            reply.append(String.format(AtTemplate, lol.getQqId()));
            reply.append("\n");
        }
        reply.append("上号征战");
        bot.sendGroupMsg(event.getGroupId(), reply.toString(),false);

        return MESSAGE_IGNORE;
    }
}
