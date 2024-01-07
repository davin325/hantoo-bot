package toyproject.hantoobot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import toyproject.hantoobot.model.jpa.entity.Order;
import toyproject.hantoobot.model.enums.State;


public interface OrderRepository extends JpaRepository<Order,Long> {

  //TODO 메소드명이 길어지는 이슈
  Order findTop1ByStockTickerAndStateOrderByBuyPrice(String ticker, State state);

  Order findBySellKey(String sellKey);

  List<Order> findByState(State wait);


}
