package com.ucar.datalink.flinker.core.admin.record;

import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class Encryption {
	private static Logger logger = LoggerFactory.getLogger(Encryption.class);

	private static String strDefaultKey = "goodluck";

	private static Cipher encryptCipher = null;

	private static Cipher decryptCipher = null;

	static {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		try {
			Key key = getKey(strDefaultKey.getBytes("UTF8"));
			encryptCipher = Cipher.getInstance("DES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			decryptCipher = Cipher.getInstance("DES");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			logger.error("something goes wrong when init Cipher", e);
		}
	}

	public static String encrypt(String strMing) {
		try {
			return byteArr2HexStr(encrypt(strMing.getBytes("UTF8")));
		} catch (Exception e) {
			logger.error("something goes wrong when encrypt", e);
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(String strMi) {
		try {
			return new String(decrypt(hexStr2ByteArr(strMi)), "UTF8");
		} catch (Exception e) {
			logger.error("something goes wrong when decrypt", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[]
	 * hexStr2ByteArr(String strIn) 互为可逆的转换过程
	 * 
	 * 
	 * @param arrB
	 *            需要转换的byte数组
	 * 
	 * @return 转换后的字符串
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	private static String byteArr2HexStr(byte[] arrB) throws Exception {
		int iLen = arrB.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍

		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**
	 * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB)
	 * 互为可逆的转换过程
	 * 
	 * 
	 * @param strIn
	 *            需要转换的字符串
	 * @return 转换后的byte数组
	 * 
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	private static byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arrB = strIn.getBytes("UTF8");
		int iLen = arrB.length;

		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2,"UTF8");
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 加密字节数组
	 * 
	 * @param arrB
	 *            需加密的字节数组
	 * 
	 * @return 加密后的字节数组
	 * 
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	}

	/**
	 * 解密字节数组
	 * 
	 * 
	 * @param arrB
	 *            需解密的字节数组
	 * 
	 * @return 解密后的字节数组
	 * 
	 * @throws Exception
	 */
	private static byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}

	/**
	 * 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位
	 * 
	 * @param arrBTmp
	 *            构成该字符串的字节数组
	 * 
	 * @return 生成的密钥
	 * @throws java.lang.Exception
	 */
	private static Key getKey(byte[] arrBTmp) throws Exception {
		// 创建一个空的8位字节数组（默认值为0）
		byte[] arrB = new byte[8];

		// 将原始字节数组转换为8位
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}

		// 生成密钥
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");
		return key;
	}

	public static void main(String[] args) throws Exception {
		String str1 = "canal";

		// DES加密
		String str2 = encrypt(str1);
		String deStr = decrypt(str2);

		System.out.println("密文:" + str2);
		System.out.println("明文:" + deStr);

	}
}