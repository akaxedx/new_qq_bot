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
                reply = "1";
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
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    Ding newDing = new Ding(event.getUserId(), length, 1);
                    dingMapper.insertOrUpdate(newDing);
                    return String.format(reply,
                            pEvent.getPrivateSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                reply = String.format(BaseReplyConstant.DING_GET_ONE,
                        pEvent.getPrivateSender().getNickname() ,
                        String.format("%.3f", dings.get(0).getLength()));
            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    Ding newDing = new Ding(event.getUserId(), length, 1);
                    dingMapper.insertOrUpdate(newDing);
                    return String.format(reply,
                            gEvent.getSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
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
        String reply = BaseReplyConstant.DING_GET_NULL;
        Random r = new Random();
        Double length = r.nextDouble() * 2 + 3;


        switch (event.getClass().getSimpleName()){
            case SenderType.PRIVATE -> {
                PrivateMessageEvent pEvent = (PrivateMessageEvent) event;
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    return String.format(reply,
                            pEvent.getPrivateSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                reply = String.format(BaseReplyConstant.DING_GET_ONE,
                        pEvent.getPrivateSender().getNickname() ,
                        String.format("%.3f", dings.get(0).getLength()));
            }
            case SenderType.Group -> {
                GroupMessageEvent gEvent = (GroupMessageEvent) event;
                if (dings.isEmpty() || dings.get(0).getHas().equals(DingConstant.NO_DING)) {
                    Ding newDing = new Ding(event.getUserId(), length, 1);
                    dingMapper.insertOrUpdate(newDing);
                    return String.format(reply,
                            gEvent.getSender().getNickname(),
                            String.format("%.3f",newDing.getLength()));
                }
                reply = String.format(BaseReplyConstant.DING_GET_ONE,
                        gEvent.getSender().getNickname(),
                        String.format("%.3f", dings.get(0).getLength()));

            }
            default -> reply = null;

        }
        return reply;
    }
}
