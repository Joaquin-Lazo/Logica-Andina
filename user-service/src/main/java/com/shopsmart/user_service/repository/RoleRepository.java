package com.shopsmart.user_service.repository;

import com.shopsmart.user_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
    
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}