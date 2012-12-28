/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;

/**
 * Service class which handles API operations like getting and creating clients and groups of clients
 * 
 * @author ipenciuc
 * 
 */
public class ClientsService {

	private MambuAPIService mambuAPIService;

	private static String CLIENTS = "clients";
	private static String GROUPS = "groups";

	private static String FIRST_NAME = "firstName";
	private static String LAST_NAME = "lastName";
	public static String HOME_PHONE = "homephone";
	public static String MOBILE_PHONE = "mobilephone";
	public static String GENDER = "gender";
	public static String BIRTH_DATE = "birthdate";
	public static String EMAIL_ADDRESS = "email";
	public static String NOTES = "notes";

	public static String ID_DOCUMENT = "idDocument";

	/***
	 * Create a new client service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public ClientsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/**
	 * Requests a client by their Mambu ID
	 * 
	 * @param accountId
	 * @return the Mambu client model
	 * @throws MambuApiException
	 */
	public Client getClient(String clientId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		Client clientResult = GsonUtils.createResponse().fromJson(jsonResposne, Client.class);

		return clientResult;

	}

	/**
	 * Requests a client by their Last name and first name
	 * 
	 * @param clientLastName
	 * @param clientFirstName
	 * @return list of Mambu clients
	 * @throws MambuApiException
	 */
	public List<Client> getClientByFullName(String clientLastName, String clientFirstName) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(FIRST_NAME, clientFirstName);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createResponse().fromJson(jsonResponse, collectionType);

		return clients;

	}
	/**
	 * Requests a client by their Last name and Birth date
	 * 
	 * @param clientLastName
	 * @param clientBirthday
	 *            ("yyyy-MM-dd")
	 * 
	 * @return list of Mambu clients
	 * @throws MambuApiException
	 */
	public List<Client> getClientByLastNameBirthday(String clientLastName, String birthDay) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(BIRTH_DATE, birthDay);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createResponse().fromJson(jsonResponse, collectionType);

		return clients;

	}

	/**
	 * Requests a client by their Document ID and Last name
	 * 
	 * @param clientLastName
	 * @param documentId
	 * @return the list of Mambu clients
	 * @throws MambuApiException
	 */
	public List<Client> getClientByLastNameDocId(String clientLastName, String documentId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		params.put(LAST_NAME, clientLastName);
		params.put(ID_DOCUMENT, documentId);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createResponse().fromJson(jsonResponse, collectionType);

		return clients;

	}

	/**
	 * Returns a client with their full details such as addresses or custom fields
	 * 
	 * @param clientId
	 *            the id of the client
	 * @return the retrieved expanded client
	 * 
	 * @throws MambuApiException
	 */
	public ClientExpanded getClientDetails(String clientId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId));
		ParamsMap params = new ParamsMap();
		params.put("fullDetails", "true");

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		ClientExpanded clientResult = GsonUtils.createResponse().fromJson(jsonResposne, ClientExpanded.class);

		return clientResult;

	}

	/**
	 * Requests a group by it's Mambu ID
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the retrieved group
	 * 
	 * @throws MambuApiException
	 */
	public Group getGroup(String groupId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		Group groupResult = GsonUtils.createResponse().fromJson(jsonResposne, Group.class);
		return groupResult;

	}

	/**
	 * Requests the details about a group
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the retrieved expanded group
	 * 
	 * @throws MambuApiException
	 */
	public GroupExpanded getGroupDetails(String groupId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		ParamsMap params = new ParamsMap();
		params.put("fullDetails", "true");

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		GroupExpanded groupResult = GsonUtils.createResponse().fromJson(jsonResposne, GroupExpanded.class);
		return groupResult;

	}
	/***
	 * Create a new client with only it's first name and last name
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @return
	 * @throws MambuApiException
	 */
	public Client createClient(String firstName, String lastName) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));
		ParamsMap params = new ParamsMap();
		params.put(FIRST_NAME, firstName);
		params.put(LAST_NAME, lastName);

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST);

		Client clientResult = GsonUtils.createResponse().fromJson(jsonResposne, Client.class);
		return clientResult;
	}

	/***
	 * Create a new client with full details
	 * 
	 * @param firstName
	 * @param lastName
	 * @param homephone
	 * @param mobilephone
	 * @param gender
	 * @param birthdate
	 * @param email
	 * @param notes
	 * @return created Mambu Client
	 * @throws MambuApiException
	 */
	public Client createClient(String firstName, String lastName, String homephone, String mobilephone, String gender,
			String birthdate, String email, String notes) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		ParamsMap params = new ParamsMap();
		params.put(FIRST_NAME, firstName);
		params.put(LAST_NAME, lastName);
		params.put(HOME_PHONE, homephone); // params.put("HOME_PHONE", homephone);
		params.put(MOBILE_PHONE, mobilephone);
		params.put(GENDER, gender);
		params.put(BIRTH_DATE, birthdate);
		params.put(EMAIL_ADDRESS, email);
		params.put(NOTES, notes); //

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST);
		Client clientResult = GsonUtils.createResponse().fromJson(jsonResposne, Client.class);

		return clientResult;
	}
}