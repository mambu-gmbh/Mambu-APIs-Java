package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.core.shared.data.DataViewType;
import com.mambu.core.shared.model.CustomView;
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

	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;
	private static String BRANCH_ID = APIData.BRANCH_ID;

	// Service helper
	private ServiceHelper serviceHelper;
	// API definitions
	private final static ApiDefinition getUsers = new ApiDefinition(ApiType.GET_LIST, User.class);
	private final static ApiDefinition getUser = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, User.class);
	private final static ApiDefinition getCustomViews = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, User.class,
			CustomView.class);

	/***
	 * Create a new users service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public UsersService(MambuAPIService mambuAPIService) {
		this.serviceHelper = new ServiceHelper(mambuAPIService);
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

		return serviceHelper.execute(getUsers, params);
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
	public List<User> getUsers(String branchId, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(BRANCH_ID, branchId);
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		return serviceHelper.execute(getUsers, params);
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
		return serviceHelper.execute(getUser, userId);
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
	 * Get Custom Views for the user by user's userName.
	 * 
	 * @param userName
	 *            the username of the user
	 * @param dataType
	 *            view filter type. Allowed values are DataViewType.CLIENT, DataViewType.GROUP, DataViewType.LOANS,
	 *            DataViewType.SAVINGS. If dataType is null then all view types are returned
	 * 
	 * @return List of Custom Views for this user
	 * 
	 * @throws MambuApiException
	 */
	// TODO: to be tested with Mambu 3.7
	public List<CustomView> getCustomViews(String userName, DataViewType dataType) throws MambuApiException {
		// See MBU-4607 @ https://mambucom.jira.com/browse/MBU-4607
		// GET /api/users/<USERNAME>/views)

		// ## Allow for filtering for the CLIENT/GROUP/LOAN/DEPOSIT views (ex: GET
		// /api/users/<USERNAME>/views?for=CLIENT)

		ParamsMap params = null;

		// If dataType is null then all types of CustomViews are retrieved
		if (dataType != null) {
			// CustomViews can be requested by type only for Clients, Groups, Loans and Saving, see MBU-4607
			final List<DataViewType> allowedTypes = Arrays.asList(DataViewType.CLIENT, DataViewType.GROUP,
					DataViewType.LOANS, DataViewType.SAVINGS);

			if (!allowedTypes.contains(dataType)) {
				throw new IllegalArgumentException("View Type " + dataType.name() + " is not supported");
			}
			params = new ParamsMap();
			params.put(APIData.FOR, dataType.name());
		}

		return serviceHelper.execute(getCustomViews, userName, params);
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
	// TODO: to be tested with Mambu 3.7
	public List<CustomView> getCustomViews(String userName) throws MambuApiException {
		DataViewType dataType = null;
		return getCustomViews(userName, dataType);
	}
}
