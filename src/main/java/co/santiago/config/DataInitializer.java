package co.santiago.config;

import co.santiago.models.Items;
import co.santiago.repositories.ItemsRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ItemsRepositories repository;

    // Inyección de dependencias vía constructor
    public DataInitializer(ItemsRepositories repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.save(new Items("Computador", 3500000));
        repository.save(new Items("Teclado", 250000));
        repository.save(new Items("Mouse", 120000));

        log.info("Datos iniciales cargados desde DatabaseInitializer");
    }
}

