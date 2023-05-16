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
            case CMD_SETTINGS:
                updateMessageForSettings(message);
                break;
            case CMD_HELP:
                updateMessageForHelp(message);
                break;
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
        Optional<User> userOptional = userRepo.findByChatId(message.getChatId());
        if (userOptional.isEmpty()) {
            editMessageText.setText(SMTH_WRONG);
            return editMessageText;
        }
        User user = userOptional.get();
        String notificationName = getCustomNameFromCallbackData(data);
        if (data.contains("_ADD_")) {
            notificationRepo.findByName(notificationName).ifPresentOrElse(n -> {
                        subscriptionRepo.save(Subscription.builder()
                                .notification(n)
                                .user(user)
                                .build());
                        editMessageText.setText("Notification \"" + notificationName + "\" is added");
                    },
                    () -> editMessageText.setText(SMTH_WRONG));
        } else if (data.contains("_DELETE_")) {
            notificationRepo.findByName(notificationName).ifPresentOrElse(n -> {
                        var id = subscriptionRepo.getId(user.getId(), n.getId());
                        if (id.isPresent()) {
                            subscriptionRepo.deleteById(id.get());
                            editMessageText.setText("Subscription to \"" + notificationName + "\" is deleted");
                        } else editMessageText.setText(SMTH_WRONG);
                    },
                    () -> editMessageText.setText(SMTH_WRONG));
        }
        return editMessageText;
    }

    private String getCustomNameFromCallbackData(String data) {
        String sub1 = data.substring(data.indexOf("_") + 1);
        return sub1.substring(sub1.indexOf("_") + 1);
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

        Optional<User> user = userRepo.findByChatId(Long.valueOf(message.getChatId()));
        if (user.isEmpty()) {
            message.setText(SMTH_WRONG);
            return message;
        }

        List<Notification> notifications = notificationRepo.getForUser(user.get().getId());

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

    private SendMessage updateMessageForSettings(SendMessage message) {
        Map<String, String> mapForKeyboard = new HashMap<>();
        Optional<User> user = userRepo.findByChatId(Long.valueOf(message.getChatId()));
        if (user.isEmpty()) {
            message.setText(SMTH_WRONG);
            return message;
        }

        List<Notification> notifications = notificationRepo.getUserNotification(user.get().getId());
        if (notifications.isEmpty()) {
            message.setText(EmojiParser.parseToUnicode("No Available Notifications to delete" + ":confused:"));
            return message;
        }
        notifications.forEach(n -> {
                    String buttonName = n.getName();
                    mapForKeyboard.put("Delete: " + buttonName, "NOTIFICATION_DELETE_" + buttonName);
                }
        );
        message.setReplyMarkup(BotUtils.getKeyboard(mapForKeyboard));
        message.setText(EmojiParser.parseToUnicode("Available Notifications to delete, please make your choice" + ":blush:"));
        return message;
    }

    private SendMessage updateMessageForHelp(SendMessage message) {
        message.setText(EmojiParser.parseToUnicode("Available commands: \n"
                + String.join("\n", botConfig.getCommands()))
                + "\n\n Hint: Chose command \\/NOTIFICATION and take your service"+ ":blush:");
        return message;
    }
}
