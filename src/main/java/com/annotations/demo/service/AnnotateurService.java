package com.annotations.demo.service;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.repository.AnnotateurRepository;

import java.util.List;

public interface AnnotateurService {
    List<Annotateur> findAll();
    Annotateur findById(Long id);
    Annotateur save(Annotateur annotateur);
    void deleteLogically(Long id);
}
