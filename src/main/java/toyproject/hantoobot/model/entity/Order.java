package toyproject.hantoobot.model.entity;

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
import toyproject.hantoobot.model.dto.CheckBuyOrderDto;
import toyproject.hantoobot.model.dto.SellLimitOrderDto;
import toyproject.hantoobot.model.enums.Status;

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
  private Status state;

  private int qty;

  private int buyPrice;

  private int totalPrice;

  private int qtyHist;

  private int sellPrice;

  public Order(String buyKey, Status state, int qty, int qtyHist, Stock stock) {
    this.buyKey = buyKey;
    this.state = state;
    this.qty = qty;
    this.qtyHist = qtyHist;
    this.stock = stock;
  }

  public void buyFinish(CheckBuyOrderDto checkBuyOrderDto) {
    this.state = Status.WAIT;
    this.buyPrice = checkBuyOrderDto.getBuyPrice();
    this.totalPrice = checkBuyOrderDto.getTotalPrice();
  }

  public void updateStartSell(SellLimitOrderDto sellLimitOrderDto) {
    this.sellKey = sellLimitOrderDto.getSellKey();
    this.sellPrice = sellLimitOrderDto.getSellPrice();
  }

  public void sellInit() {
    this.sellKey = null;
    this.state = Status.INIT;
    this.sellPrice = 0;
  }
}
