package toyproject.hantoobot.utill;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class CalculatePrice {

  /**
   * 판매 가격을 한투의 데이터 형식에 맞게 반올림 계산을 해주는 메소드
   * @param buyPrice: 매수가격
   * @param sellRate: 매도 퍼센트 가격 ex)1.02는 2퍼센트 가격에 매도
   * @return
   */
  public BigDecimal getRoundedNumber(String buyPrice, String sellRate) {
    BigDecimal sellPrice = new BigDecimal(buyPrice).multiply(new BigDecimal(sellRate));
    BigDecimal roundedNumber = null;
    if (sellPrice.intValue() < 2000) {
      //2,000  미만 : 1원
      roundedNumber = sellPrice.divide(BigDecimal.ONE, 0, RoundingMode.HALF_UP)
          .multiply(BigDecimal.ONE);
    } else if (sellPrice.intValue() >= 2000 && sellPrice.intValue() < 20000) {
//            2,000 ~ 20,000  미만 : 10원
      // 10의 자리로 반올림
      roundedNumber = sellPrice.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP)
          .multiply(BigDecimal.TEN);
    } else if (sellPrice.intValue() >= 20000 && sellPrice.intValue() < 200000) {
      //            20,000 ~ 200,000  미만 : 100원
      roundedNumber = sellPrice.divide(new BigDecimal(100), 0, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(100));
    } else {
      roundedNumber = sellPrice.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(1000));
    }
    return roundedNumber;
  }
}
