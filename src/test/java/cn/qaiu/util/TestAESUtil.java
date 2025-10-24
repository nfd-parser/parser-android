package cn.qaiu.util;

import cn.qaiu.util.AESUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class TestAESUtil {

    // 1686215935703
    // B4C5B9833113ACA41F16AABADE17349C
    @Test
    public void decode() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        String hex = AESUtils.encryptBase64ByAES("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8asrfSaoOb4je+DSmKdriQJKW\n" +
                "VJ2oDZrs3wi5W67m3LwTB9QVR+cE3XWU21Nx+YBxS0yun8wDcjgQvYt625ZCcgin\n" +
                "2ro/eOkNyUOTBIbuj9CvMnhUYiR61lC1f1IGbrSYYimqBVSjpifVufxtx/I3exRe\n" +
                "ZosTByYp4Xwpb1+WAQIDAQAB", AESUtils.CIPHER_AES2);
        System.out.println(hex);
        Assert.assertEquals("B4C5B9833113ACA41F16AABADE17349C", hex.toUpperCase());
    }

    @Test
    public void encode() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException,
            NoSuchAlgorithmException, InvalidKeyException {
        String source = AESUtils.decryptByHexAES("B4C5B9833113ACA41F16AABADE17349C", AESUtils.CIPHER_AES2);
        Assert.assertEquals("1686215935703", source);
    }

    @Test
    public void toHex() {
        byte[] d234EF67A1s = HexFormat.of().parseHex("D234EF67A1");
        Assert.assertArrayEquals(new byte[]{(byte) 0xd2, (byte) 0x34, (byte) 0xef, (byte) 0x67, (byte) 0xa1},
                d234EF67A1s);
    }

    @Test
    public void base64AES() throws NoSuchAlgorithmException {
        System.out.println(HexFormat.of().formatHex(AESUtils.createKeyString(AESUtils.KEY_SIZE_128_LENGTH).getEncoded()));
        System.out.println(HexFormat.of().formatHex(AESUtils.createKeyString(AESUtils.KEY_SIZE_192_LENGTH).getEncoded()));
        System.out.println(HexFormat.of().formatHex(AESUtils.createKeyString(AESUtils.KEY_SIZE_256_LENGTH).getEncoded()));

        // TODO Base64-AES
    }

    @Test
    public void testIdDecode() {
        Assert.assertEquals(146731, AESUtils.idEncrypt("7jy0zlv"));
    }

    // 蓝奏优享
    @Test
    public void testIzIdDecode() {
        Assert.assertEquals(26216, AESUtils.idEncryptIz("lGFndCM"));
    }

    @Test
    public void test00() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException,
            NoSuchAlgorithmException, InvalidKeyException {
        System.out.println(AESUtils.decryptByBase64AES(AESUtils.CIPHER_AES2, AESUtils.CIPHER_AES));
    }

    @Test
    public void testTs() {
        System.out.println(System.currentTimeMillis());
    }

    @Test
    public void testRandom() {
        System.out.println(AESUtils.getRandomString());
        System.out.println(AESUtils.getRandomString());
        System.out.println(AESUtils.getRandomString());
        System.out.println(AESUtils.getRandomString());
    }

    @Test
    public void testKeyAuth(){
        System.out.println(AESUtils.getAuthKey("/a/api/share/download/info"));
        System.out.println(AESUtils.getAuthKey("/a/api/share/download/info"));
        System.out.println(AESUtils.getAuthKey("/b/api/share/get"));
    }


    @Test
    public void testAES2() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        System.out.println(AESUtils.encryptBase64ByAES("AAAAA", "123123"));
        System.out.println(AESUtils.encryptBase64ByAES("AAAAA", AESUtils.generateKey("123123")));
    }
}
