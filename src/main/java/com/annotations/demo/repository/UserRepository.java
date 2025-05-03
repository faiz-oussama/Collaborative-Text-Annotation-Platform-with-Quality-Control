package com.annotations.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annotations.demo.entity.RoleType;
import com.annotations.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByLogin(String login);
    User findByNom(String nom);
    boolean hasRole(RoleType roletype);
    List<User> findByRole(RoleType role);
    boolean existsByLogin(String login);
}
 