package com.marketnest.ecommerce.service.order;

import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.exception.OrderNotFoundException;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.order.OrderMapper;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.*;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private HtmlEscapeUtil htmlEscapeUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;
    private OrderRequestDto orderRequestDto;
    private OrderResponseDto orderResponseDto;
    private OrderSummaryDto orderSummaryDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotal(new BigDecimal("114.99"));
        testOrder.setOrderItems(new ArrayList<>());

        orderRequestDto = new OrderRequestDto();
        orderRequestDto.setShippingAddressId(1L);
        orderRequestDto.setBillingAddressId(1L);

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setStatus(Order.OrderStatus.PENDING.toString());
        orderResponseDto.setTotal(new BigDecimal("114.99"));

        orderSummaryDto = new OrderSummaryDto();
        orderSummaryDto.setId(1L);
        orderSummaryDto.setOrderDate(LocalDateTime.now());
        orderSummaryDto.setStatus(Order.OrderStatus.PENDING.toString());
        orderSummaryDto.setTotal(new BigDecimal("114.99"));
    }

    @Test
    void getUserOrders_shouldReturnUserOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder));

        when(orderRepository.findByUser_UserId(1L, pageable)).thenReturn(orderPage);
        when(orderMapper.toSummary(testOrder, htmlEscapeUtil)).thenReturn(orderSummaryDto);

        Page<OrderSummaryDto> result = orderService.getUserOrders(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
        verify(orderRepository).findByUser_UserId(1L, pageable);
    }

    @Test
    void getUserOrders_shouldReturnEmptyPage_whenNoOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(new ArrayList<>());

        when(orderRepository.findByUser_UserId(1L, pageable)).thenReturn(orderPage);

        Page<OrderSummaryDto> result = orderService.getUserOrders(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getOrderDetails_shouldReturnOrderDetails_whenOrderExists() {
        when(orderRepository.findByUser_UserIdAndId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder, htmlEscapeUtil)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.getOrderDetails(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findByUser_UserIdAndId(1L, 1L);
    }

    @Test
    void getOrderDetails_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByUser_UserIdAndId(anyLong(), anyLong())).thenReturn(
                Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderDetails(999L, 1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void createOrder_shouldCreateOrder_whenDataValid() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderMapper.toEntity(orderRequestDto, htmlEscapeUtil)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder, htmlEscapeUtil)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.createOrder(orderRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowException_whenUserNotFound() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("notfound@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(orderRequestDto))
                .isInstanceOf(UserNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shouldCancelOrder_whenOrderIsPending() {
        when(orderRepository.findByUser_UserIdAndId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder, htmlEscapeUtil)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.cancelOrder(1L, 1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(testOrder);
        assertThat(testOrder.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByUser_UserIdAndId(anyLong(), anyLong())).thenReturn(
                Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(999L, 1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void trackOrder_shouldReturnOrderDetails() {
        when(orderRepository.findByUser_UserIdAndId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder, htmlEscapeUtil)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.trackOrder(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findByUser_UserIdAndId(1L, 1L);
    }

    @Test
    void generateInvoicePdf_shouldGeneratePdf() {
        when(orderRepository.findByUser_UserIdAndId(1L, 1L)).thenReturn(Optional.of(testOrder));

        byte[] result = orderService.generateInvoicePdf(1L, 1L);

        assertThat(result).isNotNull();
        verify(orderRepository).findByUser_UserIdAndId(1L, 1L);
    }
}