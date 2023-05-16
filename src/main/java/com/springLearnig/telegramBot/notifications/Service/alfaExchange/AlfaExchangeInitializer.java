package com.springLearnig.telegramBot.notifications.Service.alfaExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springLearnig.telegramBot.notifications.NotificationStatus;
import com.springLearnig.telegramBot.notifications.Notifications;
import com.springLearnig.telegramBot.notifications.model.INotificationRepository;
import com.springLearnig.telegramBot.notifications.model.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
@Slf4j
public class AlfaExchangeInitializer implements Runnable {

    private final INotificationRepository repo;

    @Override
    @Transactional
    public void run() {

        ObjectMapper objectMapper = new ObjectMapper();

        if (repo.findByName(Notifications.ALFA_EXCH.toString()).isEmpty()) {
            Entity entity = new Entity(0.0, 1.0, 0.0, 1.0, 0.0, 1.0);
            try {
                String jsonData = objectMapper.writeValueAsString(entity);
                repo.save(Notification.builder()
                        .name(Notifications.ALFA_EXCH.toString())
                        .status(NotificationStatus.FINISHED)
                        .data(jsonData)
                        .text(entity.getText())
                        .build());

            } catch (JsonProcessingException e) {
                log.error("JSON serialization error " + e);
            }

        }
    }
}