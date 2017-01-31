package com.mambu.apisdk.model;

import java.util.List;

/**
 * Helper model used to wrap the data needed by the notification messages API while POSTing failed messages to be
 * resent, see NotificationsService.
 * 
 * @author acostros
 *
 */
public class NotificationsToBeResent {

	private String action;

	private List<String> identifiers;

	public NotificationsToBeResent(String action, List<String> identifiers) {
		this.action = action;
		this.identifiers = identifiers;
	}

	public String getAction() {

		return action;
	}

	public void setAction(String action) {

		this.action = action;
	}

	public List<String> getIdentifiers() {

		return identifiers;
	}

	public void setIdentifiers(List<String> identifiers) {

		this.identifiers = identifiers;
	}

}
