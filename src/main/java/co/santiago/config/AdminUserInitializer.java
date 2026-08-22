package co.santiago.config;

import co.santiago.models.User;
import co.santiago.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = new User();

        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("12345"));

        userRepository.save(admin);

        log.info("Usuario inicial 'admin' creado");
    }
}
