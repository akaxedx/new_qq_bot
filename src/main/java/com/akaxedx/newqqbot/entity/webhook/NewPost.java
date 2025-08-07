package com.akaxedx.newqqbot.entity.webhook;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPost {
    String title;
    String slug;
    String permalink;
    String visible;
    String owner;
    String publishTime;

    public static NewPost obj2NewPost(Object o) {
        JSON parse = JSONUtil.parse(o);
        return parse.toBean(NewPost.class);
    }
}
