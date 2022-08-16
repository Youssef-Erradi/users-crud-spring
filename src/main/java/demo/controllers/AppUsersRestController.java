package demo.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import demo.entities.AppUser;
import demo.repositories.AppUserRepository;
import demo.utils.JWTUtils;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AppUsersRestController {
	private AppUserRepository repository;
	
	@GetMapping("app-users")
	public ResponseEntity<List<AppUser>> get(){
		List<AppUser> users = repository.findAll();
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = request.getHeader(JWTUtils.HEADER);
		if(refreshToken == null) return;
		if(!refreshToken.startsWith(JWTUtils.PREFIX)) return;
		
		refreshToken = refreshToken.substring(JWTUtils.PREFIX.length());
		try {
			Map<String, String> tokens = JWTUtils.renewAccessToken(refreshToken);
			response.setContentType("application/json");
			new ObjectMapper().writeValue(response.getOutputStream(), tokens);
		} catch(Exception exception) {
			exception.printStackTrace();
			response.setHeader("Error", exception.getMessage());
		}
	}
	
	@GetMapping("/profile")
	public ResponseEntity<AppUser> profile(Principal principal){
		AppUser user = repository.getByEmail(principal.getName());
		return ResponseEntity.ok(user);
	}

}
