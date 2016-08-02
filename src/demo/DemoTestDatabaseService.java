package demo;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.DatabaseBackupRequest;
import com.mambu.apisdk.model.DatabaseBackupResponse;
import com.mambu.apisdk.services.DatabaseService;

/**
 * Test class to show example usage of the database api calls
 * 
 * @author acostros
 *
 */

public class DemoTestDatabaseService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testTriggerDatabaseBackup(); // Available since V4.3

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Tasks Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	/**
	 * Tests triggering of a DB backup
	 * 
	 * @throws MambuApiException
	 */
	private static void testTriggerDatabaseBackup() throws MambuApiException {

		DatabaseService databaseService = MambuAPIFactory.getDatabaseService();

		DatabaseBackupRequest databaseRequestObject = new DatabaseBackupRequest();
		databaseRequestObject.setCallback("http://someaddressabc.com");

		DatabaseBackupResponse response = databaseService.createDatabaseBackup(databaseRequestObject);

		System.out.println(response);

	}
}
