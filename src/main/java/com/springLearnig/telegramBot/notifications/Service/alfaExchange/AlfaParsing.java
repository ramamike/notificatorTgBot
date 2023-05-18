package com.springLearnig.telegramBot.notifications.Service.alfaExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springLearnig.telegramBot.notifications.NotificationStatus;
import com.springLearnig.telegramBot.notifications.Notifications;
import com.springLearnig.telegramBot.notifications.model.INotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Service
@EnableScheduling()
public class AlfaParsing {

    private INotificationRepository notificationRepo;
    private final String customURL = "https://select.by/kurs/";

    public AlfaParsing(INotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static double LOW_BOARDER = 1.0;
    private final static double UP_BOARDER = 5.0;


        @Scheduled(cron = "${cron.scheduler.parsing}")
//    @Scheduled(fixedDelay = 30000)
    public void parsing() throws IOException {
        Connection connection = Jsoup.connect(customURL);
        Document document = connection.get();
//        Document document = Jsoup.parse(new File("/home/mihal/progr/javaProjects/_htmlForParse/alfa.html"), "UTF-8", "");
        Elements currency = document.select("table[class~=table.table-hover.table-sm.courses-main.*]");
        Elements table = currency.select("tbody");
        Elements rows = currency.select("tr");
        if (rows.isEmpty()) {
            log.error(customURL + " parsing issue, there is no result for rows selection");
            return;
        }
        Elements columns = rows.get(2).select("td");
        if (columns.isEmpty()) {
            log.error(customURL + " parsing issue, there is no result for columns selection");
            return;
        }
        String purchase = columns.get(1).text().replace(",", ".");
        String selling = columns.get(2).text().replace(",", ".");

        Double purchaseValue = Double.valueOf(purchase);
        Double sellingValue = Double.valueOf(selling);

        if (purchaseValue.compareTo(LOW_BOARDER) < 0
                && purchaseValue.compareTo(UP_BOARDER) > 0
                && sellingValue.compareTo(LOW_BOARDER) < 0
                && sellingValue.compareTo(UP_BOARDER) > 0) {
            log.error(customURL + " parsing issue, out of range [" + LOW_BOARDER + "," + UP_BOARDER + "]");
            return;
        }

        AlfaEntity newData = AlfaEntity.builder()
                .purchase(purchaseValue)
                .selling(sellingValue)
                .build();

        notificationRepo.findByName(Notifications.ALFA_EXCH.toString())
                .ifPresentOrElse(n -> {
                            try {
                                AlfaEntity data = objectMapper.readValue(n.getData(), AlfaEntity.class);
                                if (!newData.getPurchase().equals(data.getPurchase()) || !newData.getSelling().equals(data.getSelling())) {
                                    data.setMarkPct(getPercents(data.getMark(), newData.getSelling()));
                                    data.setPurchasePct(getPercents(data.getPurchase(), newData.getPurchase()));
                                    data.setPurchase(newData.getPurchase());
                                    data.setSellingPct(getPercents(data.getSelling(), newData.getSelling()));
                                    data.setSelling(newData.getSelling());
                                    String jsonData = objectMapper.writeValueAsString(data);
                                    n.setStatus(NotificationStatus.NEW);
                                    n.setData(jsonData);
                                    n.setText(data.getText());
                                    notificationRepo.save(n);
                                }
                            } catch (JsonProcessingException e) {
                                log.error("JSON deserialization to Entity error: " + e);
                            }
                        },
                        () -> log.error("Notification is not found"));

    }

    private Double getPercents(Double oldValue, Double newValue) {
        Double newPurchase = (Math.abs(oldValue - newValue) / Math.max(newValue, 1.0)) * 100;
        return ((double) (Math.round(newPurchase * 1000)) / 1000);
    }
}
