package toyproject.hantoobot.utill;

import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(value = false)
class AesUtillTest {

  @Autowired
  private AesUtill aesUtill;

  @Test
  public void aesTest() throws Exception {
    String plainText = "lunaiscat";
    String aesCBCEncode = aesUtill.aesCBCEncode(plainText);
    String aesCBCDecode = aesUtill.aesCBCDecode(aesCBCEncode);

    Assertions.assertThat(plainText).isEqualTo(aesCBCDecode);


  }

}