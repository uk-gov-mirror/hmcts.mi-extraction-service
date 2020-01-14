package uk.gov.hmcts.reform.mi.miextractionservice;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class MiExtractionServiceApplication implements ApplicationRunner {

    public static void main(final String[] args) {
        SpringApplication.run(MiExtractionServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TODO Implementation
    }
}
