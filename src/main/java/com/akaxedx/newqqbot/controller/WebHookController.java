package com.akaxedx.newqqbot.controller;

import com.akaxedx.newqqbot.entity.Rss;
import com.akaxedx.newqqbot.entity.webhook.NewComment;
import com.akaxedx.newqqbot.entity.webhook.WebHookDTO;
import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.entity.content.WebHookContent;
import com.akaxedx.newqqbot.entity.webhook.NewPost;
import com.akaxedx.newqqbot.mappers.RssMapper;
import com.akaxedx.newqqbot.tools.CozeWorkflowExecutor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class WebHookController {
    private static final Logger logger = LoggerFactory.getLogger(WebHookController.class);
    @Resource
    private RssMapper rssMapper;
    @Value("${blog.host}")
    private String host;
    @Value("${blog.http-port}")
    private String httpPort;
    @Value("${blog.blog-addr}")
    private String blogAddr;

    @PostMapping("/webhook")
    public void webhook(@RequestBody WebHookDTO request){

        String reply = "";

        if(request.getEventType().equals(WebHookContent.NEW_POST)) {
            // 新文章
            NewPost newPost = NewPost.obj2NewPost(request.getData());
            reply = String.format(WebHookContent.NEW_POST_REPLY, newPost.getOwner(), blogAddr+newPost.getPermalink());
            QueryWrapper<Rss> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().isNotNull(Rss::getGroupId);
            List<Rss> rsses = rssMapper.selectList(queryWrapper);
            logger.info(rsses.toString());
            for (Rss rss : rsses) {
                send_reply_group(reply, rss.getGroupId());
            }
        } else if (request.getEventType().equals(WebHookContent.NEW_COMMENT)){
            NewComment newComment = NewComment.obj2NewComment(request.getData());
            logger.info(newComment.toString());
            // 新评论
            reply = String.format(WebHookContent.NEW_COMMENT_REPLY,
                    newComment.getOwner().getName(),
                    newComment.getContent());
            send_reply_private(reply);
        }
    }

    private void send_reply_private(String reply) {
        try {
            String addr = host + ":" +httpPort;
            Map<String, Object> sjzParams = new HashMap<>();
            sjzParams.put("send_id", "1365957941");
            sjzParams.put("host", String.format(SendContent.SEND_TO_USER, addr));
            sjzParams.put("type", SendContent.NOTICE_TO_USER);
            sjzParams.put("extra", reply);
            String sjzResponse = CozeWorkflowExecutor.executeWorkflow(
                    SendContent.WORK_FLOW_ID,
                    sjzParams
            );
            logger.info("收到了");
        } catch (IOException e) {
            logger.info("执行失败");
        }
    }
    private void send_reply_group(String reply, String groupId) {
        try {
            String addr = host + ":" +httpPort;
            Map<String, Object> sjzParams = new HashMap<>();
            sjzParams.put("send_id", groupId);
            sjzParams.put("host", String.format(SendContent.SEND_TO_GROUP, addr));
            sjzParams.put("type", SendContent.NOTICE_TO_GROUP);
            sjzParams.put("extra", reply);
            String sjzResponse = CozeWorkflowExecutor.executeWorkflow(
                    SendContent.WORK_FLOW_ID,
                    sjzParams
            );
            logger.info("收到了");
        } catch (IOException e) {
            logger.info("执行失败");
        }
    }
}
