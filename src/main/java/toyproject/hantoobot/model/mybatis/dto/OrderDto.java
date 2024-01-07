package toyproject.hantoobot.model.mybatis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import toyproject.hantoobot.model.enums.State;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderDto {

  private Long orderId;

  private String ticker;

  private String buyKey;

  private String sellKey;

  private State state;

  private int qty;

  private int buyPrice;

  private int totalPrice;

  private int qtyHist;

  private int sellPrice;
}
