package com.springLearnig.telegramBot.notifications.Service;

import com.springLearnig.telegramBot.notifications.Service.alfaExchange.AlfaExchangeInitializer;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {
    private final AlfaExchangeInitializer alfaExchangeInitializer;

    public DataInitializer(AlfaExchangeInitializer alfaExchangeInitializer) {
        this.alfaExchangeInitializer = alfaExchangeInitializer;
        alfaExchangeInitializer.run();
    }
}
