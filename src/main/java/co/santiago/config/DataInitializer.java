package co.santiago.config;

import co.santiago.models.Item;
import co.santiago.repositories.ItemRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ItemRepositories repository;

    // Inyección de dependencias vía constructor
    public DataInitializer(ItemRepositories repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.save(new Item("Computador", 3500000));
        repository.save(new Item("Teclado", 250000));
        repository.save(new Item("Mouse", 120000));

        log.info("Datos iniciales cargados desde DatabaseInitializer");
    }
}

