package com.springLearnig.telegramBot.subscriptions.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ISubscriptionRepository extends CrudRepository<Subscription, Long> {

}
