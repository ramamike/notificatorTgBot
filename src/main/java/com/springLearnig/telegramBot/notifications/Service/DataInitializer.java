package com.springLearnig.telegramBot.notifications.Service;

import com.springLearnig.telegramBot.notifications.Service.alfaExchange.AlfaInitializer;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {
    private final AlfaInitializer alfaExchangeInitializer;

    public DataInitializer(AlfaInitializer alfaExchangeInitializer) {
        this.alfaExchangeInitializer = alfaExchangeInitializer;
        alfaExchangeInitializer.run();
    }
}
