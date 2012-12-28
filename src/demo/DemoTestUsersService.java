package demo;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.UsersService;

import java.util.List;
import com.mambu.core.shared.model.User;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestUsersService {

	public static void main(String[] args) {

		try {
			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");

			testGetUsers();
			testGetUserById();

			testGetPaginatedUsersFilteredByBranch();

		} catch (MambuApiException e) {
			System.out.println(e.getCause());
			System.out.println(e.getErrorMessage());
			System.out.println(e.getErrorCode());
		}
	}

	public static void testGetUsers() throws MambuApiException {
		System.out.println("In testGetUsers");
		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers();

		System.out.println("testGetUsers OK");
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername());
		}

	}

	public static void testGetPaginatedUsersFilteredByBranch() throws MambuApiException {
		System.out.println("In testGetPaginatedUsersFilteredByBranch");
		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers("7", null, null); // RICHMOND BRANCH

		System.out.println("testGetPaginatedUsersFilteredByBranch OK");
		for (User user : users) {
			System.out.println(" Username=" + user.getUsername());
		}

	}

	public static void testGetUserById() throws MambuApiException {
		System.out.println("In testGetUserById ");
		UsersService usersService = MambuAPIFactory.getUsersService();

		User user = usersService.getUserById("1");
		System.out.println("testGetUserById OK, retrurned user=" + user.getFirstName() + " " + user.getLastName());

	}

}
