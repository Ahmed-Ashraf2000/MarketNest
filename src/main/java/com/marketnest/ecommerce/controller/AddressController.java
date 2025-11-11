package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Address;
import com.marketnest.ecommerce.service.user.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Address Management", description = "APIs for managing user addresses")
public class AddressController {
    private final AddressService addressService;
    private final UserAddressMapper addressMapper;

    @Operation(summary = "Create a new address",
            description = "Creates a new address for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address created successfully",
                    content = @Content(
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Get all addresses",
            description = "Retrieves all addresses for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(Authentication authentication) {
        String email = authentication.getName();

        List<Address> addresses = addressService.getUserAddresses(email);

        List<AddressResponseDTO> addressDtos = addresses.stream()
                .map(addressMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(addressDtos);
    }

    @Operation(summary = "Get an address by ID",
            description = "Retrieves a specific address by its ID for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        Address address = addressService.getAddress(email, id);
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(address);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Update an address",
            description = "Updates an existing address for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
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

    @Operation(summary = "Delete an address",
            description = "Deletes an address for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        addressService.deleteAddress(email, id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Address deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set default address",
            description = "Sets an address as the default for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default address set successfully",
                    content = @Content(
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PatchMapping("/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id,
                                               Authentication authentication) {
        String email = authentication.getName();

        Address address = addressService.setDefaultAddress(email, id);
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(address);
        return ResponseEntity.ok(responseDTO);

    }
}