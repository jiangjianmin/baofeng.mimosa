package com.guohuai.component.web.view;

import org.springframework.validation.BindException;

import com.guohuai.component.exception.AMPException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResp {

	private int errorCode;
	private String errorMessage;

	public void error(Throwable error) {
		if (error instanceof AMPException) {
			AMPException e = (AMPException) error;
			this.errorCode = e.getCode() == 0 ? -1 : e.getCode();
			this.errorMessage = e.getMessage();
		} else if (error instanceof BindException) {
			BindException be = (BindException) error;
			String msg = be.getBindingResult().getAllErrors().get(0).getDefaultMessage();
			this.errorCode = -1;
			this.errorMessage = msg;
		} else {
			this.errorCode = -1;
			this.errorMessage = error.getMessage();
		}
	}

}
