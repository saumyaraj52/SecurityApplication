package com.check.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserNew, Integer> {
	  Optional<UserNew> findByEmail(String email);

	}
