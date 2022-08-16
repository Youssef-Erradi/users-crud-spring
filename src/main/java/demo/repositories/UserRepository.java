package demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import demo.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	@Query("FROM User u "
			+ "WHERE LOWER(u.name||u.email) LIKE %:keyword%")
	List<User> searchByKeyword(String keyword);
}
