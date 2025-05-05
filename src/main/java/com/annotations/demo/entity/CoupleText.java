package com.annotations.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "text_1", "text_2"})
@ToString(exclude = {"taches", "annotations"})
@NoArgsConstructor
@AllArgsConstructor

public class CoupleText {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_1", columnDefinition = "LONGTEXT")
    private String text_1;
    @Column(name = "text_2", columnDefinition = "LONGTEXT")
    private String text_2;


    @ManyToMany(mappedBy="couples")
    private List<Task> taches = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;


    @OneToMany(mappedBy="coupleText")
    private List<Annotation> annotations = new ArrayList<>();
}
