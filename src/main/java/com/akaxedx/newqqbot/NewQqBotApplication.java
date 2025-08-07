package com.akaxedx.newqqbot;


import com.akaxedx.newqqbot.entity.content.SendContent;
import com.akaxedx.newqqbot.tools.CozeWorkflowExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class NewQqBotApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(NewQqBotApplication.class, args);
    }

}
