import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;


public class Launch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MambuAPIService apiService = MambuAPIFactory.crateService("api", "api", "demo.mambucloud.com");
		
		try {
			String client = apiService.getClient("001578547");
			System.out.println(client);
		} catch (MambuApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
