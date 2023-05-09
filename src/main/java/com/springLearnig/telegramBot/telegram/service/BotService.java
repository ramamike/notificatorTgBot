package com.springLearnig.telegramBot.telegram.service;

import com.springLearnig.telegramBot.subscriptions.model.ISubscriptionRepository;
import com.springLearnig.telegramBot.telegram.config.BotConfig;
import com.springLearnig.telegramBot.telegram.model.IUserRepository;
import com.springLearnig.telegramBot.telegram.model.User;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.springLearnig.telegramBot.telegram.Constants.CMD_START;
import static com.springLearnig.telegramBot.telegram.Constants.BTN_YES_REGISTER;
import static com.springLearnig.telegramBot.telegram.Constants.BTN_NO_REGISTER;

@Service
@Slf4j
public class BotService {

    private final String HELP_TEXT = "Choose command from menu";
    private final BotConfig botConfig;

    private IUserRepository userRepository;
    private ISubscriptionRepository subscriptionRepository;

    public BotService(BotConfig botConfig, IUserRepository userRepository, ISubscriptionRepository subscriptionRepository) {
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
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
                if (!userRepository.existsByChatId(chatId)) {
                    text = text + "\nDo you want to register?";
                    message.setReplyMarkup(getKeyboardYesNo(BTN_YES_REGISTER, BTN_NO_REGISTER));
                }
                message.setText(text);
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

        String callBackData = update.getCallbackQuery().getData();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Message message = update.getCallbackQuery().getMessage();

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);

        switch (callBackData) {
            case BTN_YES_REGISTER:
                registerUser(message);
                editMessageText.setText("You are registered as " + message.getChat().getFirstName());
                break;
            case BTN_NO_REGISTER:
                editMessageText.setText(EmojiParser.parseToUnicode("Sorry, there is no service without registration" + ":thinking:"));
                break;
            default:
                editMessageText.setText(EmojiParser.parseToUnicode("Sorry, callback doesn't recognised, try again" + ":no_mouth:"));
        }
        return editMessageText;
    }


    private InlineKeyboardMarkup getKeyboardYesNo(String callBackForYes, String callBackForNo) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();

        var button_Yes = new InlineKeyboardButton();
        button_Yes.setText("Yes");
        button_Yes.setCallbackData(callBackForYes);

        var button_No = new InlineKeyboardButton();
        button_No.setText("No");
        button_No.setCallbackData(callBackForNo);

        keyboardButtons.add(button_Yes);
        keyboardButtons.add(button_No);

        keyboardRows.add(keyboardButtons);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);

        return inlineKeyboardMarkup;
    }

    private void registerUser(Message message) {
        if (!userRepository.existsByChatId(message.getChatId())) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = User.builder()
                    .chatId(chatId)
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("New user: " + user);
        }
    }

}
