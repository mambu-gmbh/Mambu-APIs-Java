package demo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mambu.accounts.shared.model.TransactionLimitType;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.customviews.model.CustomViewApiType;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ActivitiesService;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
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
		}

	}

	// getCustomViews
	public static void testGetCustomViewsByUsernameType() throws MambuApiException {
		System.out.println("\nIn testGetCustomViewsByUsernameType");

		MambuAPIServiceFactory serviceFactory = DemoUtil.getAPIServiceFactory();
		UsersService usersService = serviceFactory.getUsersService();
		String username = USER_NAME;

		for (CustomViewApiType viewType : CustomViewApiType.values()) {
			System.out.println("\n\nGetting Views for view type=" + viewType);
			List<CustomView> views = usersService.getCustomViews(username, viewType);

			int totalViws = (views == null) ? 0 : views.size();
			System.out.println("Total " + totalViws + " views for view type=" + viewType);

			// Now test GET entities using these Custom Views as a filter
			try {
				testGetEntitiesForCustomView(views);

			} catch (MambuApiException e) {
				System.out.println("\nError getting entities for view type=" + viewType + "\n");
			}
		}

	}

	// Retrieve entities by Custom View filter. Available since Mambu 3.7
	private static void testGetEntitiesForCustomView(List<CustomView> views) throws MambuApiException {
		System.out.println("\nIn testGetEntitiesForCustomView");

		if (views == null) {
			System.out.println("Cannot get entities for NULL list of views");
			return;

		}
		if (views.size() == 0) {
			System.out.println("List of user custom views is empty");
			return;
		}
		System.out.println("Getting entities for " + views.size() + " views");

		// TODO: when MBU-7042 is fixed - add additional branchId, centreId and centreId filtering params
		for (CustomView view : views) {

			logCustomView(view);

			String viewName = view.getConfigurationName();
			DataViewType viewType = view.getConfigurationDataViewType();
			String viewkey = view.getEncodedKey();

			String offset = "0";
			String limit = "5";

			// Get CustomViewApiType for this view to determine if it is supported by API
			CustomViewApiType viewApiType = UsersService.supportedDataViewTypes.get(viewType);
			if (viewApiType == null) {
				System.out.println("\nSkipping custom view type=" + viewType
						+ " it is not supported by GET entities by custom view API");
				continue;
			}

			System.out.println("\nGetting Entities for View Type= " + viewApiType + "\tName=" + viewName + "\tKey="
					+ viewkey);
			switch (viewApiType) {
			case CLIENTS:
				ClientsService clientsService = MambuAPIFactory.getClientService();
				List<Client> clients = clientsService.getClientsByCustomView(viewkey, offset, limit);
				System.out.println("Total Clients=" + clients.size() + " returned for View=" + viewName);
				break;
			case GROUPS:
				clientsService = MambuAPIFactory.getClientService();
				List<Group> groups = clientsService.getGroupsByCustomView(viewkey, offset, limit);
				System.out.println("Total Groups=" + groups.size() + " returned for View=" + viewName);
				break;
			case LOANS:
				LoansService loansService = MambuAPIFactory.getLoanService();
				List<LoanAccount> loans = loansService.getLoanAccountsByCustomView(viewkey, offset, limit);
				System.out.println("Total Loans=" + loans.size() + " returned for View=" + viewName);
				break;
			case DEPOSITS:
				SavingsService savingsService = MambuAPIFactory.getSavingsService();
				List<SavingsAccount> savings = savingsService.getSavingsAccountsByCustomView(viewkey, offset, limit);
				System.out.println("Total Savings=" + savings.size() + " returned for View=" + viewName);
				break;
			case LOAN_TRANSACTIONS:
				loansService = MambuAPIFactory.getLoanService();
				List<LoanTransaction> loanTransactions = loansService.getLoanTransactionsByCustomView(viewkey, offset,
						limit);
				System.out.println("Total Loan Transactions=" + loanTransactions.size() + " returned for View="
						+ viewName);
				break;
			case DEPOSIT_TRANSACTIONS:
				savingsService = MambuAPIFactory.getSavingsService();
				List<SavingsTransaction> savingsTransactions = savingsService.getSavingsTransactionsByCustomView(
						viewkey, offset, limit);
				System.out.println("Total Savings Transactions=" + savingsTransactions.size() + " returned for View="
						+ viewName);
				break;
			case SYSTEM_ACTIVITIES:
				ActivitiesService activitiesService = MambuAPIFactory.getActivitiesService();
				List<JSONActivity> activities = activitiesService.getActivitiesByCustomView(viewkey, offset, limit);
				System.out.println("Total Activities =" + activities.size() + " returned for View=" + viewName);
				break;
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
		// Since 3.8 ColumnConfiguration.getColumns(); is Deprecated. Use ColumnConfiguration.getFieldColumns() instead
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
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntity.USER);

	}

}
