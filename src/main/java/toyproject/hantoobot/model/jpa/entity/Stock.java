package toyproject.hantoobot.model.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseTimeEntity{

  @Id
  private String ticker;

  private String stockName;

  private double buyRate;

  private double sellRate;

  private int volume;

  public Stock(String stockName, String ticker, double buyRate, double sellRate, int volume) {
    this.stockName = stockName;
    this.ticker = ticker;
    this.buyRate = buyRate;
    this.sellRate = sellRate;
    this.volume = volume;
  }
}
