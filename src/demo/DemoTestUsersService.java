package demo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.clients.shared.data.ClientsDataField;
import com.mambu.clients.shared.data.GroupsDataField;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.data.DataViewType;
import com.mambu.core.shared.model.ColumnConfiguration;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.Permissions;
import com.mambu.core.shared.model.Permissions.Permission;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.data.LoansDataField;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.data.SavingsDataField;
import com.mambu.savings.shared.model.SavingsAccount;

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

			// TODO: test with Mambu 3.7
			// testGetCustomViewsByUsername();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Users Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetAllUsers() throws MambuApiException {
		System.out.println("\nIn testGetAllUsers");
		UsersService usersService = MambuAPIFactory.getUsersService();

		Date d1 = new Date();

		List<User> users = usersService.getUsers();

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total users=" + users.size() + " Total time=" + diff);
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tId=" + user.getId());
		}
		System.out.println();
	}

	public static void testGetUsersByPage() throws MambuApiException {

		String offset = "0";
		String limit = "500";

		System.out.println("\nIn testGetUsersByPage" + " offset=" + offset + " limit=" + limit);
		Date d1 = new Date();

		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers(offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total users=" + users.size() + " Total time=" + diff);
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tId=" + user.getId() + "\tBranchId="
					+ user.getAssignedBranchKey());
		}
		System.out.println();
	}

	public static void testGetPaginatedUsersByBranch() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();

		String offset = "0";
		String limit = "5";
		String branchId = BRANCH_ID; // GBK 001
		System.out
				.println("\nIn testGetPaginatedUsers ByBranch=" + branchId + "  offset=" + offset + " limit=" + limit);

		List<User> users = usersService.getUsers(branchId, null, null);

		System.out.println("testGetPaginatedUsers OK, barnch ID=" + BRANCH_ID);
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tId=" + user.getId());
		}
		System.out.println();
	}

	public static void testGetUserById() throws MambuApiException {
		System.out.println("\nIn testGetUserById");

		UsersService usersService = MambuAPIFactory.getUsersService();

		String userId = USER_NAME;

		System.out.println("\nIn testGetUserById=" + userId);
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
	}

	public static void testGetUserByUsername() throws MambuApiException {
		System.out.println("\nIn testGetUserByUsername");

		UsersService usersService = MambuAPIFactory.getUsersService();
		String username = USER_NAME;
		System.out.println("\nIn testGetUserByUsername with name =" + username);
		User user = usersService.getUserByUsername(username);

		System.out.println("testGetUserByUsername OK,returned user= " + user.getFirstName() + " " + user.getLastName()
				+ " Id=" + user.getId() + " Username=" + user.getUsername());

	}

	// Get Custom Views. Available since Mambu 3.7
	public static void testGetCustomViewsByUsername() throws MambuApiException {
		System.out.println("\nIn testGetCustomViewsByUsername");

		UsersService usersService = MambuAPIFactory.getUsersService();
		String username = USER_NAME;
		System.out.println("\nIn testGetCustomViewsByUsername with name =" + username);

		List<CustomView> views = usersService.getCustomViews(username);

		if (views == null) {
			System.out.println("testGetCustomViewsByUsername OK,returned null");
			return;
		}
		System.out.println("testGetCustomViewsByUsername OK,returned views count= " + views.size());

		for (CustomView view : views) {
			// Log view details
			logCustomView(view);

			String viewName = view.getConfigurationName();
			DataViewType viewType = view.getConfigurationDataViewType();
			String viewkey = view.getEncodedKey();

			// Test GET entities API with this Custom View as a filter
			testGetEntitiesForCustomView(viewType, viewName, viewkey);

		}

	}

	// Retrieve entities by Custom View filter. Available since Mambu 3.7
	private static void testGetEntitiesForCustomView(DataViewType viewType, String viewName, String viewkey)
			throws MambuApiException {
		System.out.println("\nIn testGetEntitiesForCustomView");

		final String offset = "0";
		final String limit = "5";

		switch (viewType) {
		case CLIENT:
			System.out.println("Getting Clients for Custom View ID=" + viewkey);
			ClientsService clientsService = MambuAPIFactory.getClientService();
			List<Client> clients = clientsService.getClientsByCustomView(viewkey, offset, limit);
			System.out.println("Total Clients=" + clients.size() + " returned for View=" + viewName);
			break;
		case GROUP:
			System.out.println("Getting Groups for Custom View ID=" + viewkey);
			clientsService = MambuAPIFactory.getClientService();
			List<Group> groups = clientsService.getGroupsByCustomView(viewkey, offset, limit);
			System.out.println("Total Groups=" + groups.size() + " returned for View=" + viewName);
			break;
		case LOANS:
			System.out.println("Getting Loans for Custom View ID=" + viewkey);
			LoansService loansService = MambuAPIFactory.getLoanService();
			List<LoanAccount> loans = loansService.getLoanAccountsByCustomView(viewkey, offset, limit);
			System.out.println("Total Loans=" + loans.size() + " returned for View=" + viewName);
			break;
		case SAVINGS:
			System.out.println("Getting Savings for Custom View ID=" + viewkey);
			SavingsService savingsService = MambuAPIFactory.getSavingsService();
			List<SavingsAccount> savings = savingsService.getSavingsAccountsByCustomView(viewkey, offset, limit);
			System.out.println("Total Savings=" + savings.size() + " returned for View=" + viewName);
			break;
		default:
			System.out.println("View Type= " + viewType + " is not supported by GET entities by custom view API");
			break;
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

		ColumnConfiguration columnsConfig = view.getColumnConfiguration();
		List<String> columns = columnsConfig.getColumns();

		System.out.println("\nView name= " + viewName + "\tType=" + viewType + "\tTotal columns=" + columns.size());
		// Print Columns for this view
		switch (viewType) {
		case CLIENT:
			for (String column : columns) {
				ClientsDataField clientDataField = ClientsDataField.valueOf(column);
				System.out.println("Column=" + column + "\tEnum value=" + clientDataField);
			}
			break;
		case GROUP:
			for (String column : columns) {
				GroupsDataField groupDataField = GroupsDataField.valueOf(column);
				System.out.println("Column=" + column + "\tEnum value=" + groupDataField);
			}
			break;
		case LOANS:
			for (String column : columns) {
				LoansDataField laonDataField = LoansDataField.valueOf(column);
				System.out.println("Column=" + column + "\tEnum value=" + laonDataField);
			}
			break;
		case SAVINGS:
			for (String column : columns) {
				SavingsDataField savingsDataField = SavingsDataField.valueOf(column);
				System.out.println("Column=" + column + "\tEnum value=" + savingsDataField);
			}
			break;
		default:
			System.out.println("Unknown Custom View type=" + viewType);
			break;
		}
	}
}
