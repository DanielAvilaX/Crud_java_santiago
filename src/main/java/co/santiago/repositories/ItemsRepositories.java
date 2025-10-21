package co.santiago.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import co.santiago.models.Items;

public interface ItemsRepositories extends JpaRepository<Items, Long> {

}
