package com.springLearnig.telegramBot.telegram;

import com.springLearnig.telegramBot.telegram.config.BotConfig;
import com.springLearnig.telegramBot.telegram.model.IUserRepository;
import com.springLearnig.telegramBot.telegram.service.BotService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
//   WebHookBot..hosting...ssl..server with static IP

    private BotService service;
    private final BotConfig botConfig;

    private IUserRepository userRepository;

    private final String HELP_TEXT = "Choose command from menu";

    public Bot(BotService service,BotConfig botConfig, IUserRepository userRepository) {
        this.service=service;
        this.botConfig = botConfig;
        this.userRepository = userRepository;

        // Set Owner
        userRepository.findFirstByOrderById().ifPresent(u -> botConfig.setOwnerId(u.getId()));

        try {
            this.execute(new SetMyCommands(botConfig.getCommandList(), new BotCommandScopeDefault(), null));
        } catch (
                TelegramApiException e) {
            log.error("Execute menu creation error :", e);
        }

    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    @Scheduled(cron = "${cron.scheduler}")
    private void sendNotifications() {
        userRepository.findAll().forEach(user -> {
            user.getSubscriptions().forEach(subscription->{
                SendMessage message = SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(subscription.getNotification().getText())
                        .build();
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Execution message error for user: "+ user.getFirstName() +
                            " of notification " + subscription.getNotification().getNotificationType());
                }
            });
        });

    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                execute(service.onUpdateReceivedMessage(update));
            } else if (update.hasCallbackQuery()) {
                execute(service.onUpdateReceivedCallBack(update));
            }
        } catch (TelegramApiException e) {
            log.error("Execution message error: ", e);
        }
    }
}
