package demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraint;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.NotificationsService;
import com.mambu.apisdk.services.SearchService;
import com.mambu.core.shared.data.DataFieldType;
import com.mambu.core.shared.data.FilterElement;
import com.mambu.notifications.shared.model.MessageState;
import com.mambu.notifications.shared.model.NotificationMessage;
import com.mambu.notifications.shared.model.NotificationMessageDataField;

/**
 * Test class to show example usage of the /notifications API calls
 * 
 * @author acostros
 *
 */
public class DemoTestNotificationsService {

	
	private static String methodName;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testResendingFailedNotifications(); // Available since 4.5
			
		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Notifications Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println("Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	
	}

	/**
	 * Tests resending the failed notification messages
	 * 
	 * @throws MambuApiException
	 */
	private static void testResendingFailedNotifications() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		
		NotificationsService notificationsService = MambuAPIFactory.getNotificationsService();
		
		List<String> failedNotifications = getFailedNotifications();
		
		if(CollectionUtils.isEmpty(failedNotifications)){
			System.out.println("WARNING: no failed message were found to be resent");
			return;
		}
		
 		boolean result = notificationsService.resendFailedNotifications(failedNotifications);
		
		System.out.println("Resending notifications result is " + result);

			
	}
	
	/**
	 * Helper method used to fetch all the failed messages
	 * 
	 * @return a list of fetched failed messages
	 * 
	 * @throws MambuApiException
	 */
	private static List<String> getFailedNotifications() throws MambuApiException{
		
		List<String> foundFailedNotifications = new ArrayList<>();
		
		List<JSONFilterConstraint> constraints = new ArrayList<>();
		JSONFilterConstraint constraint = new JSONFilterConstraint();

		constraint.setDataFieldType(DataFieldType.NATIVE.name());
		constraint.setFilterSelection(NotificationMessageDataField.STATE.name());
		constraint.setFilterElement(FilterElement.EQUALS.name());
		constraint.setValue(MessageState.FAILED.name());

		constraints.add(constraint);

		JSONFilterConstraints filterConstraints = new JSONFilterConstraints();
		filterConstraints.setFilterConstraints(constraints);

		SearchService searchService = MambuAPIFactory.getSearchService();

		List<NotificationMessage> notificationMessages = searchService.getNotificationMessages(filterConstraints,
				"0", "5");

		for(NotificationMessage failedMessage :notificationMessages){
			foundFailedNotifications.add(failedMessage.getEncodedKey());
		}
		
		return foundFailedNotifications;
		
	}
	
	
	
}
