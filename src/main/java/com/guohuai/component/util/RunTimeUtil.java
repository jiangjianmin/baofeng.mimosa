package com.guohuai.component.util;

import java.io.IOException;
import java.io.InputStream;

public class RunTimeUtil {
	
	
	public static boolean run(String shell) throws Exception {
		Process process = Runtime.getRuntime().exec(shell);
		InputStream normalIs = process.getInputStream();
		InputStream errorIs = process.getErrorStream();
		new RunTimeUtil().printIs(normalIs);
		new RunTimeUtil().printIs(errorIs);
		if (process.waitFor() == 0) {
			process.destroy();
			return true;
		}
		
		return false;
	}
	
	public  void printIs(InputStream is)  throws Exception {
		byte[] buffer = new byte[2048];
		int length;
		try {
			while (-1 != (length = is.read(buffer, 0, buffer.length))) {
				System.out.println(new String(buffer, 0, length));
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != is) {
				is.close();
			}
		}
	}
}
