package toyproject.hantoobot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BuyMarketOrderDto {

  private String buyKey;
  private String ticker;
  private int qty;
  private int qtyHist;

}
