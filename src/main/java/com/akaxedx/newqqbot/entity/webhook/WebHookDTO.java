package com.akaxedx.newqqbot.entity.webhook;

import lombok.Data;

@Data
public class WebHookDTO {
    String eventType;
    String eventTypeName;
    String hookTime;
    Object data;
}
