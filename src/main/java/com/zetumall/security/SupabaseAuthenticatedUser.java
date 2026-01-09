package com.zetumall.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an authenticated user from Supabase
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupabaseAuthenticatedUser {
    private String id;          // Supabase Auth UUID
    private String email;
    private String role;
}
