package com.springLearnig.telegramBot.subscriptions.model;

import com.springLearnig.telegramBot.notifications.model.Notification;
import com.springLearnig.telegramBot.telegram.model.User;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity(name="subscriptions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Notification notification;

    private String setting;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id)
                && Objects.equals(user.getId(), that.user.getId())
                && Objects.equals(notification.getId(), that.notification.getId())
                && Objects.equals(setting, that.setting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user.getId(), notification.getId(), setting);
    }
}
