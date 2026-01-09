package com.zetumall.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    
    List<Address> findByUserId(String userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);
}
