package toyproject.hantoobot.utill;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AesUtill {

  @Value("${aes256.key}")
  private String privateKey;

  /**
   * AES 암호화
   * @param plainText 평문
   * @return 암호화문
   */
  public String aesCBCEncode(String plainText) {
    try {
      SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes("UTF-8"), "AES");
      IvParameterSpec IV = new IvParameterSpec(privateKey.substring(0, 16).getBytes());

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);

      byte[] encrpytionByte = cipher.doFinal(plainText.getBytes("UTF-8"));

      return Hex.encodeHexString(encrpytionByte);
    } catch (UnsupportedEncodingException | GeneralSecurityException e) {
      throw new RuntimeException("AES 암호화 에러", e);
    }
  }

  /**
   * AES 복호화
   * @param encodeText 암호화문
   * @return 평문
   */
  public String aesCBCDecode(String encodeText) {
    try {
      SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes("UTF-8"), "AES");
      IvParameterSpec IV = new IvParameterSpec(privateKey.substring(0, 16).getBytes());

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

      cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);

      byte[] decodeByte = Hex.decodeHex(encodeText.toCharArray());

      return new String(cipher.doFinal(decodeByte), "UTF-8");
    } catch (UnsupportedEncodingException | GeneralSecurityException | DecoderException e) {
      throw new RuntimeException("AES 복호화 에러", e);
    }
  }
}
