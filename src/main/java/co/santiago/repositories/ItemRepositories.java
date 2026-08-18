package co.santiago.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import co.santiago.models.Item;

public interface ItemRepositories extends JpaRepository<Item, Long> {

}
