package toyproject.hantoobot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyproject.hantoobot.api.HanTooApi;
import toyproject.hantoobot.model.jpa.dto.BuyMarketOrderDto;
import toyproject.hantoobot.model.jpa.dto.CheckBuyOrderDto;
import toyproject.hantoobot.model.jpa.dto.CheckSellOrderDto;
import toyproject.hantoobot.model.jpa.dto.SellLimitOrderDto;
import toyproject.hantoobot.model.jpa.entity.Order;
import toyproject.hantoobot.model.jpa.entity.Stock;
import toyproject.hantoobot.model.enums.State;
import toyproject.hantoobot.repository.OrderRepository;
import toyproject.hantoobot.repository.StockRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OrderService {

  private final HanTooApi hanTooApi;

  private final StockRepository stockRepository;
  private final OrderRepository orderRepository;

  /**
   * 10:00 ~ 16:00 장 시간에 주기적으로 실행
   */
  public void newOrder() {
    List<Stock> stocks = stockRepository.findAll();
    for (Stock stock : stocks) {

      //주문 전 매도 되었는지 체크
      checkSellOrder(stock);

      //주문 전 매수 대상인지 체크
      if (!beforeBuy(stock)) {
        continue;
      }

      //매수
      Order buyOrder = buy(stock);

      //매도
      sell(buyOrder);
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        log.error("thread sleep error", e);
      }
    }
  }


  /**
   * 장 시간에 팔리지 못한 주식들을 최종적으로 매도 되었는지 확인 후 초기화 한다.
   */
  public void sellOrderInit() {
    //토큰 발급
    hanTooApi.getAuthorization();
    List<Stock> stocks = stockRepository.findAll();
    for (Stock stock : stocks) {
      checkSellOrder(stock);
    }

    List<Order> orders = orderRepository.findByState(State.WAIT);
    for (Order order : orders) {
      order.sellInit();
    }
  }

  /**
   * 장이 열리자마자 전날에 초기화 한 주식들을 매도 주문 해놓는다.
   */
  public void sellBeforeStart() {
    //토큰 발급
    hanTooApi.getAuthorization();
    List<Order> byState = orderRepository.findByState(State.INIT);
    for (Order order : byState) {
      sell(order);
    }
  }

  /**
   * 매도 되었는지 확인 후 업데이트.
   *
   * @param stock: 주식정보
   */
  public void checkSellOrder(Stock stock) {
    List<CheckSellOrderDto> checkSellOrderDtos = hanTooApi.checkSellOrder(stock);

    for (CheckSellOrderDto checkSellOrderDto : checkSellOrderDtos) {
      Order order = orderRepository.findBySellKey(checkSellOrderDto.getSellKey());
      if (order == null) {
        log.info("앱 내에서 수동으로 매도시도한 이력이 있는지 체크");
        log.info("sellUUID = {}, stockName = {}", checkSellOrderDto.getSellKey(),
            stock.getStockName());
        continue;
      }

      if (order.getQty() - checkSellOrderDto.getSellQty() == 0) {
        //매도 완료
        order.setQty(0);
        order.setState(State.SOLD);
      } else {
        //부분 매도 된 경우
        order.setQty(order.getQty() - checkSellOrderDto.getSellQty());
      }
    }

  }

  /**
   * 매수
   *
   * @param stock: 주식정보
   * @return 주문서
   */
  public Order buy(Stock stock) {
    //매수
    BuyMarketOrderDto buyMarketOrderDto = hanTooApi.buyMarketOrder(stock.getTicker(),
        stock.getVolume());

    if (buyMarketOrderDto.getBuyKey() != null) {
      //매수 주문 저장
      Order order = new Order(buyMarketOrderDto.getBuyKey(), State.BUY, buyMarketOrderDto.getQty(),
          buyMarketOrderDto.getQtyHist(), stock);
      orderRepository.save(order);

      //매수 완료 확인후 업데이트
      CheckBuyOrderDto checkBuyOrderDto = hanTooApi.checkBuyOrder(order);
      order.buyFinish(checkBuyOrderDto);
      return order;
    } else {
      throw new RuntimeException("매수 주문 실패");
    }
  }

  /**
   * 매도
   *
   * @param order: 주문서
   */
  public void sell(Order order) {
    //지정가 매도
    SellLimitOrderDto sellLimitOrderDto = hanTooApi.sellLimitOrder(order);
    if (sellLimitOrderDto.getSellKey() != null) {
      order.updateStartSell(sellLimitOrderDto);
    }
  }

  /**
   * 매수 전 매수 대상인지 확인 하는 메소드
   *
   * @param stock: 주식정보
   */
  public boolean beforeBuy(Stock stock) {

    if (stock.getVolume() == 0) {
      log.info("셋팅 수량이 0 인경우 주문을 하지 않음");
      return false;
    }

    //해당 티커의 가장 낮은 가격의주문서를 조회
    Order lowestOrder = orderRepository.findTop1ByStockTickerAndStateOrderByBuyPrice(
        stock.getTicker(), State.WAIT);
    if (lowestOrder == null) {
      log.info("이전 수량이 없어 첫 주문 시작");
      return true;
    } else if (lowestOrder.getBuyPrice() > 0) {
      Integer nowPrice = hanTooApi.checkPrice(stock.getTicker());
      double calPrice = lowestOrder.getBuyPrice() * stock.getBuyRate();

      if (nowPrice >= calPrice) {
        log.info("현재 가격이 주문해야할 가격보다 높음");
        return false;
      } else {
        return true;
      }
    } else {
      throw new RuntimeException("주문서의 주문가격이 음수");
    }
  }

  /**
   * 휴장일 체크 메소드
   *
   * @return true: 휴장, false: 개장
   */
  public boolean checkHoliday() {
    String checkMarket = hanTooApi.checkHoliday();
    if ("N".equals(checkMarket)) {
      log.info("휴장");
      return true;
    }
    return false;
  }

}
