package com.annotations.demo.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@ToString(exclude = {"tasks", "classesPossibles", "coupleTexts"})
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomDataset;
    private String description;

    //relation taches/dataset
    @OneToMany(mappedBy="dataset")
    private List<Task> tasks = new ArrayList<>();

    //relation classe/dataset
    @OneToMany(mappedBy="dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassPossible> classesPossibles = new HashSet<>();

    //relation coupleText/dataset
    @OneToMany(mappedBy="dataset")
    private Set<CoupleText> coupleTexts = new HashSet<>();
}
