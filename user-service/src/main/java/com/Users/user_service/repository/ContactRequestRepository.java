package com.Users.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Users.user_service.model.ContactRequest;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Integer>{
    
}
