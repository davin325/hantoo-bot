package toyproject.hantoobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toyproject.hantoobot.model.jpa.entity.Stock;

public interface StockRepository extends JpaRepository<Stock,Long> {

}
