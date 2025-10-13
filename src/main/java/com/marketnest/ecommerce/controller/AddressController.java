package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.service.user.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final UserAddressMapper addressMapper;

    @PostMapping
    public ResponseEntity<?> createAddress(
            @Valid @RequestBody AddressRequestDto requestDTO, BindingResult bindingResult,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        String email = authentication.getName();

        Address address = addressService.createAddress(email, requestDTO);

        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(Authentication authentication) {
        String email = authentication.getName();

        List<Address> addresses = addressService.getUserAddresses(email);

        List<AddressResponseDTO> addressDtos = addresses.stream()
                .map(addressMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(addressDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        Address address = addressService.getAddress(email, id);
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(address);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDto requestDTO,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        String email = authentication.getName();

        Address updatedAddress = addressService.updateAddress(email, id, requestDTO);
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(updatedAddress);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        addressService.deleteAddress(email, id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Address deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id,
                                               Authentication authentication) {
        String email = authentication.getName();

        Address address = addressService.setDefaultAddress(email, id);
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(address);
        return ResponseEntity.ok(responseDTO);

    }
}
