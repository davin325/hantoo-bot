package toyproject.hantoobot.controller;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import toyproject.hantoobot.model.entity.KeyValueStore;
import toyproject.hantoobot.repository.KeyValueStoreRepository;
import toyproject.hantoobot.utill.AesUtill;


@Slf4j
@RequiredArgsConstructor
@RestController
public class KeyValueStoreController {

  private final KeyValueStoreRepository keyValueStoreRepository;
  private final AesUtill aesUtill;

  @PostMapping("/api/key-value/new")
  public void newKeyValue(@RequestBody @Valid NewKeyValueRequest request) {
    keyValueStoreRepository.save(new KeyValueStore(request.getItemKey(), request.itemValue));

    log.info("등록 되었습니다. key = {}, value = {}",request.getItemKey(),request.getItemValue());
  }

  @PostMapping("/api/key-value-crypto/new")
  public void newKeyValueCrypto(@RequestBody @Valid NewKeyValueRequest request) throws Exception {
    String encodeData = aesUtill.aesCBCEncode(request.itemValue);
    keyValueStoreRepository.save(new KeyValueStore(request.getItemKey(),encodeData));

    log.info("등록 되었습니다. key = {}, value = {}",request.getItemKey(),encodeData);
  }

  @Data
  static class NewKeyValueRequest {
    private String itemKey;
    private String itemValue;
  }

}
