package com.springLearnig.telegramBot.telegram.model;


import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;

public interface IUserRepository extends CrudRepository<User, Long> {
    Optional<User> findFirstByOrderById();

    boolean existsByChatId(Long chatId);

    Optional<User> findByChatId(Long chatId);
}
