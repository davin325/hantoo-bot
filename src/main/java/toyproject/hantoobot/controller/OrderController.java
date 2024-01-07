package toyproject.hantoobot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import toyproject.hantoobot.service.OrderService;

@RequiredArgsConstructor
@RestController
public class OrderController {

  private final OrderService orderService;
  private boolean startFlag = false;

  @Scheduled(cron = "0 0/10 10-15 ? * MON-FRI")
  @GetMapping("/stock-order")
  public void order() {
    if (orderService.checkHoliday()) {
      return;
    }

    if (!startFlag) {
      orderService.sellBeforeStart();
      startFlag = true;
    }
    orderService.newOrder();
  }


  @Scheduled(cron = "0 0 17 ? * MON-FRI")
  @GetMapping("/stock-sell-order-init")
  public void sellOrderInit() {
    orderService.sellOrderInit();
    startFlag = false;
  }
}
