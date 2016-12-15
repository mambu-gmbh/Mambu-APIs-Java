package demo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mambu.activityfeed.shared.model.Activity;
import com.mambu.activityfeed.shared.model.ActivityType;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ActivitiesService;
import com.mambu.clients.shared.model.Client;

/**
 * Test class to show example usage of the api calls for Activities API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestActivitiesService {

	// Client ID to be selected for testGetActivitiesForEntity() unit test
	private static String clientKeyWithActivities;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			// available since Mambu 3.5
			testGetAllActivities();
			testGetActivitiesForEntity();
			testGetActivitiesWithPaginationForClient(); // Available since Mambu 4.3
			testGetAllActivitiesPaginated(); // Available since Mambu 4.3

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Activities Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetAllActivities() throws MambuApiException {
		System.out.println("\nIn testGetAllActivities");

		ActivitiesService activitiesService = MambuAPIFactory.getActivitiesService();

		// Till now
		Date toDate = new Date();

		long timeNow = toDate.getTime();
		int forNumberOfDays = 5;
		long activiTiesForMsecInterval = 1000 * 60 * 60 * 24 * forNumberOfDays;
		long timeBefore = timeNow - activiTiesForMsecInterval;

		// From forNumberOfDays ago
		Date fromDate = new Date(timeBefore);

		// Make an API call with entity name and Id set to NULL to get all available activities
		List<JSONActivity> jsonActivities = activitiesService.getActivities(fromDate, toDate);

		// Print results
		if (jsonActivities == null) {
			System.out.println("Error - API returned NULL for" + "\tFrom Date=" + fromDate.toString() + "\tTo Date ="
					+ toDate.toString());
			return;
		}
		System.out.println("Total activities returned=" + jsonActivities.size() + "\tFrom Date=" + fromDate.toString()
				+ "\tTo Date =" + toDate.toString());

		for (JSONActivity jsonActivity : jsonActivities) {
			// Get activity itself
			Activity activity = jsonActivity.getActivity();
			ActivityType activityType = activity.getType();

			System.out.println(activityType + "\tID =" + activity.getTransactionId() + " Client Key="
					+ activity.getClientKey() + "\tLoanAccount Key=" + activity.getLoanAccountKey()
					+ "\tSavingsAccount Key=" + activity.getSavingsAccountKey());

			// Remember Client's ID for the first Client related activity (for subsequent unit tests)
			final String activityTypePrefix = "CLIENT_";
			// Get ClientiId if activity is Client related (it would then contain non null clientId)
			if (clientKeyWithActivities == null && activityType != null
					&& activityType.name().startsWith(activityTypePrefix)) {
				clientKeyWithActivities = activity.getClientKey();

			}
		}

	}

	public static void testGetActivitiesForEntity() throws MambuApiException {
		System.out.println("\nIn testGetActivitiesForEntity");

		ActivitiesService activitiesService = MambuAPIFactory.getActivitiesService();

		// Till now
		Date toDate = new Date();

		long timeNow = toDate.getTime();
		int forNumberOfDays = 10;
		long activiTiesForMsecInterval = 1000L * 60 * 60 * 24 * forNumberOfDays;
		long timeBefore = timeNow - activiTiesForMsecInterval;

		// From forNumberOfDays ago
		Date fromDate = new Date(timeBefore);

		Class<Client> mambuEntity = Client.class;
		// Use Client ID saved in the testGetAllActivities() unit test
		String entityId = clientKeyWithActivities;
		if (entityId == null) {
			System.out.println("WARNING: No Activities for Client entity to retrieve");
			return;
		}

		// Make an API call to get all activities for the specified MambuEntity type and its ID
		List<JSONActivity> jsonActivities = activitiesService.getActivities(fromDate, toDate, mambuEntity, entityId);

		// Print results
		printMambuEntityAndActivitiesDetails(mambuEntity, entityId, jsonActivities, fromDate, toDate);
	}
	
	/**
	 * Tests getting activities paginated for a client 
	 * 
	 * @throws MambuApiException 
	 * 
	 */
	public static void testGetActivitiesWithPaginationForClient() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		ActivitiesService activitiesService = MambuAPIFactory.getActivitiesService();

		Class<Client> mambuEntity = Client.class;

		Calendar toDate = Calendar.getInstance(); 

		Calendar fromDate = Calendar.getInstance();
		fromDate.add(Calendar.DAY_OF_MONTH, -10);
		
		String entityId = DemoUtil.getDemoClient().getEncodedKey();
		if (entityId == null) {
			System.out.println("WARNING: No Activities can be obtained for a client with null encodedKey");
			return;
		}

		List<JSONActivity> jsonActivities = activitiesService.getActivities(fromDate.getTime(), toDate.getTime(),
				mambuEntity, entityId, 0, 5);

		printMambuEntityAndActivitiesDetails(mambuEntity, entityId, jsonActivities, fromDate.getTime(), toDate.getTime());
	}

	/*
	 * Tests getting ALL activities paginated
	 */
	public static void testGetAllActivitiesPaginated() throws MambuApiException{
		
		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		
		ActivitiesService activitiesService = MambuAPIFactory.getActivitiesService();
		
		Calendar toDate = Calendar.getInstance(); 

		Calendar fromDate = Calendar.getInstance();
		fromDate.add(Calendar.DAY_OF_MONTH, -10);
		
		List<JSONActivity> jsonActivities = activitiesService.getActivities(fromDate.getTime(), toDate.getTime(), 0, 5);
		
		printMambuEntityAndActivitiesDetails(null, null, jsonActivities, fromDate.getTime(), toDate.getTime());	
	}
	
	/**
	 * Helper method used to print to console the entity`s and activity`s details.
	 * 
	 * @param mambuEntity
	 *            the Mambu entity
	 * @param entityId
	 *            the entity id (encoded key)
	 * @param jsonActivities
	 *            the activities to be printed
	 */
	private static void printMambuEntityAndActivitiesDetails(Class<Client> mambuEntity, String entityId,
			List<JSONActivity> jsonActivities, Date fromDate, Date toDate) {

		String entityNameToBePrinted =  mambuEntity != null ? " Entity= " + mambuEntity.getSimpleName() : " All entities ";
		String entityIdToBePrinted = entityId != null ? " withId= " + entityId : " ";
		if (jsonActivities == null) {
			System.out.println("Error - API returned NULL for" + entityNameToBePrinted + "\tFrom Date="
					+ fromDate.toString() + "\tTo Date =" + toDate.toString());
			return;
		}
		
		System.out.println("Total " + jsonActivities.size() + " activities returned for "
				+ entityNameToBePrinted + entityIdToBePrinted + "\tFrom Date=" + fromDate.toString()
				+ "\tTo Date =" + toDate.toString());

		for (JSONActivity jsonActivity : jsonActivities) {
			// Get activity itself
			Activity activity = jsonActivity.getActivity();
			String name = jsonActivity.getClientName();
			System.out.println(activity.getType() + "\tEntity Name=" + name + "\tID =" + activity.getTransactionId()
					+ "\tNotes=" + activity.getNotes() + "\tTimestamp=" + activity.getTimestamp());
		}

	}
}
