package com.example.comus.domain.user.entity;

import com.example.comus.domain.answer.entity.Answer;
import com.example.comus.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "user")
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageUrl;

    private LocalTime todayChatTime;

    private LocalTime totalChatTime;

    private int totalChatCount;

    @OneToMany(mappedBy = "user")
    private List<Answer> answers;

}