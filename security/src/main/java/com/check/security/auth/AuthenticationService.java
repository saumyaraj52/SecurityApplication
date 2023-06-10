package com.check.security.auth;

import com.check.security.config.JwtService;
import com.check.security.user.Role;
import com.check.security.user.UserNew;
import com.check.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	@Autowired
	private final UserRepository repository;
	@Autowired
	private final PasswordEncoder passwordEncoder;
	@Autowired
	private final JwtService jwtService;
	@Autowired
	private final AuthenticationManager authenticationManager;

	public AuthenticationResponse register(RegisterRequest request) {
		// TODO Auto-generated method stub
		var user = UserNew.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.passWord(passwordEncoder.encode(request.getPassword()))
				.role(request.getRole())
				//.role(Role.USER)
				.build();
		var savedUser = repository.save(user);
		var jwtToken = this.jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		//saveUserToken(savedUser, jwtToken);
		return AuthenticationResponse.builder()
				.accessToken(jwtToken)
				.refreshToken(refreshToken)
				.build();

		//return AuthenticationResponse.builder().token(jwtToken).build();
	}
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		// TODO Auto-generated method stub
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
		var user = repository.findByEmail(request.getEmail()).orElseThrow();
		var jwtToken = this.jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		//revokeAllUserTokens(user);
		//saveUserToken(user, jwtToken);
		return AuthenticationResponse.builder()
				.accessToken(jwtToken)
				.refreshToken(refreshToken)
				.build();
		//return AuthenticationResponse.builder().token(jwtToken).build();
	}

	public void refreshToken(
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = this.repository.findByEmail(userEmail)
					.orElseThrow();
			if (jwtService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtService.generateToken(user);
				//revokeAllUserTokens(user);
				//saveUserToken(user, accessToken);
				var authResponse = AuthenticationResponse.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.build();
				new ObjectMapper().writeValue(response.getOutputStream(),
						authResponse);
			}
		}
	}
}
