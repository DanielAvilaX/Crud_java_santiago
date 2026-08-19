package co.santiago.repositories;

import co.santiago.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepositories extends JpaRepository<Item, Long> {

    Page<Item> findByActivoTrue(Pageable pageable);

    Optional<Item> findByIdAndActivoTrue(Long id);
}