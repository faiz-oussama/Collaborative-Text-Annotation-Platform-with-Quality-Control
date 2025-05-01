package com.annotations.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CoupleText {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String text_1;
    private String text_2;


    @ManyToMany(mappedBy="couple_id")
    private List<Task> taches = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;


    @OneToMany(mappedBy="annotation_id")
    private List<Annotation> annotations = new ArrayList<>();
}
