import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.core.shared.model.CustomFieldValue;

/**
 * Test class to show example usage of the api calls
 * @author edanilkis
 *
 */
public class Launch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MambuAPIService mambu = MambuAPIFactory.crateService("api", "api", "demo.mambucloud.com");
		
		try {
			
			String clientID = "001578547";
			
			//get just the client info
			Client client = mambu.getClient("001578547");
			System.out.println("--- Got a Client ---");

			System.out.println(client.getFullNameWithId());
			
			//get the full client details including some custom fields which we print out
			ClientExpanded fullClient = mambu.getClientDetails(clientID);
			List<CustomFieldValue> customFields = fullClient.getCustomFieldValues();
			System.out.println("--- Custom Fields ---");

			for (CustomFieldValue customFieldValue : customFields) {
				System.out.println(customFieldValue.getName() + " -> " + customFieldValue.getValue());
			}
		} catch (MambuApiException e) {
			System.out.println(e.getErrorCode());
			System.out.println(e.getErrorMessage());

		}
	}

}
