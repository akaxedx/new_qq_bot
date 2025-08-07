package com.akaxedx.newqqbot.plugins;

import com.akaxedx.newqqbot.entity.Rss;
import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.mappers.RssMapper;
import com.akaxedx.newqqbot.tools.CozeWorkflowExecutor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class RssPlugin extends BotPlugin {

    @Resource
    private RssMapper rssMapper;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {

        if(event.getMessage().equals(SendContent.RSS)) {
            QueryWrapper<Rss> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Rss::getGroupId, event.getGroupId());
            Rss rss = rssMapper.selectOne(queryWrapper);
            if (rss!=null) {
                bot.sendGroupMsg(event.getGroupId(), "请勿重复订阅",false);
            } else {
                Rss newRss = new Rss();
                newRss.setGroupId(String.valueOf(event.getGroupId()));
                rssMapper.insert(newRss);
                bot.sendGroupMsg(event.getGroupId(), "本群已成功订阅",false);

            }
        }
        if(event.getMessage().equals(SendContent.NO_RSS)) {
            QueryWrapper<Rss> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Rss::getGroupId, event.getGroupId());
            Rss rss = rssMapper.selectOne(queryWrapper);
            if (rss!=null) {
                rssMapper.deleteById(rss);
                bot.sendGroupMsg(event.getGroupId(), "已取消订阅",false);
            }
        }

        return MESSAGE_IGNORE;
    }
}
