package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.service.user.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @MockBean
    private UserAddressMapper addressMapper;

    private Address testAddress;
    private AddressRequestDto addressRequestDto;
    private AddressResponseDTO addressResponseDto;

    @BeforeEach
    void setUp() {
        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setAddressLine1("123 Main St");
        testAddress.setCity("Cairo");
        testAddress.setStateProvince("CA");
        testAddress.setPostalCode("10001");
        testAddress.setCountryCode("EG");
        testAddress.setDefaultAddress(false);

        addressRequestDto = new AddressRequestDto();
        addressRequestDto.setAddressLine1("123 Main St");
        addressRequestDto.setCity("Cairo");
        addressRequestDto.setStateProvince("CA");
        addressRequestDto.setPostalCode("10001");
        addressRequestDto.setCountryCode("EG");

        addressResponseDto = new AddressResponseDTO();
        addressResponseDto.setId(1L);
        addressResponseDto.setAddressLine1("123 Main St");
        addressResponseDto.setCity("Cairo");
        addressResponseDto.setStateProvince("CA");
        addressResponseDto.setPostalCode("10001");
        addressResponseDto.setCountryCode("EG");
        addressResponseDto.setDefaultAddress(false);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createAddress_shouldReturnCreated_whenValidData() throws Exception {
        when(addressService.createAddress(eq("test@example.com"), any(AddressRequestDto.class)))
                .thenReturn(testAddress);
        when(addressMapper.toResponseDTO(testAddress)).thenReturn(addressResponseDto);

        mockMvc.perform(post("/api/users/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("Cairo"));

        verify(addressService).createAddress(eq("test@example.com"), any(AddressRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createAddress_shouldReturnBadRequest_whenValidationFails() throws Exception {
        AddressRequestDto invalidDto = new AddressRequestDto();

        mockMvc.perform(post("/api/users/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());

        verify(addressService, never()).createAddress(anyString(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllAddresses_shouldReturnAddressList() throws Exception {
        List<Address> addresses = Collections.singletonList(testAddress);
        when(addressService.getUserAddresses("test@example.com")).thenReturn(addresses);
        when(addressMapper.toResponseDTO(testAddress)).thenReturn(addressResponseDto);

        mockMvc.perform(get("/api/users/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].addressLine1").value("123 Main St"));

        verify(addressService).getUserAddresses("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAddress_shouldReturnAddress_whenExists() throws Exception {
        when(addressService.getAddress("test@example.com", 1L)).thenReturn(testAddress);
        when(addressMapper.toResponseDTO(testAddress)).thenReturn(addressResponseDto);

        mockMvc.perform(get("/api/users/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addressLine1").value("123 Main St"));

        verify(addressService).getAddress("test@example.com", 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateAddress_shouldUpdateAndReturn_whenValidData() throws Exception {
        AddressRequestDto updateDto = new AddressRequestDto();
        updateDto.setAddressLine1("456 Oak Ave");
        updateDto.setCity("Giza");
        updateDto.setStateProvince("CA");
        updateDto.setPostalCode("90001");
        updateDto.setCountryCode("EG");

        testAddress.setAddressLine1("456 Oak Ave");
        testAddress.setCity("Giza");

        when(addressService.updateAddress(eq("test@example.com"), eq(1L),
                any(AddressRequestDto.class)))
                .thenReturn(testAddress);
        when(addressMapper.toResponseDTO(testAddress)).thenReturn(addressResponseDto);

        mockMvc.perform(put("/api/users/addresses/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(addressService).updateAddress(eq("test@example.com"), eq(1L),
                any(AddressRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateAddress_shouldReturnBadRequest_whenValidationFails() throws Exception {
        AddressRequestDto invalidDto = new AddressRequestDto();

        mockMvc.perform(put("/api/users/addresses/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(addressService, never()).updateAddress(anyString(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteAddress_shouldDeleteAndReturnSuccess() throws Exception {
        doNothing().when(addressService).deleteAddress("test@example.com", 1L);

        mockMvc.perform(delete("/api/users/addresses/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Address deleted successfully"));

        verify(addressService).deleteAddress("test@example.com", 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void setDefaultAddress_shouldSetAsDefault() throws Exception {
        testAddress.setDefaultAddress(true);
        addressResponseDto.setDefaultAddress(true);

        when(addressService.setDefaultAddress("test@example.com", 1L)).thenReturn(testAddress);
        when(addressMapper.toResponseDTO(testAddress)).thenReturn(addressResponseDto);

        mockMvc.perform(patch("/api/users/addresses/1/default")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.defaultAddress").value(true));

        verify(addressService).setDefaultAddress("test@example.com", 1L);
    }
}