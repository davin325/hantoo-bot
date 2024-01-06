package toyproject.hantoobot.model.mybatis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import toyproject.hantoobot.model.enums.Status;

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

  private Status state;

  private int qty;

  private int buyPrice;

  private int totalPrice;

  private int qtyHist;

  private int sellPrice;
}
