package toyproject.hantoobot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import toyproject.hantoobot.service.MergeService;

@RequiredArgsConstructor
@RestController
public class MergeController {
  private final MergeService mergeService;

  @Scheduled(cron = "0 0 17 ? * SUN")
  @GetMapping("/merge-order")
  public void mergeOrder() {
    mergeService.mergeOrder();
  }
}
