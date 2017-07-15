package com.github.bilak.poc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lvasek.
 */
@RestController
public class TestController {

	@GetMapping
	public String hello(){
		return "hello world";
	}
}
