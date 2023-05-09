package com.springLearnig.telegramBot.notifications.models;

import com.springLearnig.telegramBot.notifications.NotificationType;
import com.springLearnig.telegramBot.subscriptions.model.Subscription;
import com.springLearnig.telegramBot.telegram.model.User;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name="notifications")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private String text;

    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "notification"
    )
    private List<Subscription> subscriptions = new ArrayList<>();
}
