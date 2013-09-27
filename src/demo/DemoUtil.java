package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.apisdk.MambuAPIFactory;

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

	private static String domain = "mambucio.sandbox.mambu.com"; // Domain name. Format example: demo.mambucloud.com

	private static String user = "mambucio"; // Mambu Username
	private static String password = "cloudy2010"; // User password

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
}
