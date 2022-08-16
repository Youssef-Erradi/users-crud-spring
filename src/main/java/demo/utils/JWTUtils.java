package demo.utils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import demo.entities.AppUser;
import demo.repositories.AppUserRepository;

@Service
public class JWTUtils {
	
	//This should be an environment variable
	private static final String SECRET_KEY = "82c61c5510c54d5fa330e8fa70c8497b";
	
	public static final Algorithm ALGORITHM = Algorithm.HMAC512(SECRET_KEY);
	public static final String HEADER = "Authorization";
	public static final String PREFIX = "Bearer ";
	public static final Long   ACCESS_TOKEN_EXPIRATION = TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES);
	public static final Long   REFRESH_TOKEN_EXPIRATION = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
	
	// This shouldn't be used, need to create AppUserService class.
	@Autowired
	private static AppUserRepository repository;
	
	public static Map<String, String> createAccessTokenAndRefreshToken(final User user){
		final String accessToken = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(Instant.now().plusMillis(ACCESS_TOKEN_EXPIRATION))
				.withClaim("roles", user.getAuthorities().stream().map(ga -> ga.getAuthority()).toList())
				.sign(ALGORITHM);
		
		final String refreshToken = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
				.sign(ALGORITHM);
		
		return Map.of("access",accessToken,"refresh",refreshToken);
	}
	
	public static UsernamePasswordAuthenticationToken getAuthentication(final String token) {
		UsernamePasswordAuthenticationToken authentication = null;
		try {
			JWTVerifier jwtVerifier = JWT.require(ALGORITHM).build();
			DecodedJWT decodedJWT = jwtVerifier.verify(token);
			String username = decodedJWT.getSubject();
			List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
			authentication = new UsernamePasswordAuthenticationToken(username,
						null, roles.stream().map(role -> new SimpleGrantedAuthority(role)).toList());
		} catch(JWTVerificationException verificationException) {
			throw verificationException;
		}
		return authentication;
	}
	
	public static Map<String, String> renewAccessToken(final String refreshToken){
		Map<String, String> map = Map.of();
		JWTVerifier jwtVerifier = JWT.require(ALGORITHM).build();
		try {
			DecodedJWT decodedJWT = jwtVerifier.verify(refreshToken);
			String username = decodedJWT.getSubject();
			AppUser appUser = repository.getByEmail(username);
			final String accessToken = JWT.create()
					.withSubject(appUser.getEmail())
					.withExpiresAt(Instant.now().plusMillis(ACCESS_TOKEN_EXPIRATION))
					.withClaim("roles", appUser.getPermissions().stream().map(r -> r.toString()).toList())
					.sign(ALGORITHM);
			map = Map.of("access",accessToken,"refresh",refreshToken);
		} catch(JWTVerificationException verificationException) {
			throw verificationException;
		}
		return map;
	}
	
}
