package demo.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import demo.entities.User;
import demo.repositories.UserRepository;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UsersRestController {
	private UserRepository repository;
	
	@GetMapping
	public ResponseEntity<List<User>> get(@RequestParam(defaultValue = "") String q){
		List<User> users = repository.findAll();
		if(!q.isBlank())
			users = repository.searchByKeyword(q);
		return ResponseEntity.ok(users);
	}
	
	@PostMapping
	public ResponseEntity<User> save(@RequestBody @Valid User user){
		return ResponseEntity.ok(repository.save(user));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<User> get(@PathVariable Integer id){
		return ResponseEntity.ok(repository.findById(id).orElseThrow());
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Integer id){
		if (!repository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id '"+id+"' not found !");
		}
		repository.deleteById(id);
	}
	
	@PutMapping("/{id}")
	public void update(@PathVariable Integer id, @RequestBody @Valid User user){
		if (!repository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id '"+id+"' not found !");
		}
		repository.save(user);
	}
	
	
}
