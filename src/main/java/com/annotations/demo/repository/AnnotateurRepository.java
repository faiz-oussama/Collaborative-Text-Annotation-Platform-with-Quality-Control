package com.annotations.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annotations.demo.entity.Annotateur;

import java.util.List;

@Repository
public interface AnnotateurRepository extends JpaRepository<Annotateur, Long> {
    List<Annotateur> findAllByDeleted(boolean b);
    Page<Annotateur> findAllByDeletedFalse(Pageable pageable);
}