package demo;

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

	private static String BRANCH_ID = "richmond_001";
	private static String USER_NAME = "MichaelD";

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetUsers();

			testGetUserById();

			testGetPaginatedUsersFilteredByBranch();
			// TODO: this is not implemented yet, returns all users for now
			// testGetUserByUsername();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Users Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetUsers() throws MambuApiException {
		System.out.println("In testGetUsers");
		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers();

		System.out.println("Total users=" + users.size());
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tUser Id=" + user.getId());
		}
		System.out.println();
	}

	public static void testGetPaginatedUsersFilteredByBranch() throws MambuApiException {
		System.out.println("In testGetPaginatedUsersFilteredByBranch");

		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers(BRANCH_ID, null, null); // RICHMOND BRANCH

		System.out.println("testGetPaginatedUsersFilteredByBranch OK");
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername() + "\tUser Id=" + user.getId());
		}
		System.out.println();
	}

	public static void testGetUserById() throws MambuApiException {
		System.out.println("In testGetUserById ");
		UsersService usersService = MambuAPIFactory.getUsersService();

		User user = usersService.getUserById("1");

		System.out.println("testGetUserById OK, returned user= " + user.getFirstName() + " " + user.getLastName()
				+ " id=" + user.getId());

	}

	// getUserByUsername(
	public static void testGetUserByUsername() throws MambuApiException {
		System.out.println("In testGetUserByUsername ");
		UsersService usersService = MambuAPIFactory.getUsersService();

		User user = usersService.getUserByUsername(USER_NAME);

		System.out.println("testGetUserByUsername OK, returned user= " + user.getFirstName() + " " + user.getLastName()
				+ " id=" + user.getId());

	}

}
