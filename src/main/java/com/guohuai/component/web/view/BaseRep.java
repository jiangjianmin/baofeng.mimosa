package com.guohuai.component.web.view;
import org.springframework.validation.BindException;

import com.guohuai.component.exception.AMPException;

import lombok.NoArgsConstructor;
@lombok.Data
@NoArgsConstructor
public class BaseRep {
    
    public static final int ERROR_CODE = -1;
    
    /**
     * 错误代码
     */
    int errorCode;
    /**
     * 错误消息
     */
    String errorMessage;
    
    public BaseRep error(Throwable error) {
        if (error instanceof AMPException) {
            AMPException me = (AMPException) error;
            this.errorCode=me.getCode();
            this.errorMessage=me.getMessage();
        } else if (error instanceof BindException) {
            BindException be = (BindException) error;
            String msg = be.getBindingResult().getAllErrors().get(0).getDefaultMessage();
            this.errorCode=-1;
            this.errorMessage=msg;
        } else {
            this.errorCode=-1;
            this.errorMessage=error.getMessage();
        }
        return this;
    }
    
    
}