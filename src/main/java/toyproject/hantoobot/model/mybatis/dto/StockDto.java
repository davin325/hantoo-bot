package toyproject.hantoobot.model.mybatis.dto;

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
public class StockDto {

  private String ticker;

  private String stockName;

  private double buyRate;

  private double sellRate;

  private int volume;
}
