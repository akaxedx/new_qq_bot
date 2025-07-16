package com.akaxedx.newqqbot.tools.inter;

import com.akaxedx.newqqbot.mappers.DingMapper;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public interface BaseUtil {

    String doThis(MessageEvent event);

}
