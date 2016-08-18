package com.mambu.apisdk.model;

import java.io.ByteArrayOutputStream;

/**
 * Wrapper class, used to hold the content of a DB backup as a ByteArrayOutputStream.
 * 
 * @author acostros
 *
 */
public class DatabaseBackup {

	private ByteArrayOutputStream content;

	public ByteArrayOutputStream getContent() {

		return content;
	}

	public void setContent(ByteArrayOutputStream content) {

		this.content = content;
	}

}
