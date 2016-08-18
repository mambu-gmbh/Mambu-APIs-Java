package demo;

import java.io.IOException;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.DatabaseBackup;
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

	private static String methodName;

	public static void main(String[] args) throws IOException {

		DemoUtil.setUp();

		try {

			testTriggerDatabaseBackup(); // Available since V4.3

			testDownloadLatestDbBackup(); // Available since V4.3

		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}

	}

	/**
	 * Tests triggering of a DB backup process
	 * 
	 * @throws MambuApiException
	 */
	private static void testTriggerDatabaseBackup() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		DatabaseService databaseService = MambuAPIFactory.getDatabaseService();

		DatabaseBackupRequest databaseRequestObject = new DatabaseBackupRequest();
		databaseRequestObject.setCallback("http://someaddressabc.com");

		DatabaseBackupResponse response = databaseService.createDatabaseBackup(databaseRequestObject);

		System.out.println("Database request Response:\n" + response);
	}

	/**
	 * Tests downloading a DB backup
	 * 
	 * @throws MambuApiException
	 * @throws IOException
	 */
	private static void testDownloadLatestDbBackup() throws MambuApiException, IOException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		DatabaseService databaseService = MambuAPIFactory.getDatabaseService();

		DatabaseBackup response = databaseService.downloadLatestDbBackup();

		System.out.println("Backup was successfuly downloaded = " + (response.getContent().toByteArray() != null));

	}
}
