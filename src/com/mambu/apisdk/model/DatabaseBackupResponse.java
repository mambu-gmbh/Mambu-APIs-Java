package com.mambu.apisdk.model;

/**
 * Wrapper class used in the hold the response details for backup database API call.
 * 
 * @author acostros
 *
 */

public class DatabaseBackupResponse {

	String returnCode;
	String returnStatus;

	public String getReturnCode() {

		return returnCode;
	}

	public void setReturnCode(String returnCode) {

		this.returnCode = returnCode;
	}

	public String getReturnStatus() {

		return returnStatus;
	}

	public void setReturnStatus(String returnStatus) {

		this.returnStatus = returnStatus;
	}

	@Override
	public String toString() {

		return "DatabaseBackupResponseObject [returnCode=" + returnCode + ", returnStatus=" + returnStatus + "]";
	}

}
