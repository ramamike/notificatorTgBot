package com.springLearnig.telegramBot.notifications.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface INotificationRepository extends CrudRepository<Notification, Long> {

    Optional<Notification> findByName(String name);

    @Query("select n from notifications n, subscriptions s " +
            "where s.user.id=?1 AND n.id=s.notification.id")
    List<Notification> getUserNotification(Long userId);

    @Query("select n from notifications n " +
            "LEFT OUTER JOIN subscriptions s " +
            "ON s.user.id=?1 AND n.id=s.notification.id " +
            "where s.user.id is null")
    List<Notification> getForUser(Long userId);
}
