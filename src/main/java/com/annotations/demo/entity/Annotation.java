package com.annotations.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@ToString(exclude = {"annotateur", "coupleText"})
@AllArgsConstructor
@NoArgsConstructor
public class Annotation {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_choisie", nullable = false)
    private String chosenClass;

    @ManyToOne
    @JoinColumn(name="annotateur_id")
    private Annotateur annotateur;

    @ManyToOne
    @JoinColumn(name="couple_id")
    private CoupleText coupleText;

    //helper
    public Dataset getDataset() {
        return coupleText != null ? coupleText.getDataset() : null;
    }
}
