package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.Payment;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order testOrder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        categoryRepository.deleteAll();

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Ahmed");
        user.setLastName("Ashraf");
        user = userRepository.save(user);

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine1("123 Main St");
        address.setCity("Test City");
        address.setStateProvince("TS");
        address.setCountryCode("EGY");
        address.setPostalCode("12345");
        address = addressRepository.save(address);

        testOrder = new Order();
        testOrder.setUser(user);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setShippingAddress(address);
        testOrder.setBillingAddress(address);
        testOrder.setSubtotal(new BigDecimal("99.99"));
        testOrder.setShippingCost(new BigDecimal("10.00"));
        testOrder.setTax(new BigDecimal("5.00"));
        testOrder.setDiscount(BigDecimal.ZERO);
        testOrder.setTotal(new BigDecimal("114.99"));
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void save_shouldPersistPayment() {
        Payment payment = createPayment(
                new BigDecimal("114.99"),
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentStatus.COMPLETED
        );

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("114.99"));
        assertThat(saved.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        assertThat(saved.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByTransactionId_shouldReturnPayment_whenExists() {
        Payment payment = createPayment(
                new BigDecimal("114.99"),
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentStatus.COMPLETED
        );
        payment.setTransactionId("TXN123456");
        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByTransactionId("TXN123456");

        assertThat(found).isPresent();
        assertThat(found.get().getTransactionId()).isEqualTo("TXN123456");
    }

    @Test
    void findByTransactionId_shouldReturnEmpty_whenNotExists() {
        Optional<Payment> found = paymentRepository.findByTransactionId("NONEXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    void findByOrderId_shouldReturnAllPaymentsForOrder() {
        Payment payment1 = createPayment(
                new BigDecimal("50.00"),
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentStatus.COMPLETED
        );
        payment1.setTransactionId("TXN001");

        Payment payment2 = createPayment(
                new BigDecimal("64.99"),
                Payment.PaymentMethod.PAYPAL,
                Payment.PaymentStatus.COMPLETED
        );
        payment2.setTransactionId("TXN002");

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findByOrderId(testOrder.getId());

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getOrder().getId().equals(testOrder.getId()));
    }

    @Test
    void findByOrderId_shouldReturnEmptyList_whenNoPayments() {
        List<Payment> payments = paymentRepository.findByOrderId(999L);

        assertThat(payments).isEmpty();
    }

    @Test
    void save_shouldUpdateExistingPayment() {
        Payment payment = createPayment(
                new BigDecimal("114.99"),
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentStatus.PENDING
        );
        Payment saved = paymentRepository.save(payment);

        saved.setStatus(Payment.PaymentStatus.COMPLETED);
        saved.setPaymentDate(LocalDateTime.now());
        saved.setTransactionId("TXN789");

        Payment updated = paymentRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(updated.getTransactionId()).isEqualTo("TXN789");
        assertThat(updated.getPaymentDate()).isNotNull();
    }

    @Test
    void save_shouldHandleDifferentPaymentMethods() {
        for (Payment.PaymentMethod method : Payment.PaymentMethod.values()) {
            Payment payment = createPayment(
                    new BigDecimal("114.99"),
                    method,
                    Payment.PaymentStatus.COMPLETED
            );
            payment.setTransactionId("TXN_" + method.name());

            Payment saved = paymentRepository.save(payment);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getPaymentMethod()).isEqualTo(method);
        }
    }

    @Test
    void save_shouldHandleDifferentPaymentStatuses() {
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            Payment payment = createPayment(
                    new BigDecimal("114.99"),
                    Payment.PaymentMethod.CREDIT_CARD,
                    status
            );
            payment.setTransactionId("TXN_" + status.name());

            Payment saved = paymentRepository.save(payment);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void delete_shouldRemovePayment() {
        Payment payment = createPayment(
                new BigDecimal("114.99"),
                Payment.PaymentMethod.CREDIT_CARD,
                Payment.PaymentStatus.COMPLETED
        );
        Payment saved = paymentRepository.save(payment);

        paymentRepository.deleteById(saved.getId());

        Optional<Payment> found = paymentRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    private Payment createPayment(BigDecimal amount, Payment.PaymentMethod method,
                                  Payment.PaymentStatus status) {
        Payment payment = new Payment();
        payment.setOrder(testOrder);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setStatus(status);
        return payment;
    }
}