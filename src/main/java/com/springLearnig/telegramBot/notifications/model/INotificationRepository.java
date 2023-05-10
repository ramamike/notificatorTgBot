package com.springLearnig.telegramBot.notifications.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface INotificationRepository extends CrudRepository<Notification, Long> {

    Optional<Notification> findByName(String name);
}
