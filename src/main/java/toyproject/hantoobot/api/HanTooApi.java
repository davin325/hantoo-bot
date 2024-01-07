package toyproject.hantoobot.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import toyproject.hantoobot.model.jpa.dto.BuyMarketOrderDto;
import toyproject.hantoobot.model.jpa.dto.CheckBuyOrderDto;
import toyproject.hantoobot.model.jpa.dto.CheckSellOrderDto;
import toyproject.hantoobot.model.jpa.dto.SellLimitOrderDto;
import toyproject.hantoobot.model.jpa.entity.Order;
import toyproject.hantoobot.model.jpa.entity.Stock;
import toyproject.hantoobot.repository.KeyValueStoreRepository;
import toyproject.hantoobot.utill.AesUtill;
import toyproject.hantoobot.utill.CalculatePrice;


/**
 * 한국투자증권에서 제공하는 API
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HanTooApi {

  private final KeyValueStoreRepository keyValueStoreRepository;
  private final RestTemplate restTemplate;
  private final CalculatePrice calculatePrice;
  private final AesUtill aesUtill;

  private String appKey = null;
  private String appSecretKey = null;
  private String apiUrl = null;
  private String accessToken = null;
  private String getAccessToken = null;
  private String accessTokenExpired = null;
  private String account = null;


  /**
   * 설정 초기화 API
   */
  public void initSetting() {
    appKey = aesUtill.aesCBCDecode(
        keyValueStoreRepository.findByItemKey("hantooAppKey").getItemValue());
    appSecretKey = aesUtill.aesCBCDecode(
        keyValueStoreRepository.findByItemKey("hantooAppSecretKey").getItemValue());
    apiUrl = keyValueStoreRepository.findByItemKey("hantooApiUrl").getItemValue();
    account = keyValueStoreRepository.findByItemKey("hantooAccount").getItemValue();
    log.info("키값 초기화 완료");
  }

  /**
   * 주식의 현재가격 조회 API
   *
   * @param pdno 티커
   * @return 현재 주식의 가격
   */
  public Integer checkPrice(String pdno) {
    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "FHKST01010100");

    HttpEntity request = new HttpEntity(headers);

    URI url = UriComponentsBuilder.fromUriString(apiUrl)
        .path("uapi/domestic-stock/v1/quotations/inquire-ccnl")
        .queryParam("FID_COND_MRKT_DIV_CODE", "J").queryParam("FID_INPUT_ISCD", pdno)
        .build()
        .toUri();

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET,
        request, new ParameterizedTypeReference<>() {
        });

    if (response.getStatusCode().is2xxSuccessful()) {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        JsonNode output = jsonNode.get("output");
        Integer price = output.get("stck_prpr").asInt();
        return price;
      } catch (JsonProcessingException e) {
        log.error("[checkPrice] JSON 파싱 에러 response.getBody = {}", response.getBody());
        return Integer.MAX_VALUE;
      }
    } else {
      log.error("[checkPrice] response.getStatusCode = {}", response.getStatusCode().value());
      return Integer.MAX_VALUE;
    }
  }

  /**
   * 휴장일 조회 API
   *
   * @return "Y":개장, "N":휴장
   */
  public String checkHoliday() {
    // 현재 날짜 가져오기
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String formattedDate = currentDate.format(formatter);

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "CTCA0903R");
    headers.set("custtype", "P");

    HttpEntity request = new HttpEntity(headers);

    URI url = UriComponentsBuilder.fromUriString(apiUrl)
        .path("uapi/domestic-stock/v1/quotations/chk-holiday")
        .queryParam("BASS_DT", formattedDate).queryParam("CTX_AREA_NK", "")
        .queryParam("CTX_AREA_FK", "").build()
        .toUri();

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET,
        request, new ParameterizedTypeReference<>() {
        });

    if (response.getStatusCode().is2xxSuccessful()) {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        JsonNode outputArray = jsonNode.path("output");

        for (JsonNode item : outputArray) {
          String baseDt = item.path("bass_dt").asText();
          String opndYn = item.path("opnd_yn").asText();

          if (baseDt.equals(formattedDate) && opndYn.equals("Y")) {
            log.info("장이 열렸습니다.");
            return "Y";
          } else if (baseDt.equals(formattedDate) && opndYn.equals("N")) {
            log.info("휴장일 입니다.");
            return "N";
          } else {
            log.error("[checkHoliday] opndYn = {}", opndYn);
            return "N";
          }
        }
        //for 문을 타지 않은 경우임
        log.error("[checkHoliday] outputArray = {}", outputArray);
        return "N";
      } catch (JsonProcessingException e) {
        log.error("[checkHoliday] response.getBody = {}", response.getBody());
        return "N";
      }
    } else {
      log.error("[checkHoliday] response.getStatusCode = {}",
          response.getStatusCode().value());
      return "N";
    }
  }

  /**
   * 토큰값 발급 API
   */
  public void getAuthorization() {
    if (accessTokenExpired != null) {
      // 현재 날짜 가져오기
      LocalDateTime currentDateTime = LocalDateTime.now();

      DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      LocalDateTime parseAccessTokenExpired = LocalDateTime.parse(accessTokenExpired,
          inputFormatter).minusHours(1);

      if (currentDateTime.isBefore(parseAccessTokenExpired)) {
        //토큰값이 유효하므로 재발급하지 않고 종료
        return;
      } else {
        log.info("[getAuthorization] 토큰만료 재발급 시작");
      }
    }

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    //BODY 셋팅
    Map<String, String> requestBodyMap = new HashMap<>();
    requestBodyMap.put("grant_type", "client_credentials");
    requestBodyMap.put("appkey", appKey);
    requestBodyMap.put("appsecret", appSecretKey);

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBody = objectMapper.writeValueAsString(requestBodyMap);
      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
      URI url = UriComponentsBuilder.fromUriString(apiUrl).path("oauth2/tokenP").build().toUri();
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
          request, new ParameterizedTypeReference<>() {
          });
      if (response.getStatusCode().is2xxSuccessful()) {
        //토큰 설정
        accessToken = String.valueOf(response.getBody().get("access_token"));
        accessTokenExpired = String.valueOf(response.getBody().get("access_token_token_expired"));
      } else {
        log.error("[getAuthorization] response.getStatusCode = {}", response.getStatusCode());
      }
    } catch (JsonProcessingException e) {
      log.error("[getAuthorization] requestBodyMap = {}", requestBodyMap);
      throw new RuntimeException("requestBodyMap 셋팅 에러", e);
    }
  }

  /**
   * 시장가 매수주문
   *
   * @param pdno   : 티커
   * @param ordQty : 수량
   * @return 매수 주문서
   */
  public BuyMarketOrderDto buyMarketOrder(String pdno, int ordQty) {

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "TTTC0802U");
//    TTTC0802U : 주식 현금 매수 주문
//    TTTC0801U : 주식 현금 매도 주문

    //BODY 셋팅
    Map<String, String> requestBodyMap = new HashMap<>();
    requestBodyMap.put("CANO", account); //계좌
    requestBodyMap.put("ACNT_PRDT_CD", "01"); //계좌
    requestBodyMap.put("PDNO", pdno);
    requestBodyMap.put("ORD_DVSN", "01"); //시장가
    requestBodyMap.put("ORD_QTY", String.valueOf(ordQty)); // 주문수량
    requestBodyMap.put("ORD_UNPR", "0"); // 시장가는 0으로 셋팅

    ResponseEntity<Map<String, Object>> response = null;

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBody = objectMapper.writeValueAsString(requestBodyMap);
      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

      URI url = UriComponentsBuilder.fromUriString(apiUrl)
          .path("uapi/domestic-stock/v1/trading/order-cash").build().toUri();

      response = restTemplate.exchange(url, HttpMethod.POST,
          request, new ParameterizedTypeReference<>() {
          });

      if (response.getStatusCode().is2xxSuccessful()) {
        objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        String retCode = jsonNode.get("rt_cd").asText();
        if ("0".equals(retCode)) {
          JsonNode outputNode = jsonNode.get("output");
          String KRX_FWDG_ORD_ORGNO = outputNode.get("KRX_FWDG_ORD_ORGNO").asText();
          String odno = outputNode.get("ODNO").asText();

//* 주문번호 유일조건: ord_dt(주문일자) + ord_gno_brno(주문채번지점번호) + odno(주문번호)
          // 현재 날짜 가져오기
          LocalDate currentDate = LocalDate.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
          String formattedDate = currentDate.format(formatter);

          BuyMarketOrderDto buyMarketOrderDto = BuyMarketOrderDto.builder()
              .buyKey(formattedDate + "_" + KRX_FWDG_ORD_ORGNO + "_" + odno)
              .ticker(pdno)
              .qty(ordQty)
              .qtyHist(ordQty)
              .build();

          return buyMarketOrderDto;
        } else {
          log.error("[buyMarketOrder] 매수 주문 오류 retCode = {}", retCode);
          return new BuyMarketOrderDto();
        }
      } else {
        log.error("[buyMarketOrder] 매수 주문 오류 statusCode = {} response ={}",
            response.getStatusCode(),
            response.getBody());
        return new BuyMarketOrderDto();
      }
    } catch (JsonProcessingException e) {
      log.error("[buyMarketOrder] JSON 파싱 에러 request = {} response = {}", requestBodyMap, response);
      return new BuyMarketOrderDto();
    }
  }


  /**
   * 지정가 매도주문
   *
   * @param order: 주문서
   * @return 매도 주문서
   */
  public SellLimitOrderDto sellLimitOrder(Order order) {

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "TTTC0801U");
//    TTTC0802U : 주식 현금 매수 주문
//    TTTC0801U : 주식 현금 매도 주문

    String buyPrice = String.valueOf(order.getBuyPrice());
    String sellRate = String.valueOf(order.getStock().getSellRate());
    BigDecimal roundedNumber = calculatePrice.getRoundedNumber(buyPrice, sellRate);

    //BODY 셋팅
    Map<String, Object> requestBodyMap = new HashMap<>();
    requestBodyMap.put("CANO", account); //계좌
    requestBodyMap.put("ACNT_PRDT_CD", "01"); //계좌
    requestBodyMap.put("PDNO", order.getStock().getTicker());
    requestBodyMap.put("ORD_DVSN", "00"); //지정가
    requestBodyMap.put("ORD_QTY", String.valueOf(order.getQty())); // 주문수량
    requestBodyMap.put("ORD_UNPR", String.valueOf(roundedNumber)); // 단가

    ResponseEntity<Map<String, Object>> response = null;

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBody = objectMapper.writeValueAsString(requestBodyMap);

      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

      URI url = UriComponentsBuilder.fromUriString(apiUrl)
          .path("uapi/domestic-stock/v1/trading/order-cash").build().toUri();

      response = restTemplate.exchange(url, HttpMethod.POST,
          request, new ParameterizedTypeReference<>() {
          });

      if (response.getStatusCode().is2xxSuccessful()) {
        objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        String retCode = jsonNode.get("rt_cd").asText();
        if ("0".equals(retCode)) {
          JsonNode outputNode = jsonNode.get("output");
          String KRX_FWDG_ORD_ORGNO = outputNode.get("KRX_FWDG_ORD_ORGNO").asText();
          String odno = outputNode.get("ODNO").asText();

          // 현재 날짜 가져오기
          LocalDate currentDate = LocalDate.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
          String formattedDate = currentDate.format(formatter);

          SellLimitOrderDto sellLimitOrderDto = SellLimitOrderDto.builder()
              .buyKey(order.getBuyKey())
              .sellKey(formattedDate + "_" + KRX_FWDG_ORD_ORGNO + "_" + odno)
              .sellPrice(roundedNumber.intValue())
              .build();

          return sellLimitOrderDto;
        } else {
          log.error("[sellLimitOrder] 매도 주문 오류 response ={}", response.getBody());
          return new SellLimitOrderDto();
        }
      } else {
        log.error("[sellLimitOrder] 매도 주문 오류 statusCode = {} response ={}",
            response.getStatusCode(),
            response.getBody());
        return new SellLimitOrderDto();
      }
    } catch (JsonProcessingException e) {
      log.error("[sellLimitOrder] JSON 파싱 에러 request = {} response = {}", requestBodyMap, response);
      return new SellLimitOrderDto();
    }
  }

  /**
   * 매수 주문이 체결되었는지 확인
   *
   * @param buyOrder 매수 주문서
   * @return 매수 완료 주문서
   */
  public CheckBuyOrderDto checkBuyOrder(Order buyOrder) {

    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String formattedDate = currentDate.format(formatter);

    String pdno = buyOrder.getStock().getTicker();

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "TTTC8001R");

    //BODY 셋팅
    HttpEntity request = new HttpEntity(headers);

    URI url = UriComponentsBuilder.fromUriString(apiUrl)
        .path("uapi/domestic-stock/v1/trading/inquire-daily-ccld").queryParam("CANO", account)
        .queryParam("ACNT_PRDT_CD", "01").queryParam("INQR_STRT_DT", formattedDate)
        .queryParam("INQR_END_DT", "99991231").queryParam("SLL_BUY_DVSN_CD", "02")
        .queryParam("INQR_DVSN", "01").queryParam("PDNO", pdno).queryParam("CCLD_DVSN", "00")
        .queryParam("ORD_GNO_BRNO", "").queryParam("ODNO", "").queryParam("INQR_DVSN_3", "01")
        .queryParam("INQR_DVSN_1", "").queryParam("CTX_AREA_FK100", "")
        .queryParam("CTX_AREA_NK100", "")
        .build().toUri();

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
        request, new ParameterizedTypeReference<>() {
        });

    if (response.getStatusCode().is2xxSuccessful()) {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        JsonNode output1Array = jsonNode.path("output1");

        Map<String, Object> map = new HashMap<>();
        CheckBuyOrderDto checkBuyOrderDto = new CheckBuyOrderDto();

        for (JsonNode item : output1Array) {
          String orderDt = item.path("ord_dt").asText();
          String brno = item.path("ord_gno_brno").asText();
          String odno = item.path("odno").asText();
          String buyUuid = orderDt + "_" + brno + "_" + odno;

          int totalPrice = output1Array.get(0).path("tot_ccld_amt").asInt();
          int buyPrice = output1Array.get(0).path("avg_prvs").asInt();

          if (buyUuid.equals(buyOrder.getBuyKey())) {
            checkBuyOrderDto = CheckBuyOrderDto.builder()
                .buyPrice(buyPrice)
                .totalPrice(totalPrice)
                .buyKey(buyUuid)
                .build();
          } else {
            continue;
          }
        }
        return checkBuyOrderDto;
      } catch (JsonProcessingException e) {
        log.error("[checkBuyOrder] JSON 파싱 에러 response.getBody() = {}", response.getBody());
        return new CheckBuyOrderDto();
      }

    } else {
      return new CheckBuyOrderDto();
    }
  }

  /**
   * 매도 주문이 체결되었는지 확인
   *
   * @param stock 주식 정보
   * @return 매도 체크 완료한 주문서
   */
  public List<CheckSellOrderDto> checkSellOrder(Stock stock) {

    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String formattedDate = currentDate.format(formatter);

    String pdno = String.valueOf(stock.getTicker());

    ///헤더 셋팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("authorization", "Bearer " + accessToken);
    headers.set("appkey", appKey);
    headers.set("appsecret", appSecretKey);
    headers.set("tr_id", "TTTC8001R");

    //BODY 셋팅
    HttpEntity request = new HttpEntity(headers);

    URI url = UriComponentsBuilder.fromUriString(apiUrl)
        .path("uapi/domestic-stock/v1/trading/inquire-daily-ccld").queryParam("CANO", account)
        .queryParam("ACNT_PRDT_CD", "01").queryParam("INQR_STRT_DT", formattedDate)
        .queryParam("INQR_END_DT", "99991231").queryParam("SLL_BUY_DVSN_CD", "01")
        .queryParam("INQR_DVSN", "01").queryParam("PDNO", pdno).queryParam("CCLD_DVSN", "00")
        .queryParam("ORD_GNO_BRNO", "").queryParam("ODNO", "").queryParam("INQR_DVSN_3", "01")
        .queryParam("INQR_DVSN_1", "").queryParam("CTX_AREA_FK100", "")
        .queryParam("CTX_AREA_NK100", "")
        .build().toUri();

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
        request, new ParameterizedTypeReference<Map<String, Object>>() {
        });

    if (response.getStatusCode().is2xxSuccessful()) {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
            objectMapper.writeValueAsString(response.getBody()));
        JsonNode output1Array = jsonNode.path("output1");

        List<CheckSellOrderDto> checkSellOrderDtos = new ArrayList<>();
        for (JsonNode item : output1Array) {
          CheckSellOrderDto checkSellOrderDto = new CheckSellOrderDto();
          String orderDt = item.path("ord_dt").asText();
          String brno = item.path("ord_gno_brno").asText();
          String odno = item.path("odno").asText();

          String sellUuid = orderDt + "_" + brno + "_" + odno;
          Integer sellQty = Integer.parseInt(item.path("tot_ccld_qty").asText()); //총체결수량

          checkSellOrderDto.setSellKey(sellUuid);
          checkSellOrderDto.setSellQty(sellQty);
          checkSellOrderDtos.add(checkSellOrderDto);
        }

        return checkSellOrderDtos;

      } catch (JsonProcessingException e) {
        log.error("[checkSellOrder] JSON 파싱 에러 response.getBody() = {}", response.getBody());
        return new ArrayList<>();
      }
    } else {
      log.error("[checkSellOrder] 상태코드 확인 statusCode = {} response ={}", response.getStatusCode(),
          response.getBody());
      return new ArrayList<>();
    }

  }

}
