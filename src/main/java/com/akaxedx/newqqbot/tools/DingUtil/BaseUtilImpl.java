package com.akaxedx.newqqbot.tools.DingUtil;

import com.akaxedx.newqqbot.entity.Ding;
import com.akaxedx.newqqbot.mappers.DingMapper;
import com.akaxedx.newqqbot.tools.BaseReplyConstant;
import com.akaxedx.newqqbot.tools.inter.BaseUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
                List<Ding> dings = dingMapper.selectDingBang(((GroupMessageEvent)event).getGroupId());
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


        String reply = BaseReplyConstant.DING_GET_NULL;
        if (event.getClass().getSimpleName().equals(SenderType.Group)) {
            GroupMessageEvent gEvent = (GroupMessageEvent) event;
            QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Ding::getId, event.getUserId()).eq(Ding::getGroupId, ((GroupMessageEvent) event).getGroupId());
            List<Ding> dings = dingMapper.selectList(queryWrapper);
            Random r = new Random();
            Double length = r.nextDouble() * 2 + 3;

            // 没有金箍棒
            Ding newDing = new Ding(event.getUserId(), length, 1, 0L, gEvent.getGroupId());
            if (dings.isEmpty()) {
                dingMapper.insert(newDing);
                return "[CQ:at,qq="+event.getUserId()+"]" + String.format(reply,
                        gEvent.getSender().getNickname(),
                        String.format("%.3f", newDing.getLength()));
            } else if (dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                UpdateWrapper<Ding> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda().eq(Ding::getId, newDing.getId()).eq(Ding::getGroupId, newDing.getGroupId());
                dingMapper.update(newDing,updateWrapper);
            }
            // 已有金箍棒
            reply = "[CQ:at,qq="+event.getUserId()+"]" + String.format(BaseReplyConstant.DING_GET_ONE,
                    gEvent.getSender().getNickname(),
                    String.format("%.3f", dings.get(0).getLength()));
        } else {
            reply = null;
        }
        return reply;
    }
    private String cultivate(MessageEvent event, Bot bot) {

        String reply = BaseReplyConstant.NO_DING;
        if (event.getClass().getSimpleName().equals(SenderType.Group)) {
            GroupMessageEvent gEvent = (GroupMessageEvent) event;
            QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Ding::getId, event.getUserId()).eq(Ding::getGroupId, gEvent.getGroupId());
            List<Ding> dings = dingMapper.selectList(queryWrapper);

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

            if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                return "[CQ:at,qq="+event.getUserId()+"]" + String.format(reply,
                        gEvent.getSender().getNickname());
            }
            // 领取了
            reply = cLength(dings.get(0), length, lucky, gEvent.getSender().getNickname());
        } else {
            reply = null;
        }
        return reply;
    }
    private String battle(MessageEvent event, Bot bot) {

        String reply;
        if (event.getClass().getSimpleName().equals(SenderType.Group)) {
            GroupMessageEvent gEvent = (GroupMessageEvent) event;
            QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Ding::getId, event.getUserId()).eq(Ding::getGroupId,gEvent.getGroupId());
            List<Ding> dings = dingMapper.selectList(queryWrapper);

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

            // 没领或清空
            if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                return String.format(BaseReplyConstant.NO_DING,
                        gEvent.getSender().getNickname());
            }
            logger.info("消息内容->{}", event.getMessage());
            Ding thisUser = dings.get(0);
            String[] split = event.getMessage().split("\\[CQ:at,qq=|]");
            if (split.length == 1) {
                // 无指定对象
                QueryWrapper<Ding> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(Ding::getHas, DingConstant.HAS_DING).eq(Ding::getGroupId, gEvent.getGroupId());
                List<Ding> allDingList = dingMapper
                        .selectList(queryWrapper1)
                        .stream()
                        .filter(ding -> !ding.getId().equals(thisUser.getId()))
                        .toList();
                if (allDingList.isEmpty()) {
                    reply = "[CQ:at,qq="+event.getUserId()+"]"+"无敌是多么寂寞，无人有金箍棒与您一战";
                } else {
                    int battleUserIndex = r.nextInt(allDingList.size());
                    Ding battleUser = allDingList.get(battleUserIndex);
                    ActionData<StrangerInfoResp> strangerInfo = bot.getStrangerInfo(battleUser.getId(), false);
                    String battleName = strangerInfo.getData().getNickname();
                    reply = bLength(gEvent.getSender().getNickname(), battleName, lucky, alive, thisUser, length, battleUser, bot, gEvent.getGroupId());
                }
            } else {
                logger.info("进入分流");
                QueryWrapper<Ding> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(Ding::getId, split[1]).eq(Ding::getGroupId, gEvent.getGroupId());
                List<Ding> dings1 = dingMapper.selectList(queryWrapper1);
                if (dings1.isEmpty() || dings1.get(0).getHas().equals(DingConstant.NO_DING)) {
                    reply = BaseReplyConstant.THAT_NO_DING;
                } else {
                    Ding battleUser = dings1.get(0);
                    ActionData<StrangerInfoResp> strangerInfo2 = bot.getStrangerInfo(battleUser.getId(), false);
                    String battleName = strangerInfo2.getData().getNickname();
                    reply = bLength(gEvent.getSender().getNickname(), battleName, lucky, alive, thisUser, length, battleUser, bot, gEvent.getGroupId());
                }
            }
        } else {
            reply = null;
        }
        return reply;
    }
    private String cLength(Ding ding, Double length, Boolean lucky, String userName) {
        long now = Instant.now().getEpochSecond();
        Long lastest = ding.getTime();
        long middle = now - lastest;
        // 长度超长
        if (ding.getLength() >= 20) {
            return "[CQ:at,qq="+ding.getId()+"]"+BaseReplyConstant.TOO_LONG;
        }
        // 时间不够
        if (middle < 180) {
            return "[CQ:at,qq="+ding.getId()+"]"+ String.format(BaseReplyConstant.NEED_RELAX, 180-middle);
        }
        // 时间够了
        String reply;
        Double newLength = ding.getLength() + length;
        ding.setLength(newLength);
        ding.setTime(now);
        UpdateWrapper<Ding> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .lambda()
                .eq(Ding::getId,ding.getId())
                .eq(Ding::getGroupId,ding.getGroupId());
        dingMapper.update(ding,updateWrapper);
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
        return "[CQ:at,qq="+ding.getId()+"]"+ reply;
    }
    private String bLength(String thisName,String battleName, boolean lucky, boolean alive, Ding thisUser, double length, Ding battleUser,Bot bot,Long groupId) {
        String reply = null;
        double battleLength = 0;
        double thisLength = 0;
        // 已有金箍棒
        UpdateWrapper<Ding> updateWrapperB = new UpdateWrapper<>();
        UpdateWrapper<Ding> updateWrapperT = new UpdateWrapper<>();
        updateWrapperB
                .lambda()
                .eq(Ding::getId,battleUser.getId())
                .eq(Ding::getGroupId,battleUser.getGroupId());
        updateWrapperT
                .lambda()
                .eq(Ding::getId,thisUser.getId())
                .eq(Ding::getGroupId,thisUser.getGroupId());
        if (lucky && alive) {
            battleLength = battleUser.getLength() - length;
            thisLength = thisUser.getLength() + length;
            battleUser.setLength(battleLength);
            thisUser.setLength(thisLength);
            dingMapper.update(battleUser,updateWrapperB);
            dingMapper.update(thisUser,updateWrapperT);
            reply = String.format(BaseReplyConstant.BATTLE_SUCCESS,
                    battleName,
                    String.format("%.3f", thisLength),
                    battleName,
                    String.format("%.3f", battleLength));
        } else if (lucky && !alive) {
            thisLength = thisUser.getLength() + 2 * length;
            battleUser.setHas(DingConstant.NO_DING);
            thisUser.setLength(thisLength);
            dingMapper.update(battleUser,updateWrapperB);
            dingMapper.update(thisUser,updateWrapperT);
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
            dingMapper.update(battleUser,updateWrapperB);
            dingMapper.update(thisUser,updateWrapperT);
            reply = String.format(BaseReplyConstant.BATTLE_FAIL,
                    battleName,
                    String.format("%.3f", thisLength),
                    battleName,
                    String.format("%.3f", battleLength));
        } else if (!lucky && !alive) {
            battleLength = battleUser.getLength() + 2 * length;
            battleUser.setLength(battleLength);
            thisUser.setHas(DingConstant.NO_DING);
            dingMapper.update(battleUser,updateWrapperB);
            dingMapper.update(thisUser,updateWrapperT);
            reply = String.format(BaseReplyConstant.BATTLE_BOOM_YOU,
                    battleName,
                    battleName,
                    String.format("%.3f",battleLength));
        }

        reply = "[CQ:at,qq="+thisUser.getId()+"]"+reply;

        if (battleLength >= 30) {
            dingMapper.deleteDing(groupId);
            bot.sendGroupMsg(groupId,reply,false);
            reply = String.format(BaseReplyConstant.BATTLE_WIN,
                    battleName,
                    battleLength);
        }
        if (thisLength >= 30) {
            dingMapper.deleteDing(groupId);
            bot.sendGroupMsg(groupId,reply,false);
            reply = String.format(BaseReplyConstant.BATTLE_WIN,
                    thisName,
                    thisName);
        }
        return reply;
    }
}
