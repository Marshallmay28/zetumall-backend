package com.zetumall.user;

import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    /**
     * Get current user profile
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyProfile(
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "image", user.getImage() != null ? user.getImage() : "",
                "role", user.getRole().name(),
                "country", user.getCountry(),
                "currency", user.getCurrency(),
                "isActive", user.getIsActive(),
                "createdAt", user.getCreatedAt()
            );
            
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch profile"));
        }
    }

    /**
     * Update user profile
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (request.containsKey("name")) user.setName(request.get("name"));
            if (request.containsKey("image")) user.setImage(request.get("image"));
            if (request.containsKey("country")) user.setCountry(request.get("country"));
            
            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully"));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update profile"));
        }
    }

    /**
     * Get user addresses
     * GET /api/users/addresses
     */
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses(
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Address> addresses = addressRepository.findByUserId(user.getId());
            
            return ResponseEntity.ok(ApiResponse.success(addresses));
        } catch (Exception e) {
            log.error("Error fetching addresses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch addresses"));
        }
    }

    /**
     * Add new address
     * POST /api/users/addresses
     */
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @RequestBody Address addressRequest,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            addressRequest.setUserId(user.getId());
            
            // If this is the first address, make it default
            List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
            if (existingAddresses.isEmpty()) {
                addressRequest.setIsDefault(true);
            }
            
            Address savedAddress = addressRepository.save(addressRequest);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(savedAddress, "Address added successfully"));
        } catch (Exception e) {
            log.error("Error adding address", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add address"));
        }
    }

    /**
     * Update address
     * PUT /api/users/addresses/{id}
     */
    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @PathVariable String id,
            @RequestBody Address addressRequest,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Address address = addressRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            
            if (!address.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized"));
            }
            
            // Update fields
            address.setFullName(addressRequest.getFullName());
            address.setPhoneNumber(addressRequest.getPhoneNumber());
            address.setStreetAddress(addressRequest.getStreetAddress());
            address.setCity(addressRequest.getCity());
            address.setState(addressRequest.getState());
            address.setPostalCode(addressRequest.getPostalCode());
            address.setCountry(addressRequest.getCountry());
            
            Address savedAddress = addressRepository.save(address);
            
            return ResponseEntity.ok(ApiResponse.success(savedAddress, "Address updated"));
        } catch (Exception e) {
            log.error("Error updating address", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update address"));
        }
    }

    /**
     * Delete address
     * DELETE /api/users/addresses/{id}
     */
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Address address = addressRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            
            if (!address.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized"));
            }
            
            addressRepository.delete(address);
            
            return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting address", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete address"));
        }
    }
}
