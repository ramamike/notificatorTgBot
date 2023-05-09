package com.springLearnig.telegramBot.telegram.model;

import com.springLearnig.telegramBot.subscriptions.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name="usersData")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private Timestamp timestamp;

    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    private List<Subscription> subscriptions = new ArrayList<>();

}
