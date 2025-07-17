package com.akaxedx.newqqbot.tools.DingUtil;

import com.akaxedx.newqqbot.entity.Ding;
import com.akaxedx.newqqbot.mappers.DingMapper;
import com.akaxedx.newqqbot.tools.BaseReplyConstant;
import com.akaxedx.newqqbot.tools.inter.BaseUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.akaxedx.newqqbot.tools.SenderType;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
public class BaseUtilImpl implements BaseUtil {


    private final Logger logger = LoggerFactory.getLogger(BaseUtilImpl.class);
    @Autowired
    private DingMapper dingMapper;


    @Override
    public String doThis(MessageEvent event, Bot bot) {
        String reply;
        switch (event.getMessage().split("\\[CQ:at,qq=|]")[0]){
            case DingConstant.DING_GET -> {
                reply = getDing(event, bot);
            }
            case DingConstant.DING_CULTIVATE -> {
                reply = cultivate(event, bot);
            }
            case DingConstant.DING_BATTLE -> {
                reply = battle(event, bot);
            }
            case DingConstant.DING_BANG -> {
                List<Ding> dings = dingMapper.selectDingBang();
                reply = "金箍榜\n";
                for (int i = 0; i < dings.size(); i++) {
                    reply += "第" + (i+1) +"名：" +
                            bot.getStrangerInfo(dings.get(i).getId(),false).getData().getNickname()+
                            "，"+dings.get(i).getLength()+"m\n";
                }
            }
            default -> {
                return null;
            }
        }
        return reply;
    }

    private String getDing(MessageEvent event, Bot bot) {
        QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Ding::getId, event.getUserId());
        List<Ding> dings = dingMapper.selectList(queryWrapper);
        String reply = BaseReplyConstant.DING_GET_NULL;
        Random r = new Random();
        Double length = r.nextDouble() * 2 + 3;

        ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(event.getUserId(), false);
        String userName = strangerInfo.getData().getNickname();


