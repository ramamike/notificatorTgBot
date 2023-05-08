package com.springLearnig.telegramBot.telegram;

import com.springLearnig.telegramBot.telegram.config.BotConfig;
import com.springLearnig.telegramBot.telegram.model.IUserRepository;
import com.springLearnig.telegramBot.telegram.model.User;
import com.springLearnig.telegramBot.telegram.service.BotService;
import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
//   WebHookBot..hosting...ssl..server with static IP

    private BotService service;
    private final BotConfig botConfig;

    private IUserRepository userRepository;

    private final String HELP_TEXT = "Choose command from menu";
    private final String YES_BUTTON = "YES_BUTTON";
    private final String NO_BUTTON = "NO_BUTTON";

    public Bot(BotService service,BotConfig botConfig, IUserRepository userRepository) {
        this.service=service;
        this.botConfig = botConfig;
        this.userRepository = userRepository;

        // Set Owner
        userRepository.findFirstByOrderById().ifPresent(u -> botConfig.setOwnerId(u.getId()));

        // Execute menu
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new
                BotCommand("/start", "get a welcome message"));
        commandList.add(new
                BotCommand("/mydata", "get your data stored"));
        commandList.add(new
                BotCommand("/deletedata", "delete your data stored"));
        commandList.add(new
                BotCommand("/help", "how to use"));
        commandList.add(new
                BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
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

//        var notifications = notificationRepository.findAll();
//        var users = userRepository.findAll();
//
//        notifications.forEach(notification -> {
//            send(notification.getText(), users);
//        });
    }

    private void send(BotApiMethodMessage messageToSend){
        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error("Sending message error: ", e);
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            send(service.onUpdateReceivedMessage(update));
        }


//        } else if (update.hasCallbackQuery()) {
//            String callBackData = update.getCallbackQuery().getData();
//            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Long chatId = update.getCallbackQuery().getMessage().getChatId();
//
//            if (callBackData.equals(YES_BUTTON)) {
//                sendEditMessageText(chatId, messageId, "You pressed YES");
//            } else if (callBackData.equals(NO_BUTTON)) {
//                sendEditMessageText(chatId, messageId, "You pressed NO");
//            }
//        }
    }

    private void send(String messageText, Iterable<User> users) {
        String textToSend = EmojiParser.parseToUnicode(messageText);
        users.forEach(user -> {
            sendMessage(user.getId(), textToSend);
        });
    }

    private void register(Long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you want to register?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();

        var button_1 = new InlineKeyboardButton();
        button_1.setText("Yes");
        button_1.setCallbackData(YES_BUTTON);

        var button_2 = new InlineKeyboardButton();
        button_2.setText("No");
        button_2.setCallbackData(NO_BUTTON);

        keyboardButtons.add(button_1);
        keyboardButtons.add(button_2);

        keyboardRows.add(keyboardButtons);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Sending message error: ", e);
        }

    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = User.builder()
                    .id(chatId)
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("New user: " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ":blush:");
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageToSend)
                .replyMarkup(addKeyboardMarkup())
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Sending message error: ", e);
        }

    }

    private ReplyKeyboardMarkup addKeyboardMarkup() {

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow_1 = new KeyboardRow();
        keyboardRow_1.add("weather");
        keyboardRow_1.add("random joke");

        KeyboardRow keyboardRow_2 = new KeyboardRow();
        keyboardRow_2.add("register");
        keyboardRow_2.add("check my data");
        keyboardRow_2.add("delete my data");

        keyboardRows.add(keyboardRow_1);
        keyboardRows.add(keyboardRow_2);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();
    }

    private void sendEditMessageText(Long chatId, Integer messageId, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(text);
        editMessageText.setMessageId(messageId);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Sending message error: ", e);
        }
    }

}
