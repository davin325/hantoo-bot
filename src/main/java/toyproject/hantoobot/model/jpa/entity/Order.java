package toyproject.hantoobot.model.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import toyproject.hantoobot.model.jpa.dto.CheckBuyOrderDto;
import toyproject.hantoobot.model.jpa.dto.SellLimitOrderDto;
import toyproject.hantoobot.model.enums.State;

@Entity
@Getter
@Setter
@Table(name = "stock_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticker")
  private Stock stock;

  @Column(unique = true)
  private String buyKey;

  @Column(unique = true)
  private String sellKey;

  @Enumerated(value = EnumType.STRING)
  private State state;

  private int qty;

  private int buyPrice;

  private int totalPrice;

  private int qtyHist;

  private int sellPrice;

  public Order(String buyKey, State state, int qty, int qtyHist, Stock stock) {
    this.buyKey = buyKey;
    this.state = state;
    this.qty = qty;
    this.qtyHist = qtyHist;
    this.stock = stock;
  }

  public void buyFinish(CheckBuyOrderDto checkBuyOrderDto) {
    this.state = State.WAIT;
    this.buyPrice = checkBuyOrderDto.getBuyPrice();
    this.totalPrice = checkBuyOrderDto.getTotalPrice();
  }

  public void updateStartSell(SellLimitOrderDto sellLimitOrderDto) {
    this.sellKey = sellLimitOrderDto.getSellKey();
    this.sellPrice = sellLimitOrderDto.getSellPrice();
  }

  public void sellInit() {
    this.sellKey = null;
    this.state = State.INIT;
    this.sellPrice = 0;
  }
}
