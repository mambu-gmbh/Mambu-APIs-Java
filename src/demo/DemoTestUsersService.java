package demo;

import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.UsersService;
import com.mambu.core.shared.model.User;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestUsersService {

	private static String BRANCH_ID = "GBK 001"; // NE008 Richmond01 GBK 001
	private static String USER_NAME = "demo"; // demo michaeld

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetAllUsers();

			testGetUsersByPage();

			testGetUserById();

			testGetPaginatedUsersByBranch();

			testGetUserByUsername();

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

		List<User> users = usersService.getUsers(branchId, null, null); // RICHMOND BRANCH

		System.out.println("testGetPaginatedUsers OK, barnch ID=" + BRANCH_ID);
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tId=" + user.getId());
		}
		System.out.println();
	}

	public static void testGetUserById() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();

		String userId = "demo";

		System.out.println("\nIn testGetUserById=" + userId);
		Date d1 = new Date();

		User user = usersService.getUserById(userId);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("testGetUserById OK, Total time=" + diff + "\nReturned user= " + user.getFirstName() + " "
				+ user.getLastName() + " Id=" + user.getId() + " Username=" + user.getUsername());

	}

	public static void testGetUserByUsername() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();
		String username = "Funmi"; // USER_NAME
		System.out.println("\nIn testGetUserByUsername with name =" + username);
		User user = usersService.getUserByUsername(username);

		System.out.println("testGetUserByUsername OK,returned user= " + user.getFirstName() + " " + user.getLastName()
				+ " Id=" + user.getId() + " Username=" + user.getUsername());

	}
}
