package util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncodeAndDecode {
	private static final String CharsetName = "utf-8";
	public static String Key = "xuefengmusic-123";
	private static final String Transformation = "AES/CBC/PKCS5Padding";
	public static String decrypt(String paramString)

	{
		try {
			SecretKeySpec localSecretKeySpec = new SecretKeySpec(Key.getBytes(), "AES");
			Cipher localObject = Cipher.getInstance("AES/CBC/PKCS5Padding");

			byte[] arrayOfByte = new byte[16];
			arrayOfByte[0] = 0;
			arrayOfByte[1] = 0;
			arrayOfByte[2] = 0;
			arrayOfByte[3] = 0;
			arrayOfByte[4] = 0;
			arrayOfByte[5] = 0;
			arrayOfByte[6] = 0;
			arrayOfByte[7] = 0;
			arrayOfByte[8] = 0;
			arrayOfByte[9] = 0;
			arrayOfByte[10] = 0;
			arrayOfByte[11] = 0;
			arrayOfByte[12] = 0;
			arrayOfByte[13] = 0;
			arrayOfByte[14] = 0;
			arrayOfByte[15] = 0;
			SecretKeySpec keyspec = new SecretKeySpec(Key.getBytes(), "AES");// �����ܳ�
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(paramString);
			Cipher cipher = Cipher.getInstance(Transformation);// ����������
			IvParameterSpec ivspec = new IvParameterSpec(arrayOfByte);
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);// ��ʼ��
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "UTF-8");

			return originalString;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return "";
	}

	public static String encrypt(String paramString) throws Exception {
		Object localObject = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec localSecretKeySpec = new SecretKeySpec(Key.getBytes(), "AES");
		byte[] arrayOfByte = new byte[16];
		arrayOfByte[0] = 0;
		arrayOfByte[1] = 0;
		arrayOfByte[2] = 0;
		arrayOfByte[3] = 0;
		arrayOfByte[4] = 0;
		arrayOfByte[5] = 0;
		arrayOfByte[6] = 0;
		arrayOfByte[7] = 0;
		arrayOfByte[8] = 0;
		arrayOfByte[9] = 0;
		arrayOfByte[10] = 0;
		arrayOfByte[11] = 0;
		arrayOfByte[12] = 0;
		arrayOfByte[13] = 0;
		arrayOfByte[14] = 0;
		arrayOfByte[15] = 0;
		((Cipher) localObject).init(1, localSecretKeySpec, new IvParameterSpec(arrayOfByte));
		byte[] by = ((Cipher) localObject).doFinal(paramString.getBytes("utf-8"));
		return (String) new BASE64Encoder().encode(by);
	}

}