package com.annotations.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annotations.demo.entity.Annotateur;

@Repository
public interface AnnotateurRepository extends JpaRepository<Annotateur, Long> {
    Annotateur findByLogin(String login);
} 