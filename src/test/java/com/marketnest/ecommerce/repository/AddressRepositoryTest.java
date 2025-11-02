package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AddressRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    private User testUser;
    private Address testAddress;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");
        testUser.setRole(User.Role.CUSTOMER);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        testAddress = new Address();
        testAddress.setAddressLine1("123 Main St");
        testAddress.setCity("Cairo");
        testAddress.setStateProvince("CA");
        testAddress.setPostalCode("10001");
        testAddress.setCountryCode("EG");
        testAddress.setUser(testUser);
        testAddress.setDefaultAddress(false);
    }

    @Test
    void findAddressByUser_Email_shouldReturnAddresses_whenUserHasAddresses() {
        addressRepository.save(testAddress);

        Address secondAddress = new Address();
        secondAddress.setAddressLine1("456  ST");
        secondAddress.setCity("Giza");
        secondAddress.setStateProvince("CA");
        secondAddress.setPostalCode("90001");
        secondAddress.setCountryCode("EG");
        secondAddress.setUser(testUser);
        addressRepository.save(secondAddress);

        List<Address> addresses = addressRepository.findAddressByUser_Email("test@example.com");

        assertThat(addresses).hasSize(2);
        assertThat(addresses).extracting(Address::getAddressLine1)
                .containsExactlyInAnyOrder("123 Main St", "456 ST");
    }

    @Test
    void findAddressByUser_Email_shouldReturnEmpty_whenUserHasNoAddresses() {
        List<Address> addresses = addressRepository.findAddressByUser_Email("test@example.com");

        assertThat(addresses).isEmpty();
    }

    @Test
    void findAddressByUser_EmailAndId_shouldReturnAddress_whenExists() {
        Address savedAddress = addressRepository.save(testAddress);

        Optional<Address> found = addressRepository.findAddressByUser_EmailAndId(
                "test@example.com", savedAddress.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAddressLine1()).isEqualTo("123 Main St");
    }

    @Test
    void findAddressByUser_EmailAndId_shouldReturnEmpty_whenNotExists() {
        Optional<Address> found = addressRepository.findAddressByUser_EmailAndId(
                "test@example.com", 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAddressByDefaultAddressIs_shouldReturnDefaultAddress_whenExists() {
        testAddress.setDefaultAddress(true);
        addressRepository.save(testAddress);

        Optional<Address> found = addressRepository.findAddressByDefaultAddressIs(true);

        assertThat(found).isPresent();
        assertThat(found.get().isDefaultAddress()).isTrue();
        assertThat(found.get().getAddressLine1()).isEqualTo("123 Main St");
    }

    @Test
    void findAddressByDefaultAddressIs_shouldReturnEmpty_whenNoDefaultExists() {
        addressRepository.save(testAddress);

        Optional<Address> found = addressRepository.findAddressByDefaultAddressIs(true);

        assertThat(found).isEmpty();
    }

    @Test
    void findAddressByUser_UserIdAndId_shouldReturnAddress_whenExists() {
        Address savedAddress = addressRepository.save(testAddress);

        Optional<Address> found = addressRepository.findAddressByUser_UserIdAndId(
                savedAddress.getId(), testUser.getUserId());

        assertThat(found).isPresent();
        assertThat(found.get().getAddressLine1()).isEqualTo("123 Main St");
    }

    @Test
    void findAddressByUser_UserIdAndId_shouldReturnEmpty_whenNotExists() {
        Optional<Address> found = addressRepository.findAddressByUser_UserIdAndId(999L, 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldPersistAddress() {
        Address savedAddress = addressRepository.save(testAddress);

        assertThat(savedAddress.getId()).isNotNull();
        assertThat(savedAddress.getAddressLine1()).isEqualTo("123 Main St");
        assertThat(savedAddress.getUser()).isEqualTo(testUser);
    }

    @Test
    void delete_shouldRemoveAddress() {
        Address savedAddress = addressRepository.save(testAddress);
        Long addressId = savedAddress.getId();

        addressRepository.delete(savedAddress);

        Optional<Address> found = addressRepository.findById(addressId);
        assertThat(found).isEmpty();
    }

    @Test
    void update_shouldModifyExistingAddress() {
        Address savedAddress = addressRepository.save(testAddress);

        savedAddress.setAddressLine1("789 Rd");
        savedAddress.setCity("Cairo");
        Address updatedAddress = addressRepository.save(savedAddress);

        assertThat(updatedAddress.getAddressLine1()).isEqualTo("789 Rd");
        assertThat(updatedAddress.getCity()).isEqualTo("Cairo");
        assertThat(updatedAddress.getId()).isEqualTo(savedAddress.getId());
    }

    @Test
    void setDefaultAddress_shouldMaintainOnlyOneDefault() {
        Address address1 = addressRepository.save(testAddress);

        Address address2 = new Address();
        address2.setAddressLine1("456 ST");
        address2.setCity("Giza");
        address2.setStateProvince("CA");
        address2.setPostalCode("90001");
        address2.setCountryCode("EG");
        address2.setUser(testUser);
        address2.setDefaultAddress(true);
        addressRepository.save(address2);

        Optional<Address> defaultAddress = addressRepository.findAddressByDefaultAddressIs(true);
        assertThat(defaultAddress).isPresent();
        assertThat(defaultAddress.get().getAddressLine1()).isEqualTo("456 ST");

        address2.setDefaultAddress(false);
        addressRepository.save(address2);

        address1.setDefaultAddress(true);
        addressRepository.save(address1);

        defaultAddress = addressRepository.findAddressByDefaultAddressIs(true);
        assertThat(defaultAddress).isPresent();
        assertThat(defaultAddress.get().getAddressLine1()).isEqualTo("123 Main St");
    }
}