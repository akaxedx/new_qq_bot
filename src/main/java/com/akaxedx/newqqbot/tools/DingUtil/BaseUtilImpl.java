package com.akaxedx.newqqbot.tools.DingUtil;

import com.akaxedx.newqqbot.entity.Ding;
import com.akaxedx.newqqbot.mappers.DingMapper;
import com.akaxedx.newqqbot.tools.BaseReplyConstant;
import com.akaxedx.newqqbot.tools.inter.BaseUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public String doThis(MessageEvent event) {
        String reply;
        switch (event.getMessage()){
            case DingConstant.DING_GET -> {
                reply = getDing(event);
            }
            case DingConstant.DING_CULTIVATE -> {
                reply = cultivate(event);
            }
            case DingConstant.DING_BATTLE -> {
                reply = "2";
            }
            default -> {
                return null;
            }
        }
        return reply;
    }

    private String getDing(MessageEvent event) {
        QueryWrapper<Ding> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Ding::getId, event.getUserId());
        List<Ding> dings = dingMapper.selectList(queryWrapper);
        String reply = BaseReplyConstant.DING_GET_NULL;
        Random r = new Random();
        Double length = r.nextDouble() * 2 + 3;


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
                    return String.format(reply,
                            gEvent.getSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                // 已有金箍棒
                reply = String.format(BaseReplyConstant.DING_GET_ONE,
                        gEvent.getSender().getNickname(),
                        String.format("%.3f", dings.get(0).getLength()));

            }
            default -> reply = null;

        }
        return reply;
    }
    private String cultivate(MessageEvent event) {
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
                //  没领取或清空
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return String.format(reply,
                            pEvent.getPrivateSender().getNickname());
                }
                // 领取了
                reply = cLength(dings.get(0),length,lucky);

            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return String.format(reply,
                            gEvent.getSender().getNickname());
                }
                // 领取了
                reply = cLength(dings.get(0),length,lucky);

            }
            default -> reply = null;

        }
        return reply;
    }

    private String cLength(Ding ding, Double length, Boolean lucky) {
        long now = Instant.now().getEpochSecond();
        Long lastest = ding.getTime();
        long middle = now - lastest;
        // 时间不够
        if (middle < 180) {
            return String.format(BaseReplyConstant.NEED_RELAX, 180-middle);
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
        return reply;
    }
}
