package toyproject.hantoobot.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyValueStore extends BaseTimeEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "key_value_store_id")
  private Long id;

  @Column(unique = true)
  private String itemKey;

  private String itemValue;

  public KeyValueStore(String itemKey, String itemValue) {
    this.itemKey = itemKey;
    this.itemValue = itemValue;
  }
}
