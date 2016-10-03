package demo;

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

	public static void main(String[] args) {

		DemoUtil.setUp();

		if (testTriggerDatabaseBackup()) {// Available since V4.3
			// added here because we don`t know how long to wait until the backup is done
			// backup creation might vary in time depending on the DB size
			while (true) {
				if (testDownloadLatestDbBackup()) {// Available since V4.3
					System.out.println("DB backup was successfully downloaded");
					break;
				}
			}
		}
	}

	/**
	 * Tests triggering of a DB backup process
	 * 
	 * @throws MambuApiException
	 */
	private static boolean testTriggerDatabaseBackup() {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		DatabaseBackupResponse response = null;
		try{
			DatabaseService databaseService = MambuAPIFactory.getDatabaseService();

			DatabaseBackupRequest databaseRequestObject = new DatabaseBackupRequest();
			databaseRequestObject.setCallback("http://someaddressabc.com");

			response = databaseService.createDatabaseBackup(databaseRequestObject);
			
		} catch (MambuApiException mae){
			DemoUtil.logException(methodName, mae);
		}
		

		System.out.println("Response for DB backup request:\n" + response);
		return response.getReturnStatus().equals("SUCCESS");
	}

	/**
	 * Tests downloading a DB backup
	 * 
	 * @throws MambuApiException
	 */
	private static boolean testDownloadLatestDbBackup()  {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		DatabaseBackup response = null;

		try {
			DatabaseService databaseService = MambuAPIFactory.getDatabaseService();

			response = databaseService.downloadLatestDbBackup();

		} catch (MambuApiException mae) {
			DemoUtil.logException(methodName, mae);
		}

		return response != null && response.getContent() != null;

	}
}
