package com.annotations.demo.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class SpamDetectionResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "annotateur_id")
    private Annotateur annotateur;
    private double mismatchRate;
    private boolean flagged;
}
