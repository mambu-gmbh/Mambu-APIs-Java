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
	private static String user = "api";
	private static String password = "api";

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

			System.out.println("DemoUtil: APP KEY=" + appKeyValue);
		} catch (IOException e) {
			System.out.println("  Exception reading config.properties file in Demo Test Loan Service");
			Logger.getAnonymousLogger().severe("Could not read file config.properties");

			e.printStackTrace();

		}
		// Set up Factory
		MambuAPIFactory.setUp(domain, user, password);

		// set up App Key
		MambuAPIFactory.setApplicationKey(appKeyValue);

	}
}
