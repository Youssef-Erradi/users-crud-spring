package demo.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import demo.utils.JWTUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager authenticationManager;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String username = null, password = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> json = (Map<String, String>)new ObjectMapper().readValue(request.getInputStream(), Map.class);
			username = json.get("username");
			password = json.get("password");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		return authenticationManager.authenticate(token);
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		User user = (User) authResult.getPrincipal();
		Map<String, String> tokens = JWTUtils.createAccessTokenAndRefreshToken(user);
		
		Cookie cookie = new Cookie("refresh", tokens.get("refresh"));
//		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
//		cookie.setDomain("localhost");
		cookie.setMaxAge(JWTUtils.REFRESH_TOKEN_EXPIRATION.intValue()/1000);
		response.addCookie(cookie);
		response.setContentType("application/json");
		ResponseCookie resCookie = ResponseCookie.from("refresh_token", tokens.get("refresh"))
	            .httpOnly(true)
	            .sameSite("None")
	            .secure(false)
	            .path("/")
	            .maxAge(Math.toIntExact(JWTUtils.REFRESH_TOKEN_EXPIRATION))
	            .build();
	    response.addHeader("Set-Cookie", resCookie.toString());
		
		new ObjectMapper().writeValue(response.getOutputStream(), Map.of("access", tokens.get("access")));
	}

}
