package back.code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ModuBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuBackApplication.class, args);
    }

}
