package com.annotations.demo.repository;

import com.annotations.demo.entity.SpamDetectionResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpamDetectionResultsRepository extends JpaRepository<SpamDetectionResults, Long> {
    List<SpamDetectionResults> findByAnnotateurId(Long annotateurId);
}
