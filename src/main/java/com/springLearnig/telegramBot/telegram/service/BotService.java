package com.springLearnig.telegramBot.telegram.service;

import com.springLearnig.telegramBot.notifications.model.INotificationRepository;
import com.springLearnig.telegramBot.notifications.model.Notification;
import com.springLearnig.telegramBot.subscriptions.model.ISubscriptionRepository;
import com.springLearnig.telegramBot.subscriptions.model.Subscription;
import com.springLearnig.telegramBot.telegram.config.BotConfig;
import com.springLearnig.telegramBot.telegram.model.IUserRepository;
import com.springLearnig.telegramBot.telegram.model.User;
import com.springLearnig.telegramBot.telegram.utils.BotUtils;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.sql.Timestamp;
import java.util.*;

import static com.springLearnig.telegramBot.telegram.Constants.*;

@Service
@Slf4j
public class BotService {

    private final String HELP_TEXT = "Choose command from menu";

    private final String SMTH_WRONG = EmojiParser.parseToUnicode("Something went wrong" + ":thinking:");
    private final BotConfig botConfig;

    private IUserRepository userRepo;
    private ISubscriptionRepository subscriptionRepo;
    private INotificationRepository notificationRepo;

    public BotService(BotConfig botConfig,
                      IUserRepository userRepository,
                      ISubscriptionRepository subscriptionRepository,
                      INotificationRepository notificationRepository) {
        this.botConfig = botConfig;
        this.userRepo = userRepository;
        this.subscriptionRepo = subscriptionRepository;
        this.notificationRepo = notificationRepository;
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
            case CMD_START:
                String text = EmojiParser.parseToUnicode("Hi, " + name + ":blush:");
                if (!userRepo.existsByChatId(chatId)) {
                    text = text + "\nDo you want to register?";
                    message.setReplyMarkup(getKeyboardYesNo("REGISTRATION_SET_YES", "REGISTRATION_SET_NO"));
                }
                message.setText(text);
                break;
            case CMD_NOTIFICATIONS:
                updateMessageForNotifications(message);
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


    public EditMessageText onUpdateReceivedCallBack(Update update) {

        CallbackQuery callBack = update.getCallbackQuery();
        String data = callBack.getData();
        Integer messageId = callBack.getMessage().getMessageId();
        Long chatId = callBack.getMessage().getChatId();

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);

        if (data.contains("REGISTRATION_")) {
            updateEditMessageForRegistration(callBack, editMessageText);
        } else if (data.contains("NOTIFICATION_")) {
            updateEditMessageForNotification(callBack, editMessageText);
        }

        return editMessageText;
    }

    private EditMessageText updateEditMessageForNotification(CallbackQuery callBack, EditMessageText editMessageText) {
        String data = callBack.getData();
        Message message = callBack.getMessage();
        var userOptional = userRepo.findByChatId(message.getChatId());
        if (userOptional.isEmpty()) {
            editMessageText.setText(SMTH_WRONG);
            return editMessageText;
        }
        User user = userOptional.get();
        if (data.contains("_ADD_")) {
            String notificationNamePrep = data.substring(data.indexOf("_")+1);
            String notificationName = notificationNamePrep.substring(notificationNamePrep.indexOf("_")+1);
            var notification = notificationRepo.findByName(notificationName);
            if(notification.isEmpty()) {
                editMessageText.setText(SMTH_WRONG);
            }
            Subscription subscription = Subscription.builder()
                    .notification(notification.get())
                    .user(user)
                    .build();
            subscriptionRepo.save(subscription);
        }
        return editMessageText;
    }

    private EditMessageText updateEditMessageForRegistration(CallbackQuery callBack, EditMessageText editMessageText) {
        String data = callBack.getData();
        Message message = callBack.getMessage();
        if (data.contains("_SET_")) {
            if (data.contains("_YES")) {
                registerUser(message);
                editMessageText.setText("You are registered as " + message.getChat().getFirstName());
            } else {
                editMessageText.setText(EmojiParser.parseToUnicode("Sorry, there is no service without registration" + ":thinking:"));
            }
        }
        return editMessageText;
    }

    private InlineKeyboardMarkup getKeyboardYesNo(String callBackForYes, String callBackForNo) {
        return BotUtils.getKeyboard(new LinkedHashMap<>() {{
            put("YES", callBackForYes);
            put("NO", callBackForNo);
        }});
    }

    private void registerUser(Message message) {
        if (!userRepo.existsByChatId(message.getChatId())) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = User.builder()
                    .chatId(chatId)
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepo.save(user);
            log.info("New user: " + user);
        }
    }

    private SendMessage updateMessageForNotifications(SendMessage message) {
        Map<String, String> mapForKeyboard = new HashMap<>();
        var notifications = notificationRepo.findAll();
        if (!notifications.iterator().hasNext()) {
            message.setText(EmojiParser.parseToUnicode("Sorry, no Available Notifications" + ":confused:"));
            return message;
        }
        notifications.forEach(notification -> {
                    String buttonName = notification.getName();
                    mapForKeyboard.put(buttonName, "NOTIFICATION_ADD_" + buttonName);
                }
        );
        message.setReplyMarkup(BotUtils.getKeyboard(mapForKeyboard));
        message.setText(EmojiParser.parseToUnicode("Available Notifications, please make your choice" + ":blush:"));
        return message;
    }

}
