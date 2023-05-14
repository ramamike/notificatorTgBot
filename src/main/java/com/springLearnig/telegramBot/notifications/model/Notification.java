package com.springLearnig.telegramBot.notifications.model;

import com.springLearnig.telegramBot.notifications.NotificationStatus;
import com.springLearnig.telegramBot.subscriptions.model.Subscription;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Subscription> subscriptions = new HashSet<>();

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", status=" + status +
                ", subscriptions=" + (subscriptions==null? "0":  subscriptions.size()) +
                '}';
    }
}
