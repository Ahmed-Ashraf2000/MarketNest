package com.marketnest.ecommerce.service.user;

import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.exception.AddressNotFound;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.AddressRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserAddressMapper addressMapper;

    @Transactional
    public Address createAddress(String email, AddressRequestDto requestDTO) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User not found with email: " + email));

        Address address = addressMapper.toEntity(requestDTO);
        address.setUser(user);
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return addressRepository.findAddressByUser_Email(email);
    }

    @Transactional(readOnly = true)
    public Address getAddress(String email, Long addressId) {
        return addressRepository.findAddressByUser_EmailAndId(email, addressId)
                .orElseThrow(() -> new AddressNotFound(
                        "Address not found with id: " + addressId));
    }

    public Address updateAddress(String email, Long addressId, AddressRequestDto requestDTO) {
        Address existingAddress = getAddress(email, addressId);

        existingAddress.setAddressLine1(requestDTO.getAddressLine1());
        existingAddress.setAddressLine2(requestDTO.getAddressLine2());
        existingAddress.setCity(requestDTO.getCity());
        existingAddress.setStateProvince(requestDTO.getStateProvince());
        existingAddress.setPostalCode(requestDTO.getPostalCode());
        existingAddress.setCountryCode(requestDTO.getCountryCode());

        return addressRepository.save(existingAddress);
    }

    public void deleteAddress(String email, Long addressId) {
        Address address = getAddress(email, addressId);
        addressRepository.delete(address);
    }

    @Transactional
    public Address setDefaultAddress(String email, Long addressId) {
        Address address = getAddress(email, addressId);

        Optional<Address> currentDefaultOpt = addressRepository.findAddressByDefaultAddressIs(true);
        if (currentDefaultOpt.isPresent()) {
            Address currentDefaultAddress = currentDefaultOpt.get();
            currentDefaultAddress.setDefaultAddress(false);
            addressRepository.save(currentDefaultAddress);
        }

        address.setDefaultAddress(true);
        addressRepository.save(address);

        return address;
    }

}