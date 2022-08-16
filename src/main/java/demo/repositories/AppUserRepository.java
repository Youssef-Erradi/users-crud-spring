package demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.entities.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
	AppUser getByEmail(String email);
}
