package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;

/**
 * Helper class to be used with Demo programs. It defines and handles:
 * 
 * a) login params,
 * 
 * b) initializes LogManager by reading the logger.properties file
 * 
 * c) initializes APPLICATION_KEY settings by reading the config.properties file
 * 
 * d) initializes MambuAPIFactory and sets up Application Key parameter
 * 
 * Usage: Update domain, user password as needed. Update logger.properties file and config.properties file as needed,
 * 
 * All other demo programs must invoke the following static method to use this class: setUp(). E.g. DemoUtil.setUp();
 * 
 * @author mdanilkis
 * 
 */
public class DemoUtil {

	// "subdomain.sandbox.mambu.com"
	private static String domain = "subdomain.sandbox.mambu.com"; // Domain name. Format example: demo.mambucloud.com
	private static String domain2 = "subdomain.mambu.com"; // Domain name where demo client #1 does not exist. Format
															// example: demo.mambucloud.com
	// username
	private static String user = "demo"; // demo Mambu Username
	private static String user2 = "demo"; // demo Mambu Username for domain2
	// password
	private static String password = "demo"; // demo User password
	private static String password2 = "demo"; // demo User password for domain2

	// Demo Data
	final static String demoClientLastName = "Doe"; // Doe Chernaya
	final static String demoClientLastName2 = "Doe"; // Doe Chernaya
	final static String demoClientFirstName = "John"; // John Irina
	final static String demoClientFirstName2 = "Jane"; // John Irina
	final static String demoUsername = "demo"; // demo MichaelD

	public static void setUp() {
		// get Logging properties file
		try {

			FileInputStream loggingFile = new FileInputStream("logger.properties");

			LogManager.getLogManager().readConfiguration(loggingFile);
			System.out.println("DemoUtil: Logger Initiated");

		} catch (IOException e) {
			System.out.println("  Exception reading property file in Demo Test Loan Service");
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
		}

		Properties prop = new Properties();
		String appKeyValue = null;
		try {
			InputStream configFile = new FileInputStream("config.properties");

			prop.load(configFile);

			appKeyValue = prop.getProperty("APPLICATION_KEY");
			if (appKeyValue == null)
				System.out.println("WARNING: DemoUtil: APP KEY is NOT specified");
			else
				System.out.println("DemoUtil: APP KEY specified");

		} catch (IOException e) {
			System.out.println("  Exception reading config.properties file in Demo Util Service");
			Logger.getAnonymousLogger().severe("Could not read file config.properties");

			e.printStackTrace();

		}
		// Set up Factory
		MambuAPIFactory.setUp(domain, user, password);

		// set up App Key
		MambuAPIFactory.setApplicationKey(appKeyValue);

	}

	/**
	 * Get service factory object that includes fixed Mambu credentials with domain
	 * 
	 * @return
	 */
	public static MambuAPIServiceFactory getAPIServiceFactory() {
		return getAPIServiceFactory(false);
	}

	/**
	 * Get service factory object that includes fixed Mambu credentials with domain2
	 * 
	 * @param secondaryDomain
	 *            true if service factory for secondary domain is required, false for primary domain
	 * 
	 * @return
	 */
	public static MambuAPIServiceFactory getAPIServiceFactory(boolean secondaryDomain) {
		if (!secondaryDomain) {
			return MambuAPIServiceFactory.getFactory(domain, user, password);
		} else {
			return MambuAPIServiceFactory.getFactory(domain2, user2, password2);
		}
	}

	// Get Demo User
	public static User getDemoUser() throws MambuApiException {
		System.out.println("\nIn getDemoUser");

		UsersService usersService = MambuAPIFactory.getUsersService();
		User user = usersService.getUserByUsername(demoUsername);

		return user;
	}

