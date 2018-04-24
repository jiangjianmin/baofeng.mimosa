package com.guohuai.component.util;

import com.guohuai.basic.component.exception.GHException;

public class PwdUtil {
	
	/**
	 * encrypt password
	 * 
	 * @param password
	 *            raw password
	 * @param salt
	 *            encrypted key
	 * @return
	 */
	public static String encryptPassword(String password, String salt) {
		byte[] saltb = Digests.decodeHex(salt);
		byte[] pwdb = Digests.sha1(password.getBytes(Digests.UTF8), saltb);
		String pwd = Digests.encodeHex(pwdb);
		return pwd;
	}

	/**
	 * check user's password
	 * 
	 * @param src
	 *            raw password
	 * @param target
	 *            encrypted password
	 * @param salthex
	 *            encrypted key
	 * @return {@code true} valid, {@code false} invalid
	 */
	public static boolean checkPassword(String src, String target, String salthex) {
		if (target == null || salthex == null) {
			return false;
		}
		byte[] salt = Digests.decodeHex(salthex);
		byte[] password = Digests.sha1(src.getBytes(Digests.UTF8), salt);
		String pwd = Digests.encodeHex(password);
		if (pwd.equals(target)) {
			return true;
		}

		return false;
	}
	
	/**
	 * encrypt password
	 * 
	 * @param password
	 *            raw password
	 * @param salt
	 *            encrypted key
	 * @return
	 */
	public static String encryptPassword1(String password, String salt) {
		byte[] saltb = salt.getBytes(Digests.UTF8);
		byte[] pwdb = Digests.sha1(password.getBytes(Digests.UTF8), saltb);
		String pwd = Digests.encodeHex(pwdb);
		return pwd;
	}

	/**
	 * check user's password
	 * 
	 * @param src
	 *            raw password
	 * @param target
	 *            encrypted password
	 * @param salthex
	 *            encrypted key
	 * @return {@code true} valid, {@code false} invalid
	 */
	public static boolean checkPassword1(String src, String target, String salt) {
		if (target == null || salt == null) {
			throw new GHException("用户密码不正确！");
		}
		byte[] s = salt.getBytes(Digests.UTF8);
		byte[] password = Digests.sha1(src.getBytes(Digests.UTF8), s);
		String pwd = Digests.encodeHex(password);
		if (pwd.equals(target)) {
			return true;
		}

		return false;
	}
}
