package demo.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import demo.utils.JWTUtils;

public class JwtAuthorizationFilter extends OncePerRequestFilter{
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(request.getServletPath().equals("/refresh-token")) {
			filterChain.doFilter(request, response);
			return;
		}
		String jwtToken = request.getHeader(JWTUtils.HEADER);
		if(jwtToken == null) {
			filterChain.doFilter(request, response);
			return;
		}
		if(!jwtToken.startsWith(JWTUtils.PREFIX)) return;
		
		jwtToken = jwtToken.substring(JWTUtils.PREFIX.length());
		try {
			UsernamePasswordAuthenticationToken authentication = JWTUtils.getAuthentication(jwtToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch(JWTVerificationException verificationException) {
			verificationException.printStackTrace();
			response.setHeader("Error", verificationException.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
		
	}

}
