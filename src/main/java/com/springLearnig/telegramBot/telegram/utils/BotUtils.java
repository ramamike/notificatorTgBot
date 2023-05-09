package com.springLearnig.telegramBot.telegram.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BotUtils {
    public static InlineKeyboardMarkup getKeyboard(Map<String, String> buttonNameAndCallback) {
        List<InlineKeyboardButton> keyboardButtons =
                buttonNameAndCallback.keySet().stream()
                        .map(btn ->
                                InlineKeyboardButton.builder().text(btn).callbackData(buttonNameAndCallback.get(btn)).build())
                        .collect(Collectors.toList());
        return new InlineKeyboardMarkup(List.of(keyboardButtons));
    }
}
