package com.mambu.apisdk.model;

/**
 * Wrapper class used in the hold the request details for backup database API call.
 * 
 * @author acostros
 *
 */

public class DatabaseBackupRequest {

	private String callback;

	public String getCallback() {

		return callback;
	}

	public void setCallback(String callback) {

		this.callback = callback;
	}

}
