package com.springLearnig.telegramBot.telegram.service;

import com.springLearnig.telegramBot.telegram.config.BotConfig;
import com.springLearnig.telegramBot.telegram.model.IUserRepository;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class BotService {

    private final String HELP_TEXT = "Choose command from menu";
    private final String YES_BUTTON = "YES_BUTTON";
    private final String NO_BUTTON = "NO_BUTTON";

    private final BotConfig botConfig;

    private IUserRepository userRepository;

    public BotService(BotConfig botConfig, IUserRepository userRepository) {
        this.botConfig = botConfig;
        this.userRepository = userRepository;
    }

    public SendMessage onUpdateReceivedMessage(Update update) {

        String firstName = update.getMessage().getChat().getFirstName();
        String name = (firstName == null) ? "" : firstName;
        String messageText = update.getMessage().getText();
        String messageCommand = messageText;
        String textInMessage = messageText;
        if (messageText.contains(" ")) {
            messageCommand = messageText.substring(0, messageText.indexOf(" "));
            textInMessage = messageText.substring(messageText.indexOf(" "));
        }
        long chatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        switch (messageCommand) {
            case "/start":
                message.setText(EmojiParser.parseToUnicode("Hi, " + name + ":blush:"));
                break;
//                case "/help":
//                    startCommandReceived(chatId, HELP_TEXT);
//                    break;
//                case "/register":
//                    register(chatId);
//                    break;
//                case "/send":
//                    send(textInMessage, userRepository.findAll());
//                    break;
            default:
                message.setText(EmojiParser.parseToUnicode("Sorry, command doesn't recognised" + ":wink:"));
        }

        return message;
    }

}
