package demo;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import demo.entities.AppUser;
import demo.enums.AppRole;
import demo.repositories.AppUserRepository;

@SpringBootApplication
public class SpringRestDemoApplication {

	PasswordEncoder encoder = new BCryptPasswordEncoder();
	
	public static void main(String[] args) {
		SpringApplication.run(SpringRestDemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(AppUserRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(
						List.of(new AppUser().setEmail("admi@gmail.com").setPassword(encoder.encode("Test 123")),
								new AppUser().setEmail("user@gmail.com").setPassword(encoder.encode("Test 123"))
										.setPermissions(List.of(AppRole.USER))));
			}

		};
	}

}
