package com.annotations.demo.repository;

import java.util.List;
import java.util.Optional;

import com.annotations.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.CoupleText;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByAnnotateur(User user);
    Annotation findAnnotationById(Long id);
    List<Annotation> findByCoupleText(CoupleText coupleText);
    Optional<Annotation> findByAnnotateurIdAndCoupleTextId(Long annotateurId, Long coupleId);
    List<Annotation> findByCoupleText_Dataset_Id(Long datasetId);
    List<Annotation> findByAnnotateur_IdAndCoupleText_IdIn(Long annotateurId, List<Long> coupleIds);
    List<Annotation> findByAnnotateur_Taches_Id(Long taskId);
    Page<Annotation> findByAnnotateur(User user, Pageable pageable);
}