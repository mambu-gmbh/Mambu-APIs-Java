package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.apisdk.MambuAPIFactory;

public class DemoUtil {

	private static String domain = "demo.mambucloud.com"; // demo.mambucloud.com decisions21.sandbox.mambu.com
															// dec21.mambucloud.com
	private static String user = "api"; // api MichaelD
	private static String password = "api"; // api

	public static void setUp() {
		// get Logging properties file
		try {

			FileInputStream loggingFile = new FileInputStream("logging.properties");

			LogManager.getLogManager().readConfiguration(loggingFile);
			System.out.println("DemoUtil: Logger Initiated");

		} catch (IOException e) {
			System.out.println("  Exception reading property file in Demo Test Loan Service");
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
		}

		Properties prop = new Properties();
		String appKeyValue = null;
		try {
			InputStream configFile = new FileInputStream("config.properties"); // am.open("config.properties",
																				// Context.MODE_PRIVATE);

			prop.load(configFile);

			appKeyValue = prop.getProperty("APPLICATION_KEY");

			System.out.println("DemoUtil: APP KEY=" + appKeyValue);
		} catch (IOException e) {
			System.out.println("  Exception reading Config file in Demo Test Loan Service");
			Logger.getAnonymousLogger().severe("Could not read config file config.properties");

			e.printStackTrace();

		}
		// Set up Factory
		// MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");
		// MambuAPIFactory.setUp("decisions21.sandbox.mambu.com", "MichaelD", "MichaelD");
		MambuAPIFactory.setUp(domain, user, password);

		// set up App Key
		MambuAPIFactory.setApplicationKey(appKeyValue);

	}
}
