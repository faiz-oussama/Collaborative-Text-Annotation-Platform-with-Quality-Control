package com.annotations.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adminitrator")
@PrimaryKeyJoinColumn(name = "user_id")

//ici je vais utiliser des annotations de lombok plus specifique
//car @Data ne vas pas prendre en consideration la classe parente User
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor

public class Administrator extends User{
    @Id
    private Long id;
}
