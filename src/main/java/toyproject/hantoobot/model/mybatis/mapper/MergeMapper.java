package toyproject.hantoobot.model.mybatis.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import toyproject.hantoobot.model.mybatis.dto.OrderDto;
import toyproject.hantoobot.model.mybatis.dto.StockDto;

@Mapper
public interface MergeMapper {
  List<StockDto> getTicker();

  List<OrderDto> getOrders(StockDto stock);

  void insertMergeOrder(OrderDto mergeOrder);

  void updateMergeOrder(@Param("lowOrder") OrderDto lowOrder, @Param("highOrder")OrderDto highOrder);
}
