package com.springLearnig.telegramBot.notifications.model;

import com.springLearnig.telegramBot.notifications.NotificationStatus;
import com.springLearnig.telegramBot.subscriptions.model.Subscription;
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

    private String name;

    private String text;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "notification"
    )
    private List<Subscription> subscriptions = new ArrayList<>();
}
