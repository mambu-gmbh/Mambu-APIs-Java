package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accounts.shared.model.TransactionChannel.ChannelField;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.apisdk.util.APIData;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomField.DataType;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
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
	final static String demoClientFirstName2 = "Jane"; // Jane Irina
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

	// Get Demo Branch with full details
	public static Branch getDemoBranch() throws MambuApiException {
		System.out.println("\nIn getDemoBranch");

		OrganizationService orgService = MambuAPIFactory.getOrganizationService();
		List<Branch> branches = orgService.getBranches("0", "5");
		if (branches == null || branches.size() == 0) {
			return null;
		}
		// Get Full details for the demo Branch
		String branchId = branches.get(0).getId();
		return orgService.getBranch(branchId);
	}

	// Get Demo Centre with full details
	public static Centre getDemoCentre() throws MambuApiException {
		System.out.println("\nIn getDemoCentre");

		OrganizationService orgService = MambuAPIFactory.getOrganizationService();
		List<Centre> centres = orgService.getCentres(null, "0", "5");
		if (centres == null || centres.size() == 0) {
			return null;
		}
		// Get Full details for the demo Centre
		String centreId = centres.get(0).getId();
		return orgService.getCentre(centreId);
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
		System.out.println("\nIn getDemoClient with secondaryDomain flag=" + secondaryDomain);

		ClientsService clientsService = (secondaryDomain) ? getAPIServiceFactory(true).getClientService()
				: MambuAPIFactory.getClientService();
		String clientFirstName = (secondaryDomain) ? demoClientFirstName2 : demoClientFirstName;
		String clientLastname = (secondaryDomain) ? demoClientLastName2 : demoClientLastName;

		List<Client> clients = clientsService.getClientByFullName(clientLastname, clientFirstName);
		Client client = null;
		if (clients == null || clients.isEmpty()) {
			// Create new client
			client = new Client(clientFirstName, clientLastname);
			ClientExpanded clientDetails = new ClientExpanded(client);

			clientDetails = clientsService.createClient(clientDetails);
			client = clientDetails.getClient();
		} else {
			// Return first client
			client = clients.iterator().next();
		}

		return client;

	}

	public static Group getDemoGroup() throws MambuApiException {
		System.out.println("\nIn getDemoGroup");

		ClientsService clientsService = MambuAPIFactory.getClientService();
		// all groups for our demo user
		List<Group> groups = clientsService.getGroupsByBranchOfficer(null, demoUsername, "0", "5");

		if (groups != null && groups.size() > 0) {
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

	// Helper to create a valid test value for a custom field based on its data type
	public static String makeNewCustomFieldValue(CustomFieldValue value) {

		String fieldId = value.getCustomField().getId();
		DataType fieldType = value.getCustomField().getDataType();
		String fieldValue = value.getValue();

		String newValue = null;
		switch (fieldType) {
		case STRING:
			// Set demo string with the current date
			return newValue = "Updated by API on " + new Date().toString();
		case NUMBER:
			// Increase current numeric value by 10
			return newValue = String.valueOf(Float.parseFloat(fieldValue) + 10);
		case CHECKBOX:
			// Change the check box's value to opposite
			return newValue = (fieldValue.equals("TRUE")) ? "FALSE" : "TRUE";
		case SELECTION:
			// Change selection to any other allowed selection (if more than one is defined)
			ArrayList<String> values = value.getCustomField().getValues();
			for (String selectionValue : values) {
				if (!fieldValue.equalsIgnoreCase(selectionValue)) {
					return newValue = selectionValue;
				}
			}
			System.out.println("WARNING: Cannot update selection value as only one value is defined, Field ID="
					+ fieldId);
			return fieldValue;
		case DATE:
			// return current date as new value
			return newValue = new SimpleDateFormat(APIData.yyyyMmddFormat).format(new Date());
		}

		return newValue;
	}

	// Get valid Transaction channel for testing account transactions
	public static TransactionChannel getDemoTransactionChannel() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		List<TransactionChannel> transactionChannels = organizationService.getTransactionChannels();

		if (transactionChannels == null) {
			return null;
		}
		int randomIndex = (int) Math.round(Math.random() * (transactionChannels.size() - 1));
		randomIndex = 1;
		return transactionChannels.get(randomIndex);
	}

	// Helper to make valid TransactionDetails object for testing account transactions
	public static TransactionDetails makeDemoTransactionDetails() throws MambuApiException {

		TransactionChannel channel = getDemoTransactionChannel();

		if (channel == null) {
			return null;
		}

		// Create demo TransactionDetails
		TransactionDetails transactionDetails = new TransactionDetails(channel);
		List<ChannelField> channelFields = channel.getChannelFields();
		if (channelFields == null || channelFields.size() == 0) {
			return transactionDetails;
		}
		// Create random number for this transactionDetails values
		String randomNumber = String.valueOf((int) (Math.random() * 100000));
		for (ChannelField field : channelFields) {
			switch (field) {
			case ACCOUNT_NAME:
				transactionDetails.setAccountName("Account Name demo " + randomNumber);
				break;
			case ACCOUNT_NUMBER:
				transactionDetails.setAccountNumber("Account Number demo " + randomNumber);
				break;
			case BANK_NUMBER:
				transactionDetails.setBankNumber("Bank Number demo " + randomNumber);
				break;
			case CHECK_NUMBER:
				transactionDetails.setCheckNumber("Check Number demo " + randomNumber);
				break;
			case IDENTIFIER:
				transactionDetails.setIdentifier("Identifier demo " + randomNumber);
				break;
			case RECEPIT_NUMBER:
				transactionDetails.setReceiptNumber("Receipt Number demo " + randomNumber);
				break;
			case ROUTING_NUMBER:
				transactionDetails.setRoutingNumber("Routing Number demo " + randomNumber);
				break;

			}

		}
		return transactionDetails;
	}
}
