package com.marketnest.ecommerce.service.user;

import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.exception.AddressNotFound;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.AddressRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address testAddress;
    private AddressRequestDto addressRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setAddressLine1("123 Main St");
        testAddress.setCity("Cairo");
        testAddress.setStateProvince("CA");
        testAddress.setPostalCode("10001");
        testAddress.setCountryCode("EG");
        testAddress.setUser(testUser);
        testAddress.setDefaultAddress(false);

        addressRequestDto = new AddressRequestDto();
        addressRequestDto.setAddressLine1("123 Main St");
        addressRequestDto.setCity("Cairo");
        addressRequestDto.setStateProvince("CA");
        addressRequestDto.setPostalCode("10001");
        addressRequestDto.setCountryCode("EG");
    }

    @Test
    void createAddress_shouldCreateNewAddress_whenValidData() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressMapper.toEntity(addressRequestDto)).thenReturn(testAddress);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        Address result = addressService.createAddress("test@example.com", addressRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAddressLine1()).isEqualTo("123 Main St");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> addressService.createAddress("unknown@example.com", addressRequestDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: unknown@example.com");

        verify(addressRepository, never()).save(any());
    }

    @Test
    void getUserAddresses_shouldReturnAddressList_whenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findAddressByUser_Email("test@example.com"))
                .thenReturn(Collections.singletonList(testAddress));

        List<Address> result = addressService.getUserAddresses("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAddressLine1()).isEqualTo("123 Main St");
        verify(addressRepository).findAddressByUser_Email("test@example.com");
    }

    @Test
    void getUserAddresses_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getUserAddresses("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: unknown@example.com");

        verify(addressRepository, never()).findAddressByUser_Email(anyString());
    }

    @Test
    void getAddress_shouldReturnAddress_whenExists() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 1L))
                .thenReturn(Optional.of(testAddress));

        Address result = addressService.getAddress("test@example.com", 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAddressLine1()).isEqualTo("123 Main St");
    }

    @Test
    void getAddress_shouldThrowException_whenNotFound() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress("test@example.com", 999L))
                .isInstanceOf(AddressNotFound.class)
                .hasMessageContaining("Address not found with id: 999");
    }

    @Test
    void updateAddress_shouldUpdateExistingAddress_whenValidData() {
        AddressRequestDto updateDto = new AddressRequestDto();
        updateDto.setAddressLine1("456 Oak Ave");
        updateDto.setCity("Giza");
        updateDto.setStateProvince("CA");
        updateDto.setPostalCode("90001");
        updateDto.setCountryCode("EG");

        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 1L))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        Address result = addressService.updateAddress("test@example.com", 1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getAddressLine1()).isEqualTo("456 GM");
        assertThat(result.getCity()).isEqualTo("Giza");
        assertThat(result.getStateProvince()).isEqualTo("CA");
        verify(addressRepository).save(testAddress);
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> addressService.updateAddress("test@example.com", 999L, addressRequestDto))
                .isInstanceOf(AddressNotFound.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    void deleteAddress_shouldDeleteAddress_whenExists() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 1L))
                .thenReturn(Optional.of(testAddress));

        addressService.deleteAddress("test@example.com", 1L);

        verify(addressRepository).delete(testAddress);
    }

    @Test
    void deleteAddress_shouldThrowException_whenNotFound() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress("test@example.com", 999L))
                .isInstanceOf(AddressNotFound.class);

        verify(addressRepository, never()).delete(any());
    }

    @Test
    void setDefaultAddress_shouldSetNewDefault_whenNoCurrentDefault() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 1L))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.findAddressByDefaultAddressIs(true)).thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        Address result = addressService.setDefaultAddress("test@example.com", 1L);

        assertThat(result).isNotNull();
        assertThat(result.isDefaultAddress()).isTrue();
        verify(addressRepository).save(testAddress);
    }

    @Test
    void setDefaultAddress_shouldReplaceExistingDefault_whenCurrentDefaultExists() {
        Address currentDefault = new Address();
        currentDefault.setId(2L);
        currentDefault.setDefaultAddress(true);
        currentDefault.setUser(testUser);

        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 1L))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.findAddressByDefaultAddressIs(true))
                .thenReturn(Optional.of(currentDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        Address result = addressService.setDefaultAddress("test@example.com", 1L);

        assertThat(result).isNotNull();
        assertThat(result.isDefaultAddress()).isTrue();
        assertThat(currentDefault.isDefaultAddress()).isFalse();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void setDefaultAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findAddressByUser_EmailAndId("test@example.com", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.setDefaultAddress("test@example.com", 999L))
                .isInstanceOf(AddressNotFound.class);

        verify(addressRepository, never()).save(any());
    }
}