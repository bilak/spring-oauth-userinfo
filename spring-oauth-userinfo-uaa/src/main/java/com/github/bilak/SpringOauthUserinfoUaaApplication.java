package com.github.bilak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
@RestController
public class SpringOauthUserinfoUaaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauthUserinfoUaaApplication.class, args);
	}


	@GetMapping("/user")
	public Principal user(Principal principal){
		return principal;
	}
}
