# hantoo-bot
 한국투자증권의 API를 활용하여 그리드 형태의 거래를 하는 봇을 제작한다.

### 주문 로직
 1. 10시 최초 주문이 들어가기 전에 현재 초기화 되어있는 매도 주문들을 매도를 걸어 놓는다.
 2. 10시 ~ 16시 사이에 주기적으로 가격을 감시하며<br>
일정 설정 내의 가격 밑으로 비율이 내려갈 경우 매수를 하고 매도 분량은 매도 되었는지 체크한다.
 4. 17시에는 전체적으로 매도 되었는지 한번 더 체크 후 매도를 초기화한다. 

---

- DB Schema

<img width="240" alt="image" src="https://github.com/davin325/hantoo-bot/assets/24787361/d10dd683-3d74-434b-a523-ff574402632e">
<br>
<img width="605" alt="image" src="https://github.com/davin325/hantoo-bot/assets/24787361/1a21eaa3-9d28-4b0c-bc27-0ece159886ec">


- JPA PersistenceModel

<img width="417" alt="image" src="https://github.com/davin325/hantoo-bot/assets/24787361/c2096342-92ad-431a-8032-b78224cdfdc6">

- 텔레그램 연동
  
  <img width="347" alt="image" src="https://github.com/davin325/hantoo-bot/assets/24787361/60496521-11c7-471d-8885-9a9d4b7663f5">

---
개발환경<br>
Java17, SpringBoot3.2, JPA, MariaDB <br>

참고<br>
한국투자증권 KIS Developer: https://apiportal.koreainvestment.com







