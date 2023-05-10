package com.springLearnig.telegramBot.subscriptions.model;

import com.springLearnig.telegramBot.notifications.model.Notification;
import com.springLearnig.telegramBot.telegram.model.User;
import lombok.*;

import javax.persistence.*;

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

    @ManyToOne(fetch = FetchType.EAGER)
    private Notification notification;

    private String setting;

}
