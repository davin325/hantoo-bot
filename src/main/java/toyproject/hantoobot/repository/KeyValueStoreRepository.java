package toyproject.hantoobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import toyproject.hantoobot.model.entity.KeyValueStore;

public interface KeyValueStoreRepository extends JpaRepository<KeyValueStore,Long> {

  KeyValueStore findByItemKey(@Param("item_key") String itemKey);

}
