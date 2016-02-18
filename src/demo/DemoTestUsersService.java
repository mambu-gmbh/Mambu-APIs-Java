package demo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mambu.accounts.shared.model.TransactionLimitType;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.customviews.model.ApiViewType;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.CustomViewsService;
import com.mambu.apisdk.services.CustomViewsService.CustomViewResultType;
import com.mambu.apisdk.services.UsersService;
import com.mambu.apisdk.util.APIData.UserBranchAssignmentType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.data.DataItemType;
import com.mambu.core.shared.data.DataViewType;
import com.mambu.core.shared.model.ColumnConfiguration;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.FieldColumn;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.Permissions;
import com.mambu.core.shared.model.Permissions.Permission;
import com.mambu.core.shared.model.Role;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestUsersService {

	private static User demoUser;
	private static String BRANCH_ID;
	private static String USER_NAME;
	private static String methodName = null; // print method name

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			USER_NAME = demoUser.getUsername();
			BRANCH_ID = demoUser.getAssignedBranchKey();

			testGetAllUsers();

			testGetUsersByPage();

			testGetUserById();

			testGetPaginatedUsersByBranch();

			testGetUserByUsername();

			testGetCustomViewsByUsername();

			testGetCustomViewsByUsernameType();

			// Available since 3.8
			testUpdateDeleteCustomFields();

			List<Role> userRoles = testGetAllUserRoles(); // Available since 3.14
			testGetAllUserRoleDetails(userRoles); // Available since 3.14

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Users Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetAllUsers() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetAllUsers");
		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers();
		logUsers(users, methodName);
	}

	public static void testGetUsersByPage() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetUsersByPage");
		String offset = "0";
		String limit = "500";

		System.out.println("Offset=" + offset + " Limit=" + limit);
		UsersService usersService = MambuAPIFactory.getUsersService();
		List<User> users = usersService.getUsers(offset, limit);

		logUsers(users, methodName);
	}

	public static void testGetPaginatedUsersByBranch() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetPaginatedUsersByBranch");
		UsersService usersService = MambuAPIFactory.getUsersService();

		String offset = "0";
		String limit = "5";

		// Test getting all users
		List<User> allUsers = usersService.getUsers(null, offset, limit);
		logUsers(allUsers, methodName + ": All Users");

		// Test getting users for a demo branch ID
		String branchId = BRANCH_ID;

		// Test getting assigned users
		UserBranchAssignmentType usersType = UserBranchAssignmentType.ASSIGNED;
		List<User> assignedUsers = usersService.getUsers(branchId, usersType, offset, limit);
		logUsers(assignedUsers, methodName + ": Users Assigned to " + BRANCH_ID);

		// Test getting users managing the branch
		usersType = UserBranchAssignmentType.MANAGE;
		List<User> usersManagingBranch = usersService.getUsers(branchId, usersType, offset, limit);
		logUsers(usersManagingBranch, methodName + ": Users Managing Branch " + BRANCH_ID);
	}

	public static void testGetUserById() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetUserById");

		UsersService usersService = MambuAPIFactory.getUsersService();

		String userId = USER_NAME;

		System.out.println("UserId=" + userId);
		Date d1 = new Date();

		User user = usersService.getUserById(userId);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("testGetUserById OK, Total time=" + diff + "\nReturned user= " + user.getFirstName() + " "
				+ user.getLastName() + " Id=" + user.getId() + " Username=" + user.getUsername());

		// Verify that Permissions object is returned with API response. See MBU-4526 implemented in Mambu 3.4
		Permissions permissions = user.getPermissions();

		if (permissions == null) {
			System.out.println("testGetUserById. User has NULL permissions");
			return;
		}
		List<Permission> permissionList = permissions.getPermissions();
		HashSet<Permission> permissionsHashSet = permissions.getPermissionSet();
		HashMap<Permission, Boolean> permissionsMap = permissions.getPermissionsMap();

		System.out.println("User Permissions. List size=" + permissionList.size() + " HashSet="
				+ permissionsHashSet.size() + "  HashMap=" + permissionsMap.size());

		// Get "Can Manage" permissions
		boolean canManageAllBranches = user.canManageAllBranches();
		boolean canManageMultipleBranches = user.canManageMultipleBranches();
		boolean canManageOtherOfficers = user.canManageEntitiesAssignedToOtherCreditOfficers(); // since 3.9

		System.out.println("Can Manage All Branches=" + canManageAllBranches + "\t Can Manage Multiple Branches="
				+ canManageMultipleBranches + "\tCan Manage Other Officers=" + canManageOtherOfficers);

		// Verify Transaction Limits and User Role are returned (see MBU-7019)
		Role userRole = user.getRole();
		String userRoleKey = (userRole == null) ? null : userRole.getEncodedKey();
		HashMap<TransactionLimitType, Money> transactionLimits = user.getTransactionLimits();
		System.out.println("User Role Key=" + userRoleKey + "\tTransaction Limits=" + transactionLimits);
	}

	public static void testGetUserByUsername() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetUserByUsername");

		UsersService usersService = MambuAPIFactory.getUsersService();
		String username = USER_NAME;
		System.out.println("\nUsername =" + username);
		User user = usersService.getUserByUsername(username);

		System.out.println("Returned user= " + user.getFirstName() + " " + user.getLastName() + " Id=" + user.getId()
				+ " Username=" + user.getUsername());

	}

	// Get Custom Views. Available since Mambu 3.7
	public static void testGetCustomViewsByUsername() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetCustomViewsByUsername");

		UsersService usersService = MambuAPIFactory.getUsersService();

		String username = USER_NAME;
		System.out.println("Username =" + username);

		List<CustomView> views = usersService.getCustomViews(username);

		if (views == null) {
			System.out.println("Returned null views");
			return;
		}
		System.out.println("Returned views count= " + views.size());

		for (CustomView view : views) {
			// Log view details
			logCustomView(view);
		}

	}

	// getCustomViews
	public static void testGetCustomViewsByUsernameType() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetCustomViewsByUsernameType");

		MambuAPIServiceFactory serviceFactory = DemoUtil.getAPIServiceFactory();
		UsersService usersService = serviceFactory.getUsersService();
		String username = USER_NAME;

		ApiViewType[] testedTypes = new ApiViewType[] { ApiViewType.LOAN_TRANSACTIONS, ApiViewType.DEPOSIT_TRANSACTIONS };
		for (ApiViewType viewType : testedTypes) {
			System.out.println("\n\nGetting Views for view type=" + viewType);
			List<CustomView> views = usersService.getCustomViews(username, viewType);

			int totalViws = (views == null) ? 0 : views.size();
			System.out.println("Total " + totalViws + " views for view type=" + viewType);

			// Now test GET entities using these Custom Views as a filter
			try {
				testGetEntitiesForCustomView(views);

			} catch (MambuApiException e) {
				System.out.println(methodName + "\nError getting entities for view type=" + viewType + "\n");
			}
		}

	}

	// Retrieve entities by Custom View filter. Available since Mambu 3.7
	private static void testGetEntitiesForCustomView(List<CustomView> views) throws MambuApiException {
		System.out.println(methodName = "\nIn testGetEntitiesForCustomView");

		if (views == null) {
			System.out.println("Cannot get entities for NULL list of views");
			return;

		}
		if (views.size() == 0) {
			System.out.println("List of user custom views is empty");
			return;
		}
		System.out.println("Getting entities for " + views.size() + " views");

		String offset = "0";
		String limit = "5";
		// Custom views can be filtered by an optional Branch ID filter. Available since 4.0. See MBU-7042
		String branchId = demoUser.getAssignedBranchKey();

		System.out.println("Getting entities for branch key=" + branchId);
		for (CustomView view : views) {

			logCustomView(view);

			String viewName = view.getConfigurationName();
			DataViewType viewType = view.getConfigurationDataViewType();
			String viewkey = view.getEncodedKey();

			// Get ApiViewType for this view to determine if it is supported by API
			ApiViewType apiViewType = UsersService.supportedDataViewTypes.get(viewType);
			if (apiViewType == null) {
				System.out.println("\nSkipping custom view type=" + viewType
						+ " it is not supported by GET entities by custom view API");
				continue;
			}

			CustomViewsService service = MambuAPIFactory.getCustomViewsService();
			// Test for all custom view types and all result types
			CustomViewResultType resultTypes[] = CustomViewResultType.values();
			for (CustomViewResultType resultType : resultTypes) {
				System.out.println("\nGetting " + resultType + " type=" + apiViewType + ": " + viewName + "\tKey="
						+ viewkey);

				boolean fullDetails;
				switch (apiViewType) {

				case CLIENTS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;

						List<Client> clients = service.getCustomViewEntities(apiViewType, branchId, fullDetails,
								viewkey, offset, limit);
						System.out.println("Clients=" + clients.size() + "  for View=" + viewName);
						break;
					case FULL_DETAILS:
						fullDetails = true;
						List<ClientExpanded> clientsExpanded = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Client Details=" + clientsExpanded.size() + "  for View=" + viewName);
						break;
					}
					break;
				case GROUPS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<Group> groups = service.getCustomViewEntities(apiViewType, branchId, fullDetails, viewkey,
								offset, limit);
						System.out.println("Groups=" + groups.size() + "  for View=" + viewName);
						break;
					case FULL_DETAILS:
						fullDetails = true;
						List<GroupExpanded> groupsExpanded = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Group Details=" + groupsExpanded.size() + "  for View=" + viewName);
						break;
					}
					break;
				case LOANS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<LoanAccount> loans = service.getCustomViewEntities(apiViewType, branchId, fullDetails,
								viewkey, offset, limit);
						System.out.println("Loans=" + loans.size() + "  for View=" + viewName);
						break;
					case FULL_DETAILS:
						fullDetails = true;
						List<LoanAccountExpanded> loansExpanded = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Loan Details=" + loansExpanded.size() + "  for View=" + viewName);
					}
					break;
				case DEPOSITS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<SavingsAccount> savings = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Savings=" + savings.size() + "  for View=" + viewName);
						break;
					case FULL_DETAILS:
						fullDetails = true;
						List<JSONSavingsAccount> savingsExpanded = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Savings Details=" + savingsExpanded.size() + "  for View=" + viewName);
						break;
					}
					break;
				case LOAN_TRANSACTIONS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<LoanTransaction> transactions = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Loan Transactions=" + transactions.size() + " for View=" + viewName);
						break;
					case FULL_DETAILS:
						System.out.println("No Details type for " + apiViewType);
						break;
					}
					break;
				case DEPOSIT_TRANSACTIONS:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<SavingsTransaction> transactions = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Savings Transactions=" + transactions.size() + " for View=" + viewName);
						break;
					case FULL_DETAILS:
						System.out.println("No Details type for " + apiViewType);
						break;
					}
					break;
				case SYSTEM_ACTIVITIES:
					switch (resultType) {
					case BASIC:
						fullDetails = false;
						List<JSONActivity> activities = service.getCustomViewEntities(apiViewType, branchId,
								fullDetails, viewkey, offset, limit);
						System.out.println("Activities=" + activities.size() + " for View=" + viewName);
						break;
					case FULL_DETAILS:
						System.out.println("No Details type for " + apiViewType);
						break;
					}
					break;
				}
			}
		}
	}

	// Helper: Log CustomView response details
	private static void logCustomView(CustomView view) {
		if (view == null) {
			System.out.println("Custom view is null");
			return;
		}
		String viewName = view.getConfigurationName();
		DataViewType viewType = view.getConfigurationDataViewType();

		System.out.println("\nView name= " + viewName + "\tType=" + viewType + "\tViewKey=" + view.getEncodedKey());

		ColumnConfiguration columnsConfig = view.getColumnConfiguration();
		List<FieldColumn> fieldColumns = columnsConfig.getFieldColumns();

		if (fieldColumns == null) {
			System.out.println("No Columns are defined  for View Name= " + viewName);
			return;
		}
		System.out.println("Total Field Columns=" + fieldColumns.size());
		for (FieldColumn filedColumn : fieldColumns) {
			// Get the column name
			String columnName = filedColumn.getDataField();
			DataItemType dataItemType = filedColumn.getDataItemType();
			// If Custom field - print custom fields details
			CustomField customFild = filedColumn.getCustomField();
			String customFieldId = (customFild == null) ? null : customFild.getId();
			String customFieldName = (customFild == null) ? null : customFild.getName();
			System.out.println("Column=" + columnName + "\tDataType=" + dataItemType + "\tCustomField ID="
					+ customFieldId + "\tCustomField Name=" + customFieldName);
		}
		System.out.println();
	}

	// Update Custom Field values for the User and delete the first available custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntityType.USER);

	}

	// Test getting all user roles. Return all user roles
	public static List<Role> testGetAllUserRoles() throws MambuApiException {
		System.out.println("\nIn testGetAllUserRoles");

		UsersService usersService = MambuAPIFactory.getUsersService();
		List<Role> userRoles = usersService.getUserRoles();

		System.out.println("Total users roles=" + userRoles.size());
		for (Role role : userRoles) {
			System.out.println("\tKey=" + role.getEncodedKey() + "\tName=" + role.getName());
		}
		System.out.println();

		return userRoles;
	}

	// Test get full Role details by role key
	public static void testGetAllUserRoleDetails(List<Role> userRoles) throws MambuApiException {
		System.out.println("\nIn testGetAllUserRole");

		if (userRoles == null || userRoles.size() == 0) {
			System.out.println("WARNING: cannot test GET user role, no roles available");
			return;
		}
		UsersService usersService = MambuAPIFactory.getUsersService();
		// Get random user role key from a list of available roles
		int roleIndex = (int) Math.random() * (userRoles.size() - 1);
		String roleKey = userRoles.get(roleIndex).getEncodedKey();
		// Get full role details
		Role userRole = usersService.getUserRole(roleKey);
		System.out.println("\tKey=" + userRole.getEncodedKey() + "\tName=" + userRole.getName() + "\n\tPermissions="
				+ userRole.getPermissions().getPermissionSet());

	}

	// Log some details for a list of users. Prefix with an optional message
	private static void logUsers(List<User> users, String message) {
		if (users == null) {
			return;
		}
		message = message == null ? "" : message;
		System.out.println(message + "\tTotal Users=" + users.size());
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tName=" + user.getFullName() + "\tId="
					+ user.getId() + "\tBranch=" + user.getAssignedBranchKey() + "\tAdmin=" + user.isAdministrator());
		}
		System.out.println();
	}
}
