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
			String client = apiService.getClient("A01578547");
			System.out.println(client);
		} catch (MambuApiException e) {
			System.out.println(e.getErrorCode());
			System.out.println(e.getErrorMessage());
//			e.printStackTrace();

		}
	}

}
