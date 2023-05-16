package com.springLearnig.telegramBot.notifications.Service.alfaExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springLearnig.telegramBot.notifications.NotificationStatus;
import com.springLearnig.telegramBot.notifications.Notifications;
import com.springLearnig.telegramBot.notifications.model.INotificationRepository;
import com.springLearnig.telegramBot.notifications.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@EnableScheduling()
public class Parsing {

    private INotificationRepository notificationRepo;

    public Parsing(INotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    private final String URL = "https://www.alfabank.by/";

    private final ObjectMapper objectMapper = new ObjectMapper();

    //    @Scheduled(cron = "${cron.scheduler}")
    @Scheduled(fixedDelay = 10)
    public void parsingByTag() throws IOException {
//        Document document = Jsoup.connect(URL).get();
        Document document = Jsoup.parse(new File("/home/mihal/progr/javaProjects/_htmlForParse/alfa.html"), "UTF-8", "");
        Elements currency = document.select("div[class~=v-data-table.currency-table.*]");
        Elements rows = currency.select("tr");

        Elements columns = rows.get(1).select("td");
        String purchase = columns.get(2).text();
        String selling = columns.get(3).text();

        Double purchaseValue = Double.valueOf(purchase);
        Double sellingValue = Double.valueOf(selling);

        Entity newData = Entity.builder()
                .purchase(purchaseValue)
                .selling(sellingValue)
                .build();

        notificationRepo.findByName(Notifications.ALFA_EXCH.toString())
                .ifPresentOrElse(n -> {
                            try {
                                Entity data = objectMapper.readValue(n.getData(), Entity.class);
                                if (!newData.getPurchase().equals(data.getPurchase()) || !newData.getSelling().equals(data.getSelling())) {
                                    data.setPurchase(newData.getPurchase());
                                    data.setPurchasePct(data.getPurchase() / Math.max(newData.getPurchase(), 1.0) * 100);
                                    data.setSelling(newData.getSelling());
                                    data.setSellingPct(data.getSelling() / Math.max(newData.getSelling(), 1.0) * 100);
                                    data.setMarkPct(data.getMark() / Math.max(newData.getSelling(), 1.0) * 100);
                                    String jsonData = objectMapper.writeValueAsString(data);
                                    notificationRepo.save(Notification.builder()
                                            .name(Notifications.ALFA_EXCH.toString())
                                            .status(NotificationStatus.NEW)
                                            .data(jsonData)
                                            .text(n.getText())
                                            .build());
                                }
                            } catch (JsonProcessingException e) {
                                log.error("JSON diserelisation to Entuty error: " + e);
                            }
                        },
                        () -> log.error("Notification is not found"));

    }
}
