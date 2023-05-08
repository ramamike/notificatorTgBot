package com.springLearnig.telegramBot.telegram.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@Configuration
@PropertySource("application.properties")
@Data
@Slf4j
public class BotConfig {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    private Long ownerId;

}