        switch (event.getClass().getSimpleName()){
            case SenderType.PRIVATE -> {
                PrivateMessageEvent pEvent = (PrivateMessageEvent) event;
                // 没有金箍棒
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    Ding newDing = new Ding(event.getUserId(), length, 1, 0L);
                    dingMapper.insertOrUpdate(newDing);
                    return String.format(reply,
                            pEvent.getPrivateSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                // 已有金箍棒
                reply = String.format(BaseReplyConstant.DING_GET_ONE,
                        pEvent.getPrivateSender().getNickname() ,
                        String.format("%.3f", dings.get(0).getLength()));
            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                // 没有金箍棒
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    Ding newDing = new Ding(event.getUserId(), length, 1, 0L);
                    dingMapper.insertOrUpdate(newDing);
                    return "@"+userName+"："+String.format(reply,
                            gEvent.getSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                // 已有金箍棒
                reply = "@"+userName+"："+ String.format(BaseReplyConstant.DING_GET_ONE,
                        gEvent.getSender().getNickname(),
                        String.format("%.3f", dings.get(0).getLength()));

            }
            default -> reply = null;

        }
        return reply;
    }
    private String cultivate(MessageEvent event, Bot bot) {
        QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Ding::getId, event.getUserId());
        List<Ding> dings = dingMapper.selectList(queryWrapper);
        String reply = BaseReplyConstant.NO_DING;
        Random r = new Random();
        double luck = r.nextDouble();
        // 暴击 1-3，普通 0-1，暴击率30%
        boolean lucky;
        double length;
        if (luck >= 0.7) {
            length =  r.nextDouble() * 2 + 1;
            lucky = true;
        } else {
            length = r.nextDouble();
            lucky = false;
        }

        switch (event.getClass().getSimpleName()){
            case SenderType.PRIVATE -> {
                PrivateMessageEvent pEvent = (PrivateMessageEvent) event;
                ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(pEvent.getUserId(), false);
                String userName = strangerInfo.getData().getNickname();
                //  没领取或清空
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return String.format(reply,
                            pEvent.getPrivateSender().getNickname());
                }
                // 领取了

                reply = cLength(dings.get(0),length,lucky,userName);

            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(gEvent.getUserId(), false);
                String userName = strangerInfo.getData().getNickname();
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return "@"+userName+"："+ String.format(reply,
                            gEvent.getSender().getNickname());
                }
                // 领取了

                reply = cLength(dings.get(0),length,lucky, strangerInfo.getData().getNickname());

            }
            default -> reply = null;

        }
        return reply;
    }
    private String battle(MessageEvent event, Bot bot) {
        QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Ding::getId, event.getUserId());
        List<Ding> dings = dingMapper.selectList(queryWrapper);
        String reply;
        Random r = new Random();
        double luck = r.nextDouble();
        // 暴击 1-3，普通 0-1，暴击率30%
        boolean alive;
        boolean lucky;
        double length;
        length =  r.nextDouble() * 2 + 1;
        if (luck > 0.02) {
            alive = true;
            lucky = luck > 0.51;
        } else {
            alive = false;
            lucky = luck > 0.01;
        }

        switch (event.getClass().getSimpleName()){
            case SenderType.PRIVATE -> {
                reply = BaseReplyConstant.PRIVATE_BATTLE;
            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                // 没领或清空
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return String.format(BaseReplyConstant.NO_DING,
                            gEvent.getSender().getNickname());
                }
                logger.info("消息内容->{}",event.getMessage());
                Ding thisUser = dings.get(0);
                String[] split = event.getMessage().split("\\[CQ:at,qq=|]");
                if (split.length == 1) {
                    // 无指定对象
                    QueryWrapper<Ding> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(Ding::getHas, DingConstant.HAS_DING);
                    List<Ding> allDingList = dingMapper
                            .selectList(queryWrapper1)
                            .stream()
                            .filter(ding -> !ding.getId().equals(thisUser.getId()))
                            .toList();
                    int battleUserIndex = r.nextInt(allDingList.size());
                    Ding battleUser = allDingList.get(battleUserIndex);
                    ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(thisUser.getId(), false);
                    ActionData<StrangerInfoResp> strangerInfo2 = bot.getStrangerInfo(battleUser.getId(), false);
                    String thisName = strangerInfo.getData().getNickname();
                    String battleName = strangerInfo2.getData().getNickname();
                    reply = bLength(thisName,battleName,lucky, alive, thisUser, length, battleUser,bot,gEvent.getGroupId());
                } else {
                    logger.info("进入分流");
                    QueryWrapper<Ding> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(Ding::getId, split[1]);
                    List<Ding> dings1 = dingMapper.selectList(queryWrapper1);
                    if (dings1.isEmpty() || dings1.get(0).getHas().equals(DingConstant.NO_DING)) {
                        reply = BaseReplyConstant.THAT_NO_DING;
                    } else {
                        Ding battleUser = dings1.get(0);
                        ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(thisUser.getId(), false);
                        ActionData<StrangerInfoResp> strangerInfo2 = bot.getStrangerInfo(battleUser.getId(), false);
                        String thisName = strangerInfo.getData().getNickname();
                        String battleName = strangerInfo2.getData().getNickname();
                        reply = bLength(thisName,battleName,lucky, alive, thisUser, length, battleUser,bot,gEvent.getGroupId());
                    }
                }

            }
            default -> reply = null;

        }
        return reply;
    }
    private String cLength(Ding ding, Double length, Boolean lucky, String userName) {
        long now = Instant.now().getEpochSecond();
        Long lastest = ding.getTime();
        long middle = now - lastest;
        // 长度超长
        if (ding.getLength() >= 20) {
            return "@"+userName+"："+BaseReplyConstant.TOO_LONG;
        }
        // 时间不够
        if (middle < 180) {
            return "@"+userName+"："+ String.format(BaseReplyConstant.NEED_RELAX, 180-middle);
        }
        // 时间够了
        String reply;
        Double newLength = ding.getLength() + length;
        ding.setLength(newLength);
        ding.setTime(now);
        dingMapper.updateById(ding);
        // 暴击了
        if (lucky) {
            reply = String.format(BaseReplyConstant.CULTIVATE,
                    "绝佳",
                    String.format("%.3f", length) ,
                    String.format("%.3f", ding.getLength()));
        } else {
            reply = String.format(BaseReplyConstant.CULTIVATE,
                    "一般",
                    String.format("%.3f", length)  ,
                    String.format("%.3f", ding.getLength()));
        }
        return "@"+userName+"："+ reply;
    }
    private String bLength(String thisName,String battleName, boolean lucky, boolean alive, Ding thisUser, double length, Ding battleUser,Bot bot,Long groupId) {
        Random r = new Random();
        String reply = null;
        double battleLength = 0;
        double thisLength = 0;
        // 已有金箍棒
        if (lucky && alive) {
            battleLength = battleUser.getLength() - length;
            thisLength = thisUser.getLength() + length;
            battleUser.setLength(battleLength);
            thisUser.setLength(thisLength);
            dingMapper.updateById(battleUser);
            dingMapper.updateById(thisUser);
            reply = String.format(BaseReplyConstant.BATTLE_SUCCESS,
                    battleName,
                    String.format("%.3f", thisLength),
                    battleName,
                    String.format("%.3f", battleLength));
        } else if (lucky && !alive) {
            thisLength = thisUser.getLength() + 2 * length;
            battleUser.setHas(DingConstant.NO_DING);
            thisUser.setLength(thisLength);
            dingMapper.updateById(battleUser);
            dingMapper.updateById(thisUser);
            reply = String.format(BaseReplyConstant.BATTLE_BOOM_THAT,
                    battleName,
                    battleName,
                    String.format("%.3f",thisLength),
                    battleName);
        } else if (!lucky && alive) {
            battleLength = battleUser.getLength() + length;
            thisLength = thisUser.getLength() - length;
            battleUser.setLength(battleLength);
            thisUser.setLength(thisLength);
            dingMapper.updateById(battleUser);
            dingMapper.updateById(thisUser);
            reply = String.format(BaseReplyConstant.BATTLE_FAIL,
                    battleName,
                    String.format("%.3f", thisLength),
                    battleName,
                    String.format("%.3f", battleLength));
        } else if (!lucky && !alive) {
            battleLength = battleUser.getLength() + 2 * length;
            battleUser.setLength(battleLength);
            thisUser.setHas(DingConstant.NO_DING);
            dingMapper.updateById(battleUser);
            dingMapper.updateById(thisUser);
            reply = String.format(BaseReplyConstant.BATTLE_BOOM_YOU,
                    battleName,
                    battleName,
                    String.format("%.3f",battleLength));
        }

        reply = "@"+thisName+"："+reply;

        if (battleLength >= 30) {
            dingMapper.deleteDing();
            bot.sendGroupMsg(groupId,reply,false);
            reply = String.format(BaseReplyConstant.BATTLE_WIN,
                    battleName,
                    battleLength);
        }
        if (thisLength >= 30) {
            dingMapper.deleteDing();
            bot.sendGroupMsg(groupId,reply,false);
            reply = String.format(BaseReplyConstant.BATTLE_WIN,
                    thisName,
                    thisName);
        }
        return reply;
    }
}
