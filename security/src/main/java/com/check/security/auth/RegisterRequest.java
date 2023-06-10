package com.check.security.auth;

import com.check.security.user.Role;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
	
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private Role role;
}
