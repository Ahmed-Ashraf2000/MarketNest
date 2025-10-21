package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAddressByUser_Email(String userEmail);

    Optional<Address> findAddressByUser_EmailAndId(String userEmail, Long id);

    Optional<Address> findAddressByDefaultAddressIs(boolean isDefault);

    Optional<Address> findAddressByUser_UserIdAndId(Long id, Long userId);
}
