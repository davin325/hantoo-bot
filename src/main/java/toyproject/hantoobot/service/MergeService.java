package toyproject.hantoobot.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import toyproject.hantoobot.model.enums.State;
import toyproject.hantoobot.model.mybatis.dto.OrderDto;
import toyproject.hantoobot.model.mybatis.dto.StockDto;
import toyproject.hantoobot.model.mybatis.mapper.MergeMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class MergeService {

  @Autowired
  private MergeMapper mergeMapper;

  /**
   * 주문이 많이 쌓인 주식은 높은 가격에 산 주식과 낮은 가격에 산 주식으로 평균을 내어 하나의 주문서로 만들어 평단을 낮춘다.
   */
  public void mergeOrder() {
    List<StockDto> stocks = mergeMapper.getTicker();
    for (StockDto stock : stocks) {
      List<OrderDto> orders = mergeMapper.getOrders(stock);

      //주문이 10개 이상 되어있는 주식들
      if (orders.size() > 10) {
        OrderDto lowOrder = orders.get(1);
        OrderDto highOrder = orders.get(orders.size() - 1);

        String oldDate = String.valueOf(highOrder.getBuyKey()).split("_")[0];
        String newBuyUuid = (oldDate + "_" + UUID.randomUUID()).replaceAll("-", "_");

        int newQty = lowOrder.getQty() + highOrder.getQty();
        int newTotalPrice = lowOrder.getTotalPrice() + highOrder.getTotalPrice();
        int newBuyPrice = newTotalPrice / newQty; // 평단

        OrderDto mergeOrder = OrderDto.builder()
            .buyKey(newBuyUuid)
            .ticker(highOrder.getTicker())
            .qty(newQty)
            .qtyHist(newQty)
            .totalPrice(newTotalPrice)
            .state(State.INIT)
            .buyPrice(newBuyPrice).build();

        highOrder.setState(State.MERGE);
        lowOrder.setState(State.MERGE);
        mergeMapper.insertMergeOrder(mergeOrder);
        mergeMapper.updateMergeOrder(lowOrder, highOrder);

        log.info("MERGE 완료 티커: {}", mergeOrder.getTicker());
      }
    }
  }

}
