package com.vu.springapi.repository;

import com.vu.springapi.model.InvalidatedToken;
import com.vu.springapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
