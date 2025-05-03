package com.annotations.demo.service;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.repository.AnnotateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnnotateurServiceImpl implements AnnotateurService {

    private final AnnotateurRepository repo;

    public AnnotateurServiceImpl(AnnotateurRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Annotateur> findAll() {
        return repo.findAll();
    }

    @Override
    public Annotateur findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotateur not found"));
    }

    @Override
    public Annotateur save(Annotateur annotateur) {
        return repo.save(annotateur);
    }

    @Override
    public void deleteLogically(Long id) {
        Annotateur a = findById(id);
        a.setDeleted(true);
        repo.save(a);
    }
}
