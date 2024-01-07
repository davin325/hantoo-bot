package toyproject.hantoobot.model.enums;

/**
 * BUY: 매수완료,
 * WAIT: 매도중,
 * SOLD: 매도완료,
 * MERGE: 합쳐진 주문서,
 * INIT: 매도 되지 않은 주문서 초기화
 */
public enum State {
  BUY, WAIT, SOLD, MERGE, INIT
}
