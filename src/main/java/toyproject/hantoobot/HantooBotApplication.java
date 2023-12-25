package toyproject.hantoobot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import toyproject.hantoobot.api.HanTooApi;


@RequiredArgsConstructor
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class HantooBotApplication {

  private final HanTooApi hanTooApi;

  @PostConstruct
  public void init() throws Exception {
    hanTooApi.initSetting();
    hanTooApi.getAuthorization();
  }

  public static void main(String[] args) {
    SpringApplication.run(HantooBotApplication.class, args);
  }

}
