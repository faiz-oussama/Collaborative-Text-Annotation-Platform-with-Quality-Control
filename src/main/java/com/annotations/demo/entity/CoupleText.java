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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"taches", "annotations"})
@NoArgsConstructor
@AllArgsConstructor

public class CoupleText {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String text_1;
    private String text_2;


    @ManyToMany(mappedBy="couples")
    private List<Task> taches = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;


    @OneToMany(mappedBy="coupleText")
    private List<Annotation> annotations = new ArrayList<>();
}
