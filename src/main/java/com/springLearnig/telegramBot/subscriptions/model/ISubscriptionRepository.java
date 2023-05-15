package com.springLearnig.telegramBot.subscriptions.model;

import com.springLearnig.telegramBot.notifications.model.Notification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface ISubscriptionRepository extends CrudRepository<Subscription, Long> {

    Set<Subscription> findByUserId(Long id);

    @Query("select s.id from subscriptions s where s.user.id=?1 and s.notification.id=?2")
    Optional<Long> getId(Long userId, Long notificationId);

    @Query("select s from subscriptions s where s.user.id=?1")
    List<Subscription> getAll(Long userId);

    @Query("select n from notifications n, subscriptions s " +
            "where s.user.id=?1 AND n.id=s.notification.id")
    List<Notification> getNotificationsOfUser(Long userId);

    @Query("select n from notifications n " +
            "LEFT OUTER JOIN subscriptions s " +
            "ON s.user.id=?1 AND n.id=s.notification.id " +
            "where s.user.id is null")
    List<Notification> getNotificationsForUser(Long userId);
}
