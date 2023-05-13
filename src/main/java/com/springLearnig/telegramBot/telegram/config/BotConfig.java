package com.springLearnig.telegramBot.telegram.config;

import com.springLearnig.telegramBot.telegram.Constants;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;


@EnableScheduling
@Configuration
@PropertySource("application.properties")
@Getter
@Slf4j
public class BotConfig {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    private Long ownerId;

    private List<BotCommand> commandList = List.of(
            new BotCommand(Constants.CMD_HELP, "how to use"),
            new BotCommand(Constants.CMD_START, "get a welcome message, invite to registration"),
            new BotCommand(Constants.CMD_MY_DATA, "get your data stored"),
            new BotCommand(Constants.CMD_DELETE_DATA, "delete your data stored"),
            new BotCommand(Constants.CMD_SETTINGS, "set your preferences"),
            new BotCommand(Constants.CMD_NOTIFICATIONS, "chose notifications")
    );

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

}
