package com.mambu.apisdk.exception;

/**
 * Encapsulation for exceptions which may occur when calling Mambu APIs
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
		errorCode = -1;
		errorMessage = "";

		// preserve the original's exception class
		String classNameMessage = getExceptionClassName(e);

		if (e.getMessage() != null)
			errorMessage = classNameMessage + ", " + e.getMessage();
		else
			errorMessage = classNameMessage;
	}

	private String getExceptionClassName(Exception e) {

		String className = new String();
		String classNameMessage = new String("");

		Class eClass = e.getClass();

		if (eClass != null) {

			className = eClass.getSimpleName();
			String words[] = className.split("(?=[A-Z])"); // Split Name by Upper Case for readability

			// put the Name back together, now with spaces between words
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				if (i > 0 && word.length() > 1)
					classNameMessage = classNameMessage.concat(" ");
				classNameMessage = classNameMessage.concat(word);
			}
		}

		return classNameMessage.trim();
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

	@Override
	public String getMessage() {
		return getErrorMessage();

	}

}
