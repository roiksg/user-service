package com.example.user_service.repository;

import com.example.user_service.entity.Users;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsersRepository extends JpaRepository<Users, UUID>, JpaSpecificationExecutor<Users> {

    Page<Users> findAll(Specification<Users> spec, Pageable pageable);
}
