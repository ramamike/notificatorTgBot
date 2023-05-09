package com.springLearnig.telegramBot.notifications.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface INotificationRepository extends CrudRepository<Notification, Long> {

}
