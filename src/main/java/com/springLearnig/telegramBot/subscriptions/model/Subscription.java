package com.springLearnig.telegramBot.subscriptions.model;

import com.springLearnig.telegramBot.notifications.NotificationType;
import com.springLearnig.telegramBot.notifications.models.Notification;
import com.springLearnig.telegramBot.telegram.model.User;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity(name="subscriptions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Notification notification;

}
