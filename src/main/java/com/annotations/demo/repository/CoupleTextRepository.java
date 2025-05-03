package com.annotations.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Dataset;

@Repository
public interface CoupleTextRepository extends JpaRepository<CoupleText, Long> {
    List<CoupleText> findByDataset(Dataset dataset);
} 