package com.annotations.demo.repository;

import com.annotations.demo.entity.NliPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NliPredictionRepository extends JpaRepository<NliPrediction, Long> {
}
