package demo;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.UsersService;

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
			testGetPaginatedUsersFilteredByBranch();

		} catch (MambuApiException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			System.out.println(e.getErrorCode());
		}
	}

	public static void testGetUsers() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();

		System.out.println(usersService.getUsers().get(1).getUsername());

	}

	public static void testGetPaginatedUsersFilteredByBranch() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();

		System.out.println(usersService.getUsers("41", null, null).size());

	}

}
