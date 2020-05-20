package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.UsersService;
import com.mambu.core.shared.model.User;

/**
 * @author cezarrom
 */
public class DemoTestUsersWithApiKeyService {

	public static void main(String[] args) {

		DemoUtil.setUpWithApiKey();

		try {

			testGetAllUsers();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Users Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}


	private static void testGetAllUsers() throws MambuApiException {

		String methodName = "\nIn testGetAllUsers";

		System.out.println(methodName);
		UsersService usersService = MambuAPIFactory.getUsersService();

		List<User> users = usersService.getUsers();
		UsersUtil.logUsers(users, methodName);
	}


}