	/**
	 * Get or Create a Demo Client of primary domain, delegate for {@link DemoUtil#getDemoClient(boolean)}
	 * 
	 * @return
	 * @throws MambuApiException
	 */
	public static Client getDemoClient() throws MambuApiException {
		return getDemoClient(false);
	}

	/**
	 * Get or Create a Demo Client
	 * 
	 * @param secondaryDomain
	 *            true if demo client of secondary domain is required, false for primary domain
	 * @return
	 * @throws MambuApiException
	 */
	public static Client getDemoClient(boolean secondaryDomain) throws MambuApiException {
		System.out.println("\nIn getDemoClient");

		ClientsService clientsService;
		if (!secondaryDomain) {
			clientsService = MambuAPIFactory.getClientService();
		} else {
			clientsService = getAPIServiceFactory(true).getClientService();
		}

		List<Client> clients = clientsService.getClientByFullName(demoClientLastName, demoClientFirstName);
		Client client;
		if (clients.isEmpty()) {
			if (!secondaryDomain) {
				client = clientsService.createClient(demoClientFirstName, demoClientLastName);
			} else {
				client = clientsService.createClient(demoClientFirstName2, demoClientLastName2);
			}
		} else {
			client = clients.iterator().next();
		}
		return client;

	}

	public static Group getDemoGroup() throws MambuApiException {
		System.out.println("\nIn getDemoGroup");

		ClientsService clientsService = MambuAPIFactory.getClientService();
		// all groups for our demo user
		List<Group> groups = clientsService.getGroupsByBranchOfficer(null, demoUsername, "0", "5");

		if (groups != null) {
			int randomIndex = (int) Math.random() * (groups.size() - 1);
			return groups.get(randomIndex);
		}

		System.out.println("getDemoGroup: no groups for the Demo User in the demo data");

		return null;

	}

	public static LoanProduct getDemoLoanProduct() throws MambuApiException {
		System.out.println("\nIn getDemoLoanProduct");

		LoansService service = MambuAPIFactory.getLoanService();
		// all groups for our demo user
		List<LoanProduct> products = service.getLoanProducts("0", "5");

		if (products != null) {
			int randomIndex = (int) (Math.random() * (products.size() - 1));
			return products.get(randomIndex);
		}

		System.out.println("getDemoLoanProduct: no Loan products defined");

		return null;

	}

	public static SavingsProduct getDemoSavingsProduct() throws MambuApiException {
		System.out.println("\nIn getDemoSavingsProduct");

		SavingsService service = MambuAPIFactory.getSavingsService();
		// all groups for our demo user
		List<SavingsProduct> products = service.getSavingsProducts("0", "5");

		if (products != null) {
			int randomIndex = (int) (Math.random() * (products.size() - 1));
			return products.get(randomIndex);
		}

		System.out.println("getDemoSavingsProduct: no Savings products defined");

		return null;
	}

	public static LoanAccount getDemoLoanAccount() throws MambuApiException {
		System.out.println("\nIn getDemoLoanAccount");

		LoansService service = MambuAPIFactory.getLoanService();
		// all groups for our demo user
		List<LoanAccount> loans = service.getLoanAccountsByBranchOfficerState(null, demoUsername, null, "0", "5");

		if (loans != null) {
			int randomIndex = (int) (Math.random() * (loans.size() - 1));
			return loans.get(randomIndex);
		}

		System.out.println("getDemoLoanAccount: no Loan Accounts the Demo User exist");

		return null;
	}

	public static SavingsAccount getDemoSavingsAccount() throws MambuApiException {
		System.out.println("\nIn getDemoSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();
		// all savings for our demo user
		List<SavingsAccount> savings = service.getSavingsAccountsByBranchOfficerState(null, demoUsername, null, "0",
				"5");

		if (savings != null) {
			int randomIndex = (int) (Math.random() * (savings.size() - 1));
			return savings.get(randomIndex);
		}

		System.out.println("getDemoLoanAccount: no Loan Accounts the Demo User exist");

		return null;
	}
}
