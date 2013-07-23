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
import com.mambu.apisdk.util.APIData;
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

	private static String CLIENTS = APIData.CLIENTS;
	private static String GROUPS = APIData.GROUPS;

	// Client search and create fields
	private static String FIRST_NAME = APIData.FIRST_NAME;
	private static String LAST_NAME = APIData.LAST_NAME;
	private static String HOME_PHONE = APIData.HOME_PHONE;
	private static String MOBILE_PHONE = APIData.MOBILE_PHONE;
	private static String GENDER = APIData.GENDER;
	private static String BIRTH_DATE = APIData.BIRTH_DATE;
	private static String EMAIL_ADDRESS = APIData.EMAIL_ADDRESS;
	private static String ID_DOCUMENT = APIData.ID_DOCUMENT;

	private static String NOTES = APIData.NOTES;

	private static final String BRANCH_ID = APIData.BRANCH_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String CLIENT_STATE = APIData.CLIENT_STATE;

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
	 * @param clientId
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
	 * @param birthDay
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
	 * Requests a list of clients
	 * 
	 * @param active True if active Clients should retrieved
	 * @return the list of Mambu clients
	 * @throws MambuApiException
	 */
	public List<Client> getClients(boolean active) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		params.addParam(CLIENT_STATE,(active ? "ACTIVE" : "INACTIVE"));
		
		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createResponse().fromJson(jsonResponse, collectionType);

		return clients;

	}

	/**
	 * Requests a list of clients
	 * 
	 * @param active True if active Clients should retrieved
	 * @param offset Offset to state loading Clients
	 * @param limit Limit of Clients to load   
	 * @return the list of Mambu clients
	 * @throws MambuApiException
	 */
	public List<Client> getClients(boolean active,int offset, int limit) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		params.addParam(CLIENT_STATE,(active ? "ACTIVE" : "INACTIVE"));
		params.addParam(APIData.OFFSET, String.valueOf(offset));
		params.addParam(APIData.LIMIT, String.valueOf(limit));
		
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
		params.put(APIData.FULL_DETAILS, "true");

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
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Group groupResult = GsonUtils.createResponse().fromJson(jsonResponse, Group.class);
		return groupResult;

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
	public Group getGroupList(String groupId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		// TODO: temp fix issue in 3.1 patch- it returns an array instead of a single json object

		// *** Temp
		Type collectionType = new TypeToken<List<Group>>() {}.getType();
		List<Group> groups = (List<Group>) GsonUtils.createResponse().fromJson(jsonResponse, collectionType);
		Group groupResult = null;
		if (groups != null && groups.size() > 0)
			groupResult = groups.get(0);
		// *** Temp end
		// Group groupResult = GsonUtils.createResponse().fromJson(jsonResponse, Group.class);
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
		params.put(APIData.FULL_DETAILS, "true");

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		GroupExpanded groupResult = GsonUtils.createResponse().fromJson(jsonResponse, GroupExpanded.class);
		return groupResult;

	}
	public GroupExpanded getGroupDetailsList(String groupId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		ParamsMap params = new ParamsMap();
		params.put(APIData.FULL_DETAILS, "true");

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);
		// TODO: temp fix issue in 3.1 patch- it returns an array instead of a single json object

		// *** Temp
		Type collectionType = new TypeToken<List<GroupExpanded>>() {}.getType();
		List<GroupExpanded> groups = (List<GroupExpanded>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);
		GroupExpanded groupResult = null;
		if (groups != null && groups.size() > 0)
			groupResult = groups.get(0);
		// *** Temp end
		// GroupExpanded groupResult = GsonUtils.createResponse().fromJson(jsonResposne, GroupExpanded.class);
		return groupResult;

	}
	/***
	 * Create a new client with only it's first name and last name
	 * 
	 * @param firstName
	 * @param lastName
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
	/***
	 * Get Clients by branch id, credit officer, clientState
	 * 
	 * @param branchId
	 *            the ID of the Client's branch
	 * @param creditOfficerUserName
	 *            - the username of the credit officer to whom the CLients are assigned to
	 * @param clientState
	 *            - the desired state of a Client to filter on (eg: ACTIVE) *
	 * @return the list of Clients matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<Client> getClientsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String clientState, String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(CLIENT_STATE, clientState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();

		List<Client> clients = (List<Client>) GsonUtils.createResponse().fromJson(jsonResponse, collectionType);
		return clients;
	}

	/***
	 * Get Groups by branch id, credit officer
	 * 
	 * @param branchId
	 *            the ID of the Group's branch
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the Groups are assigned to
	 * 
	 * @return the list of Groups matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<Group> getGroupsByBranchOfficer(String branchId, String creditOfficerUserName, String offset,
			String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Group>>() {}.getType();

		List<Group> groups = (List<Group>) GsonUtils.createResponse().fromJson(jsonResponse, collectionType);
		return groups;
	}
}