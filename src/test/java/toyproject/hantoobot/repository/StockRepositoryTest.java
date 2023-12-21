package toyproject.hantoobot.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import toyproject.hantoobot.model.entity.Stock;

@SpringBootTest
@Transactional
@Rollback(value = false)
class StockRepositoryTest {

  @Autowired
  private StockRepository stockRepository;



  @Test
  public void createStork() {
    Stock stock = new Stock("신한지주","055550",0.99,1.03,1);
    stockRepository.save(stock);
  }
}