package com.check.security.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	@Value("${application.security.jwt.expiration}")
	private long jwtExpiration;
	@Value("${application.security.jwt.refresh-token.expiration}")
	private long refreshExpiration;
	//private static final String SECRET_KEY="5A7134743777217A25432646294A404E635266556A586E3272357538782F413F";
	public String extractUsername(String jwt) {
		// TODO Auto-generated method stub
		return extractClaim(jwt,Claims::getSubject);
	}
	
	public <T> T extractClaim(String token, Function<Claims,T> claimsResolver)
	{
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	private Claims extractAllClaims(String token)
	{
		return Jwts
				.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	public String generateToken(UserDetails userDetails)
	{
		return generateToken(new HashMap<>(),userDetails,jwtExpiration);
				
	}

	public String generateRefreshToken(UserDetails userDetails)
	{
		return generateToken(new HashMap<>(),userDetails,refreshExpiration);

	}

	public String generateToken(Map<String,Object> extraClaims,UserDetails userDetails,long expiration)
	{
		return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+ expiration))//1000*60*24))
				.signWith(getSigningKey(),SignatureAlgorithm.HS256)
				.compact();
				
	}
	
	private Key getSigningKey() {
		// TODO Auto-generated method stub
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	public boolean isTokenValid(String token,UserDetails userDetails)
	{
		final String userName = extractUsername(token);
		return (userName.equals(userDetails.getUsername())) && (!isTokenExpired(token));
	}
	
	private boolean isTokenExpired(String token)
	{
		return extractExpiration(token).before(new Date());
	}
	
	private Date extractExpiration(String token)
	{
		return (Date) extractClaim(token,Claims::getExpiration);
	}

}
