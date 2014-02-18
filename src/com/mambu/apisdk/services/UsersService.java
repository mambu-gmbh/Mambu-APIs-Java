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
import com.mambu.core.shared.model.User;

/**
 * Service class which handles API operations like getting and creating users. When getting users, for safety reasons
 * the API call will have the response stripped of some fields: - transactionLimits - password - apiAppId - apiAppKey -
 * preferences - permissions
 * 
 * @author ipenciuc
 * 
 */
public class UsersService {

	private MambuAPIService mambuAPIService;

	private static String USERS = APIData.USERS;

	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;
	private static String BRANCH_ID = APIData.BRANCH_ID;
	private static String FULL_DETAILS = APIData.FULL_DETAILS;

	/***
	 * Create a new users service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public UsersService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/**
	 * Get all the users with offset and limit
	 * 
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return List of all Users
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String offset, String limit) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(USERS));

		ParamsMap params = new ParamsMap();

		params.put(OFFSET, offset);
		params.put(LIMIT, limit);
		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<User>>() {}.getType();

		List<User> users = (List<User>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return users;

	}

	/**
	 * Get users (first 50 per default)
	 * 
	 * @return List of Users
	 * 
	 * @throws MambuApiException
	 */
	public List<User> getUsers() throws MambuApiException {
		List<User> users = getUsers(null, null);
		return users;
	}

	/**
	 * Get a paginated list of users filtered by branch
	 * 
	 * @param branchId
	 *            the id of the branch to filter with
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return list of Users
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String branchId, String offset, String limit) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(USERS));

		ParamsMap params = new ParamsMap();
		params.put(BRANCH_ID, branchId);
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<User>>() {}.getType();

		List<User> users = (List<User>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return users;
	}

	/**
	 * Get User by it's userID
	 * 
	 * @param userId
	 *            the id of the user to filter.
	 * 
	 * @return User - with full details
	 * 
	 * @throws MambuApiException
	 */

	public User getUserById(String userId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(USERS) + "/" + userId);
		ParamsMap params = new ParamsMap();

		params.put(FULL_DETAILS, "true");
		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		User user = GsonUtils.createGson().fromJson(jsonResponse, User.class);

		return user;
	}

	/**
	 * Get User by it's userName.
	 * 
	 * NOTE: This is just a convenience method, it uses the getById() API. One can use getById() directly too.
	 * 
	 * @param userName
	 *            the username of the user to filter
	 * 
	 * @return User - with full details
	 * 
	 * @throws MambuApiException
	 */

	public User getUserByUsername(String userName) throws MambuApiException {

		User user = getUserById(userName);

		return user;
	}

}
