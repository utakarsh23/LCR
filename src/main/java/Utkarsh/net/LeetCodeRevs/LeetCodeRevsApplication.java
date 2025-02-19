package Utkarsh.net.LeetCodeRevs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //helps with scheduling
public class LeetCodeRevsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeetCodeRevsApplication.class, args);
	}

}
