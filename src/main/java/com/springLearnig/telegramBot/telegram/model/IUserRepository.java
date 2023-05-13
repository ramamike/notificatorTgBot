package com.springLearnig.telegramBot.telegram.model;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IUserRepository extends CrudRepository<User, Long> {
    Optional<User> findFirstByOrderById();

    boolean existsByChatId(Long chatId);

    Optional<User> findByChatId(Long chatId);

}
