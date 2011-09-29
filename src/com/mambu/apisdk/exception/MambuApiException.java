package com.mambu.apisdk.exception;

/**
 * Encapculation for exceptions which may occur when calling Mambu APIs
 * 
 * @author edanilkis
 * 
 */
public class MambuApiException extends Exception {

	private static final long serialVersionUID = 1L;

	
	Integer errorCode;
	String errorMessage;

	public MambuApiException(Exception e) {
		super(e);
	}
	
	public MambuApiException(Integer errorCode, String errorMessage) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Integer getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


}
