package toyproject.hantoobot.utill;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AesUtill {
  @Value("${aes256.key}")
  private String privateKey;


  public String aesCBCEncode(String plainText) throws Exception {

    SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes("UTF-8"), "AES");
    IvParameterSpec IV = new IvParameterSpec(privateKey.substring(0,16).getBytes());

    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

    c.init(Cipher.ENCRYPT_MODE, secretKey, IV);

    byte[] encrpytionByte = c.doFinal(plainText.getBytes("UTF-8"));

    return Hex.encodeHexString(encrpytionByte);
  }


  public String aesCBCDecode(String encodeText) throws Exception {

    SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes("UTF-8"), "AES");
    IvParameterSpec IV = new IvParameterSpec(privateKey.substring(0,16).getBytes());

    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

    c.init(Cipher.DECRYPT_MODE, secretKey, IV);

    byte[] decodeByte = Hex.decodeHex(encodeText.toCharArray());

    return new String(c.doFinal(decodeByte), "UTF-8");
  }
}
