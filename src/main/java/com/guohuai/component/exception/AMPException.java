package com.guohuai.component.exception;

import com.guohuai.basic.component.exception.GHException;
import com.guohuai.basic.config.ErrorDefineConfig;

public class AMPException extends GHException {
	public static final Integer CONCURRENT_CODE = 5678;

	private int code;

	private static final long serialVersionUID = -7344147416330238244L;

	public AMPException(int code, Object... args) {
		super(String.format(ErrorDefineConfig.define.get(code), args));
		this.code = code;
	}

	public AMPException(String message) {
		super(message);
		this.code = -1;
	}
	
	public AMPException(int code, String message) {
		super(message);
		this.code = code;
	}

	public AMPException(Throwable cause) {
		super(cause);
		this.code = -1;
	}

	public int getCode() {
		return this.code;
	}

	public static AMPException getException(int errorCode, Object... args) {
		if (ErrorDefineConfig.define.containsKey(errorCode)) {
			return new AMPException(errorCode, args);
		}
		return new AMPException(String.valueOf(errorCode));
	}

	public static AMPException getException(String errorMessage) {
		return new AMPException(errorMessage);
	}

	public static AMPException getException(Throwable error) {
		if (error instanceof AMPException) {
			return (AMPException) error;
		}
		return new AMPException(error);
	}
}
