package org.example.smarthealthmanagmentsystem.Repository;

import java.util.Optional;

import org.example.smarthealthmanagmentsystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
