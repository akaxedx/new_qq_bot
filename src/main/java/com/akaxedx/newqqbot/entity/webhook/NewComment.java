package com.akaxedx.newqqbot.entity.webhook;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class NewComment {
    Owner owner;
    String content;
    String createTime;
    Boolean approved;
    String refKind;
    SinglePageData singlePageData;


    @Setter
    @Getter
    public static class Owner{
        String kind;
        String name;
        String displayName;
    }
    @Getter
    @Setter
    public static class SinglePageData {
        String title;
        String slug;
        String visible;
        String owner;
        String createTime;
        String publishTime;
    }


    public static NewComment obj2NewComment(Object o) {
        JSON parse = JSONUtil.parse(o);
        return parse.toBean(NewComment.class);
    }
}




