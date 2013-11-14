package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;

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
	private static String domain = "decisions21.sandbox.mambu.com"; // Domain name. Format example: demo.mambucloud.com
	// username
	private static String user = "MichaelD"; // demo Mambu Username
	// password
	private static String password = "michaeld"; // demo User password

	// Demo Data
	final static String demoClientLastName = "Chernaya"; // Doe Chernaya
	final static String demoClientFirstName = "Irina"; // John Irina
	final static String demoUsername = "MichaelD"; // demo MichaelD

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

	// Get Demo User
	public static User getDemoUser() throws MambuApiException {

		UsersService usersService = MambuAPIFactory.getUsersService();
		User user = usersService.getUserByUsername(demoUsername);

		return user;
	}

	// Get or Create a Demo Client
	public static Client getDemoClient() throws MambuApiException {

		ClientsService clientsService = MambuAPIFactory.getClientService();
		List<Client> clients = clientsService.getClientByFullName(demoClientLastName, demoClientFirstName);
		Client client;
		if (clients.isEmpty()) {
			client = clientsService.createClient(demoClientFirstName, demoClientLastName);
		} else {
			client = clients.iterator().next();
		}
		return client;
	}

}
