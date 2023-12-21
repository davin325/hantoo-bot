package toyproject.hantoobot.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import toyproject.hantoobot.model.entity.KeyValueStore;

@SpringBootTest
@Transactional
@Rollback(value = false)
class KeyValueStoreRepositoryTest {

  @Autowired
  private KeyValueStoreRepository keyValueStoreRepository;

  @PersistenceContext
  EntityManager em;

  @Test
  public void test() {
    KeyValueStore test = new KeyValueStore("test33", "123");
    keyValueStoreRepository.save(test);

    List<KeyValueStore> all = keyValueStoreRepository.findAll();

    Assertions.assertThat(all.size()).isEqualTo(3);
  }

  @Test
  public void findByItemKey() {
    KeyValueStore hantooApiUrl = keyValueStoreRepository.findByItemKey("hantooApiUrl");
    System.out.println("hantooApiUrl.getItemKey() = " + hantooApiUrl.getItemKey());
    System.out.println("hantooApiUrl.getItemValue() = " + hantooApiUrl.getItemValue());
  }

}