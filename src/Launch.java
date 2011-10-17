import java.math.BigDecimal;
import java.util.List;

import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class Launch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			MambuAPIService mambu = MambuAPIFactory.crateService("apied",
					"apied", "demo.mambuonline.com");

			String clientID = "960178943";

			// get just the client info
			Client client = mambu.getClient(clientID);
			System.out.println("--- Got a Client ---");

			System.out.println(client.getFullNameWithId());

			// get the full client details including some custom fields which we
			// print out
			ClientExpanded fullClient = mambu.getClientDetails(clientID);
			List<CustomFieldValue> customFields = fullClient
					.getCustomFieldValues();
			System.out.println("--- Custom Fields ---");

			for (CustomFieldValue customFieldValue : customFields) {
				System.out.println(customFieldValue.getName() + " -> "
						+ customFieldValue.getValue());
			}

			// get an indicator
			BigDecimal indicator = mambu.getIndicator(Indicator.NUM_CLIENTS);
			System.out.println("Num clients: " + indicator);
			
			//get a gl account
			GLAccount glAccount = mambu.getGLAccount("33000");
			System.out.println("Account Name " + glAccount.getLongName());
			System.out.println("Account Balance: " + glAccount.getBalance());

		} catch (MambuApiException e) {
			e.printStackTrace();
			System.out.println(e.getErrorCode());
			System.out.println(e.getErrorMessage());

		}
	}

}
