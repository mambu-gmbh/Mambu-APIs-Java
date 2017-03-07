package com.mambu.apisdk.services;

import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.api.server.handler.customviews.model.ApiViewType;
import com.mambu.api.server.handler.users.model.JSONUser;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.UserBranchAssignmentType;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.core.shared.data.DataViewType;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.Role;
import com.mambu.core.shared.model.User;

/**
 * Service class which handles API operations like getting and creating users. When getting users, for safety reasons
 * the API call will have the response stripped of some fields: password - apiAppId - apiAppKey
 * 
 * @author ipenciuc
 * 
 */
public class UsersService {

	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;
	private static String BRANCH_ID = APIData.BRANCH_ID;
	private static String BRANCH_ID_TYPE = APIData.BRANCH_ID_TYPE;

	// Service Executor
	private ServiceExecutor serviceExecutor;
	// API definitions
	private final static ApiDefinition getUsers = new ApiDefinition(ApiType.GET_LIST, User.class);
	private final static ApiDefinition getUser = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, User.class);
	private final static ApiDefinition getCustomViews = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, User.class,
			CustomView.class);
	private final static ApiDefinition getUserRoles = new ApiDefinition(ApiType.GET_LIST, Role.class);
	private final static ApiDefinition getUserRole = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, Role.class);
	
	private final static ApiDefinition createUser = new ApiDefinition(ApiType.CREATE_JSON_ENTITY, JSONUser.class, JSONUser.class);

	/***
	 * Create a new users service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public UsersService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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
	public List<User> getUsers(String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		return serviceExecutor.execute(getUsers, params);
	}

	/**
	 * Get users (first 50 per default)
	 * 
	 * @return List of Users
	 * 
	 * @throws MambuApiException
	 */
	public List<User> getUsers() throws MambuApiException {
		return getUsers(null, null);
	}

	/**
	 * Get a paginated list of users. API allows getting all users, or users who are assigned to a branch or users who
	 * are assigned or allowed to manage the branch
	 * 
	 * @param branchId
	 *            the id of the branch. If null, users for all branches are returned
	 * @param userBranchAssignmentType
	 *            user's branch assignment type. This parameter is ignored if the branchId parameter is null. If
	 *            userBranchAssignmentType is ASSIGNED then only users assigned to the specified branch are returned. If
	 *            its value is MANAGE then all users who are either assigned or who can manage the branch are returned.
	 *            If null, the default is ASSIGNED.
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return list of Users
	 * 
	 * @throws MambuApiException
	 */
	public List<User> getUsers(String branchId, UserBranchAssignmentType userBranchAssignmentType, String offset,
			String limit) throws MambuApiException {
		// Examples:
		// GET api/users?offset="0"&limit="100" # get all users
		// GET api/users?branchId=b1 # get assigned users for a branch
		// Note: branchIdType parameter is available since Mambu 4.0. See MBU-11769
		// GET api/users?branchId=b1&branchIdType=MANAGE" or GET api/users?branchId=b1&branchIdType=ASSIGNED"

		ParamsMap params = new ParamsMap();
		params.put(BRANCH_ID, branchId);
		// Add UserBranchAssignmentType. Applicable only if branchId is not null
		if (branchId != null && userBranchAssignmentType != null) {
			params.put(BRANCH_ID_TYPE, userBranchAssignmentType.name());
		}
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		return serviceExecutor.execute(getUsers, params);
	}

	/**
	 * Convenience method to Get a paginated list of users assigned to a branch, filtered by branch
	 * 
	 * @param branchId
	 *            the id of the branch to filter with. If null, all users are returned
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return list of assigned Users if branch id is not null. Otherwise, all users are returned
	 * 
	 * @throws MambuApiException
	 */
	public List<User> getUsers(String branchId, String offset, String limit) throws MambuApiException {
		// Return users assigned to a branch
		return getUsers(branchId, null, offset, limit);
	}

	/**
	 * Get User by its userID
	 * 
	 * @param userId
	 *            the id of the user to filter.
	 * 
	 * @return User - with full details
	 * 
	 * @throws MambuApiException
	 */

	public User getUserById(String userId) throws MambuApiException {
		return serviceExecutor.execute(getUser, userId);
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
		return getUserById(userName);
	}

	/**
	 * Get Custom Views for the user by user's userName and apiViewType.
	 * 
	 * See more in  @see <a href="https://mambucom.jira.com/browse/MBU-4607">MBU-4607</a> and in 
	 * @see <a href="https://mambucom.jira.com/browse/MBU-6306">MBU-6306</a>
	 * 
	 * @param username
	 *            the user name of the user. Mandatory field
	 * @param apiViewType
	 *            view filter type. If null, all custom views are returned
	 * 
	 * @return List of Custom Views for this user
	 * 
	 * @throws MambuApiException
	 */
	public List<CustomView> getCustomViews(String username, ApiViewType apiViewType) throws MambuApiException {
		// GET /api/users/<USERNAME>/views
		// Allow also for filtering for the CLIENTS/GROUPS/LOANS/DEPOSITS views (ex: GET
		// /api/users/<USERNAME>/views?for=CLIENTS)

		if (username == null) {
			throw new IllegalArgumentException("Username must not be NULL");
		}

		ParamsMap params = new ParamsMap();
		if (apiViewType != null) {
			params.put(APIData.FOR, apiViewType.name());
		}

		return serviceExecutor.execute(getCustomViews, username, params);
	}

	/**
	 * Convenience method to get all Custom Views for the user by user's userName.
	 * 
	 * @param userName
	 *            the username of the user
	 * 
	 * @return List of Custom Views for this user
	 * 
	 * @throws MambuApiException
	 */
	public List<CustomView> getCustomViews(String userName) throws MambuApiException {
		ApiViewType apiViewType = null;
		return getCustomViews(userName, apiViewType);
	}

	/**
	 * Map to convert custom view's DataViewType to the ApiViewType (required by Custom View API)
	 */
	public static HashMap<DataViewType, ApiViewType> supportedDataViewTypes;
	static {
		supportedDataViewTypes = new HashMap<DataViewType, ApiViewType>();

		supportedDataViewTypes.put(DataViewType.CLIENT, ApiViewType.CLIENTS);
		supportedDataViewTypes.put(DataViewType.GROUP, ApiViewType.GROUPS);

		supportedDataViewTypes.put(DataViewType.LOANS, ApiViewType.LOANS);
		supportedDataViewTypes.put(DataViewType.SAVINGS, ApiViewType.DEPOSITS);

		supportedDataViewTypes.put(DataViewType.LOAN_TRANSACTIONS_LOOKUP, ApiViewType.LOAN_TRANSACTIONS);
		supportedDataViewTypes.put(DataViewType.SAVINGS_TRANSACTIONS_LOOKUP, ApiViewType.DEPOSIT_TRANSACTIONS);

		supportedDataViewTypes.put(DataViewType.ACTIVITIES_LOOKUP, ApiViewType.SYSTEM_ACTIVITIES);
		//Added in 4.5
		supportedDataViewTypes.put(DataViewType.LINE_OF_CREDIT, ApiViewType.LINES_OF_CREDIT);

	}

	/**
	 * Helper to get ApiViewType for a DataViewType
	 * 
	 * @param dataViewType
	 *            data view type
	 * @return Api View Type
	 */
	public static ApiViewType getApiViewType(DataViewType dataViewType) {
		if (dataViewType == null || !supportedDataViewTypes.containsKey(dataViewType)) {
			return null;
		}

		return supportedDataViewTypes.get(dataViewType);
	}

	/**
	 * Get user roles
	 * 
	 * @return list of all User Roles
	 * 
	 * @throws MambuApiException
	 */
	public List<Role> getUserRoles() throws MambuApiException {
		// GET /api/userroles
		// See MBU-9263
		return serviceExecutor.execute(getUserRoles);
	}

	/**
	 * Get user role by a role key
	 * 
	 * @param roleKey
	 *            the encoded key of the user role
	 * @return user Role
	 * @throws MambuApiException
	 */
	public Role getUserRole(String roleKey) throws MambuApiException {
		// GET /api/userroles/{KEY}
		// See MBU-9263
		return serviceExecutor.execute(getUserRole, roleKey);
	}
	
	/**
	 * Creates a new user
	 * 
	 * i.e. API call POST api/users JSON payload: { "user": { "username": "tinker", "password": "pass1234566", "email":
	 * "ddd@mambu.com", "title": "Mrs", "firstName": "John"... } } 
	 * NOTE: for more information on users API visit
	 * 
	 * @see <a href="https://developer.mambu.com/customer/en/portal/articles/1162284-users-api?b_id=874">Users API</a>
	 * 
	 * @param user
	 *            The user to be created. Must be not be null. Also, take into account that the user must have a role
	 *            assigned.
	 *            
	 * @return newly created user
	 */
	public User createUser(User user) throws MambuApiException {

		// See MBU-15091
		if (user == null) {
			throw new IllegalArgumentException("User must not be null.");
		}

		JSONUser jsonUser = new JSONUser();
		jsonUser.setUser(user);
		JSONUser jsonCreatedUser = serviceExecutor.executeJson(createUser, jsonUser);
		return jsonCreatedUser.getUser();
	}

}
