package demo.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import demo.entities.AppUser;
import demo.repositories.AppUserRepository;

@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer {
	@Autowired
	private AppUserRepository repository;
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
//		http.csrf().disable();
		http.cors().and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests().antMatchers("/refresh-token").permitAll()
			.and()
			.authorizeRequests().anyRequest().authenticated()
			.and()
			.csrf().disable()
			.addFilter(new JwtAuthenticationFilter(authenticationConfiguration.getAuthenticationManager()))
			.addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
	
	@Bean
	protected PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	protected UserDetailsService userDetailsService() {
		return username -> {
			AppUser user = repository.getByEmail(username);
			if(user == null)
				throw new UsernameNotFoundException("Username '"+username+"' not found");
			List<GrantedAuthority> authorities = new ArrayList<>();
			user.getPermissions().forEach(role ->{
				authorities.add(new SimpleGrantedAuthority(role.toString()));
			});
			return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
		};
	}
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowedOrigins("*");
        WebMvcConfigurer.super.addCorsMappings(registry);
    }

}
