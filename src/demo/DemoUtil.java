package demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

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
import com.mambu.apisdk.util.DateUtils;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldDataType;
import com.mambu.core.shared.model.CustomFieldLink;
import com.mambu.core.shared.model.CustomFieldLink.LinkType;
import com.mambu.core.shared.model.CustomFieldSelection;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldSet.Usage;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.CustomFilterConstraint;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanProductType;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsType;

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
	static String demoClientLastName = "Doe"; // Doe Chernaya
	static String demoClientLastName2 = "Doe"; // Doe Chernaya
	static String demoClientFirstName = "John"; // John Irina
	static String demoClientFirstName2 = "Jane"; // Jane Irina
	static String demoUsername = "demo"; // demo

	static String demoClientId = null;
	static String demoGroupId = null;

	static String demoLaonAccountId = null;
	static String demoLaonProductId = null;
	// set demo product ID to "ALL_TYPES" to run tests for all product types
	public static final String allProductTypes = "ALL_TYPES";
	// Maintain static Map of Product Type to a list of products of this type
	static HashMap<LoanProductType, List<LoanProduct>> loansProductsMap;
	static HashMap<SavingsType, List<SavingsProduct>> savingsProductsMap;

	static String demoSavingsAccountId = null;
	static String demoSavingsProductId = null;

	static String demoLineOfCreditId = null;

	public static String exceptionLogPrefix = "*** Exception *** ";

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

			// Get IDs for demo entities defined in the property file
			getDemoEntities(prop);

		} catch (IOException e) {
			System.out.println("  Exception reading config.properties file in Demo Util Service");
			Logger.getAnonymousLogger().severe("Could not read file config.properties");

			e.printStackTrace();

		}
		// Set up Factory
		MambuAPIFactory.setUp(domain, user, password);

		// set up App Key
		MambuAPIFactory.setApplicationKey(appKeyValue);

		initData();
	}

	private static void initData() {
		loansProductsMap = null;
		savingsProductsMap = null;
	}

	public static final String demoLogPrefix = "DemoUtil data: ";

	// Get IDs for the demo entities defined in the Properties file
	private static void getDemoEntities(Properties properties) {
		if (properties == null) {
			System.out.println("Null  Properties file, cannot obtain demo data");
			return;
		}

		// Get Properties. For domain, user and demo client we can also use hardcoded defaults if not provided
		domain = properties.getProperty("domain", domain);
		user = properties.getProperty("user", domain);
		password = properties.getProperty("password", password);
		System.out.println(demoLogPrefix + "Domain=" + domain + "\tUser=" + user);

		// Get Demo User username
		demoUsername = makeNullIfEmpty(properties.getProperty("demoUsername", demoUsername));

		// Get Demo Client defines by first and last name
		demoClientFirstName = makeNullIfEmpty(properties.getProperty("demoClientFirstName", demoClientFirstName));
		demoClientLastName = makeNullIfEmpty(properties.getProperty("demoClientLastName", demoClientLastName));
		System.out.println(demoLogPrefix + "Username=" + demoUsername + "\tClient First Name=" + demoClientFirstName
				+ "\tClient Last Name=" + demoClientLastName);

		// Domain 2: Get Demo Client defines by first and last name
		demoClientFirstName2 = makeNullIfEmpty(properties.getProperty("demoClientFirstName", demoClientFirstName2));
		demoClientLastName2 = makeNullIfEmpty(properties.getProperty("demoClientLastName", demoClientLastName2));

		// Get Demo Client and Demo Group IDs
		demoClientId = makeNullIfEmpty(properties.getProperty("demoClientId"));
		demoGroupId = makeNullIfEmpty(properties.getProperty("demoGroupId"));
		System.out.println(demoLogPrefix + "Client ID=" + demoClientId + "\tGroup ID=" + demoGroupId);

		// Get Demo Loan and Demo Savings Product IDs
		demoLaonAccountId = makeNullIfEmpty(properties.getProperty("demoLaonAccountId")); // account
		demoLaonProductId = makeNullIfEmpty(properties.getProperty("demoLaonProductId")); // product
		System.out.println(demoLogPrefix + "Loan Account ID=" + demoLaonAccountId + "\tLoan Product ID="
				+ demoLaonProductId);

		// Get Demo Savings Account Demo Savings Product IDs
		demoSavingsAccountId = makeNullIfEmpty(properties.getProperty("demoSavingsAccountId")); // account
		demoSavingsProductId = makeNullIfEmpty(properties.getProperty("demoSavingsProductId")); // product
		System.out.println(demoLogPrefix + "Savings Account ID=" + demoSavingsAccountId + "\tSavings Product ID="
				+ demoSavingsProductId);

		// Get Demo Line Of Credit ID
		demoLineOfCreditId = makeNullIfEmpty(properties.getProperty("demoLineOfCreditId"));
		System.out.println(demoLogPrefix + "Line of Credit ID=" + demoLineOfCreditId);

	}

	// Helper to set properties parameter value to null if it is empty.
	// This would allow leaving undefined properties blank in the configuration file instead of commenting them out each
	// time
	private static String makeNullIfEmpty(String param) {
		if (param == null || param.trim().length() == 0) {
			return null;
		}
		return param;
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

	/**
	 * Get Demo User entity. Returns demo user specified as the "demoUsername" parameter of the configuration file
	 * (config.properties). Defaults to "demo"
	 * 
	 * @return user
	 * @throws MambuApiException
	 */
	public static User getDemoUser() throws MambuApiException {
		System.out.println("\nIn getDemoUser");

		UsersService usersService = MambuAPIFactory.getUsersService();
		User user = usersService.getUserByUsername(demoUsername);

		return user;
	}

	/**
	 * Get Demo Branch entity with full details. Returns random Mambu Branch with full details
	 * 
	 * @return branch with full details
	 * @throws MambuApiException
	 */
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

	/**
	 * Get Demo Centre entity with full details. Returns random Mambu Centre
	 * 
	 * @return centre
	 * @throws MambuApiException
	 */
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
	 * Get or Create a Demo Client of primary domain, delegate for {@link DemoUtil#getDemoClient(boolean)}. Demo client
	 * is retrieved or created using "demoClientFirstName" and "demoClientLastName" parameters of the configuration file
	 * 
	 * @return
	 * @throws MambuApiException
	 */
	public static Client getDemoClient() throws MambuApiException {
		return getDemoClient(false);
	}

	/**
	 * Get or Create a Demo Client. Demo client is retrieved or created using parameters specified in the configuration
	 * file: "demoClientFirstName" and "demoClientLastName" parameters are used for primary domain and
	 * "demoClientFirstName2" and "demoClientLastName2" are used for secondary domain
	 * 
	 * @param secondaryDomain
	 *            true if demo client of secondary domain is required, false for primary domain
	 * @return client
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

	/**
	 * Get Demo Client by client ID.
	 * 
	 * @param clientId
	 *            client ID. Can be null. If null, then "demoClientId" parameter specified in the configuration file is
	 *            used. If the configuration parameter is absent (or is empty) then a client specified in the
	 *            "demoClientFirstName" and "demoClientLastName" parameters for the corresponding domain is retrieved.
	 *            See {@link #getDemoClient(boolean)}
	 * @return client
	 * @throws MambuApiException
	 */
	public static Client getDemoClient(String clientId) throws MambuApiException {
		System.out.println("\nIn getDemoClient for id=" + clientId);

		// If clientId ID is null and nothing is specified in the configuration file then get by the first and last name
		if (clientId == null && demoClientId == null) {
			// Both are null, use a random one
			return getDemoClient();

		}
		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		clientId = (clientId != null) ? clientId : demoClientId;
		ClientsService clientsService = MambuAPIFactory.getClientService();
		Client client = clientsService.getClient(clientId);

		return client;

	}

	/**
	 * Get Demo client with full details by ID
	 * 
	 * @param clientId
	 *            client ID. Can be null. If null, then "demoClientId" parameter specified in the configuration file is
	 *            used. If the configuration parameter is absent (or is empty) then full details for a client specified
	 *            in the "demoClientFirstName" and "demoClientLastName" parameters for the corresponding domain are
	 *            retrieved. See {@link #getDemoClient(boolean)}
	 * @return client
	 * @throws MambuApiException
	 */
	public static ClientExpanded getDemoClientDetails(String clientId) throws MambuApiException {
		System.out.println("\nIn getDemoClient with details for id=" + clientId);

		ClientsService clientsService = MambuAPIFactory.getClientService();

		// If clientId ID is null and nothing is specified in the configuration file then get by the first and last name
		if (clientId == null && demoClientId == null) {
			// Both are null, get client ID for the client specified by a first/last name
			Client client = getDemoClient();
			clientId = client.getId();

		} else {
			// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
			clientId = (clientId != null) ? clientId : demoClientId;
		}
		ClientExpanded client = clientsService.getClientDetails(clientId);

		return client;

	}

	/**
	 * Get Demo group. Return random group
	 * 
	 * @return group
	 * @throws MambuApiException
	 */
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

	/**
	 * Get Demo group by ID
	 * 
	 * @param groupId
	 *            group ID. Can be null. If null, then "demoGroupId" parameter specified in the configuration file is
	 *            used. If the configuration parameter is absent (or is empty) then random group is returned. See
	 *            {@link #getDemoGroup())}
	 * @return group
	 * @throws MambuApiException
	 */
	public static Group getDemoGroup(String groupId) throws MambuApiException {
		System.out.println("\nIn getDemoGroup for id=" + groupId);

		// If groupId ID is null and nothing is specified in the configuration file then get a random one
		if (groupId == null && demoGroupId == null) {
			// Both are null, use a random one
			return getDemoGroup();

		}
		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		groupId = (groupId != null) ? groupId : demoGroupId;
		ClientsService clientsService = MambuAPIFactory.getClientService();
		Group group = clientsService.getGroup(groupId);

		return group;

	}

	/**
	 * Get Demo group with full details by ID
	 * 
	 * @param groupId
	 *            groupId ID. Can be null. If null, then "demoGroupId" parameter specified in the configuration file is
	 *            used. If the configuration parameter is absent (or is empty) then full details for a random group are
	 *            retrieved. See {@link #getDemoGroup())}
	 * @return group
	 * @throws MambuApiException
	 */
	public static GroupExpanded getDemoGroupDetails(String groupId) throws MambuApiException {
		System.out.println("\nIn getDemoGroup for id=" + groupId);

		ClientsService clientsService = MambuAPIFactory.getClientService();
		// If groupId ID is null and nothing is specified in the configuration file then get a random one
		if (groupId == null && demoGroupId == null) {
			// Both are null, use a random ID
			Group group = getDemoGroup();
			groupId = group.getId();

		} else {
			// One of them is not null. Use specified groupId if it is not not null, otherwise use demoGroupId
			groupId = (groupId != null) ? groupId : demoGroupId;
		}
		GroupExpanded group = clientsService.getGroupDetails(groupId);

		return group;

	}

	/**
	 * Get random active loan product
	 * 
	 * @return loan product
	 * @throws MambuApiException
	 */
	public static LoanProduct getDemoLoanProduct() throws MambuApiException {
		System.out.println("\nIn getDemoLoanProduct");

		LoansService service = MambuAPIFactory.getLoanService();

		// all products for our demo user
		List<LoanProduct> products = service.getLoanProducts("0", "5");
		if (products == null || products.size() == 0) {
			System.out.println("getDemoLoanProduct: no Loan products defined");
			return null;
		}

		List<LoanProduct> activeProducts = new ArrayList<LoanProduct>();
		for (LoanProduct product : products) {
			if (product.isActivated()) {
				activeProducts.add(product);
			}
		}
		if (activeProducts.size() == 0) {
			System.out.println("getDemoLoanProduct: no Active Loan products defined");
			return null;
		}

		int randomIndex = (int) (Math.random() * (activeProducts.size() - 1));
		return activeProducts.get(randomIndex);

	}

	/**
	 * Get loan product by ID
	 * 
	 * @param productId
	 *            product id. Can be null. If null, then "demoLaonProductId" parameter specified in the configuration
	 *            file is used. If the configuration parameter is absent (or is empty) then random loan product is
	 *            retrieved. See {@link #getDemoLoanProduct())}
	 * @return
	 * @throws MambuApiException
	 */
	public static LoanProduct getDemoLoanProduct(String productId) throws MambuApiException {
		System.out.println("\nIn getDemoLoanProduct by ID=" + productId);

		// If provided ID is null and nothing is specified in the configuration file then get a random one
		if (productId == null && demoLaonProductId == null) {
			// Both are null, use a random one
			return getDemoLoanProduct();

		}

		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		productId = (productId != null) ? productId : demoLaonProductId;
		// Check if the product ID is our reserved "All_Types" ID
		if (productId.equalsIgnoreCase(allProductTypes)) {
			// no specific product ID is available in the configuration file
			// Both are null, use a random one
			return getDemoLoanProduct();
		}
		LoansService service = MambuAPIFactory.getLoanService();
		LoanProduct product = service.getLoanProduct(productId);

		return product;

	}

	/**
	 * Get Demo Loan Product
	 * 
	 * @param productType
	 *            product type
	 * @return random product of the specified type
	 * @throws MambuApiException
	 */
	public static LoanProduct getDemoLoanProduct(LoanProductType productType) throws MambuApiException {
		if (productType == null) {
			return null;
		}
		if (loansProductsMap == null) {
			loansProductsMap = makeLoanProductsMap();
		}

		List<LoanProduct> products = loansProductsMap.get(productType);
		if (products == null || products.size() == 0) {
			System.out.println("WARNING: No active Loan products found for product Type=" + productType);
			return null;
		}
		int randomIndex = (int) (Math.random() * (products.size() - 1));
		return products.get(randomIndex);

	}

	// Make a map of LoanProductType to a list of active Loan Products of this type
	private static HashMap<LoanProductType, List<LoanProduct>> makeLoanProductsMap() throws MambuApiException {

		HashMap<LoanProductType, List<LoanProduct>> productsMap = new HashMap<>();

		LoansService loanService = MambuAPIFactory.getLoanService();
		List<LoanProduct> products = loanService.getLoanProducts("0", "500");
		if (products == null || products.size() == 0) {
			System.out.println("WARNING: No Loan products defined");
			return productsMap;
		}
		for (LoanProduct product : products) {
			if (!product.isActivated()) {
				continue;
			}
			LoanProductType productType = product.getLoanProductType();
			List<LoanProduct> thisTypeProducts = productsMap.get(productType);
			if (thisTypeProducts == null) {
				thisTypeProducts = new ArrayList<>();
				productsMap.put(productType, thisTypeProducts);
			}
			thisTypeProducts.add(product);
		}
		return productsMap;

	}

	/**
	 * Get Demo Savings Product
	 * 
	 * @param productType
	 *            product type
	 * @return random product of the specified type
	 * @throws MambuApiException
	 */
	public static SavingsProduct getDemoSavingsProduct(SavingsType productType) throws MambuApiException {
		if (productType == null) {
			return null;
		}
		if (savingsProductsMap == null) {
			savingsProductsMap = makeSavingsProductsMap();
		}

		List<SavingsProduct> products = savingsProductsMap.get(productType);
		if (products == null || products.size() == 0) {
			System.out.println("WARNING: No active Savings products found for product Type=" + productType);
			return null;
		}
		int randomIndex = (int) (Math.random() * (products.size() - 1));
		return products.get(randomIndex);

	}

	// Make a map of SavingsType to a list of active Savings Products of this type
	private static HashMap<SavingsType, List<SavingsProduct>> makeSavingsProductsMap() throws MambuApiException {

		HashMap<SavingsType, List<SavingsProduct>> productsMap = new HashMap<>();

		SavingsService service = MambuAPIFactory.getSavingsService();
		List<SavingsProduct> products = service.getSavingsProducts("0", "500");
		if (products == null || products.size() == 0) {
			System.out.println("WARNING: No Savings products defined");
			return productsMap;
		}
		for (SavingsProduct product : products) {
			if (!product.isActivated()) {
				continue;
			}
			SavingsType productType = product.getProductType();
			List<SavingsProduct> thisTypeProducts = productsMap.get(productType);
			if (thisTypeProducts == null) {
				thisTypeProducts = new ArrayList<>();
				productsMap.put(productType, thisTypeProducts);
			}
			thisTypeProducts.add(product);
		}
		return productsMap;

	}

	/**
	 * Get random active savings product
	 * 
	 * @return savings product
	 * @throws MambuApiException
	 */
	public static SavingsProduct getDemoSavingsProduct() throws MambuApiException {
		System.out.println("\nIn getDemoSavingsProduct");

		SavingsService service = MambuAPIFactory.getSavingsService();
		List<SavingsProduct> products = service.getSavingsProducts("0", "5");

		if (products == null || products.size() == 0) {
			System.out.println("getDemoSavingsProduct: no Savings products defined");
			return null;
		}
		List<SavingsProduct> activeProducts = new ArrayList<SavingsProduct>();
		for (SavingsProduct product : products) {
			if (product.isActivated()) {
				activeProducts.add(product);
			}
		}
		if (activeProducts.size() == 0) {
			System.out.println("getDemoSavingsProduct: no Active Savings products defined");
			return null;
		}

		int randomIndex = (int) (Math.random() * (activeProducts.size() - 1));
		return activeProducts.get(randomIndex);

	}

	/**
	 * Get savings product by ID
	 * 
	 * @param productId
	 *            product id. Can be null. If null, then "demoSavingsProductId" parameter specified in the configuration
	 *            file is used. If the configuration parameter is absent (or is empty) then random savings product is
	 *            retrieved. See {@link #getDemoSavingsProduct())}
	 * @return savings product
	 * @throws MambuApiException
	 */
	public static SavingsProduct getDemoSavingsProduct(String productId) throws MambuApiException {
		System.out.println("\nIn getDemoSavingsProduct by ID=" + productId);

		// If provided ID is null and nothing is specified in the configuration file then get a random one
		if (productId == null && demoSavingsProductId == null) {
			// Both are null, use a random one
			return getDemoSavingsProduct();

		}
		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		productId = (productId != null) ? productId : demoSavingsProductId;
		// Check if the product ID is our reserved "All_Types" ID
		if (productId.equalsIgnoreCase(allProductTypes)) {
			// no specific product ID is available in the configuration file
			// Both are null, use a random one
			return getDemoSavingsProduct();
		}
		SavingsService service = MambuAPIFactory.getSavingsService();
		SavingsProduct product = service.getSavingsProduct(productId);

		return product;
	}

	/**
	 * Get random loan account
	 * 
	 * @return loan account
	 * @throws MambuApiException
	 */
	public static LoanAccount getDemoLoanAccount() throws MambuApiException {
		System.out.println("\nIn getDemoLoanAccount");

		LoansService service = MambuAPIFactory.getLoanService();
		List<LoanAccount> loans = service.getLoanAccountsByBranchOfficerState(null, demoUsername, null, "0", "5");

		if (loans != null && loans.size() > 0) {
			int randomIndex = (int) (Math.random() * (loans.size() - 1));
			return loans.get(randomIndex);
		}

		System.out.println("getDemoLoanAccount: no Loan Accounts the Demo User exist");

		return null;
	}

	/**
	 * Get loan account by ID
	 * 
	 * @param accountId
	 *            account ID. Can be null. If null, then "demoLaonAccountId" parameter specified in the configuration
	 *            file is used. If the configuration parameter is absent (or is empty) then random loan account is
	 *            retrieved. See {@link #getDemoLoanAccount())}
	 * @return loan account
	 * @throws MambuApiException
	 */
	public static LoanAccount getDemoLoanAccount(String accountId) throws MambuApiException {
		System.out.println("\nIn getDemoLoanAccount by ID-" + accountId);

		// If provided ID is null and nothing is specified in the configuration file then get a random one
		if (accountId == null && demoLaonAccountId == null) {
			// Both are null, use a random one
			return getDemoLoanAccount();

		}
		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		accountId = (accountId != null) ? accountId : demoLaonAccountId;
		LoansService service = MambuAPIFactory.getLoanService();
		LoanAccount account = service.getLoanAccountDetails(accountId);

		return account;
	}

	/**
	 * Get random savings account
	 * 
	 * @return savings account
	 * @throws MambuApiException
	 */
	public static SavingsAccount getDemoSavingsAccount() throws MambuApiException {
		System.out.println("\nIn getDemoSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();
		List<SavingsAccount> savings = service.getSavingsAccountsByBranchOfficerState(null, demoUsername, null, "0",
				"5");

		if (savings != null && savings.size() > 0) {
			int randomIndex = (int) (Math.random() * (savings.size() - 1));
			return savings.get(randomIndex);
		}

		System.out.println("getDemoSavingsAccount: no Savings Accounts for the Demo User exist");

		return null;
	}

	/**
	 * Get savings account by ID
	 * 
	 * @param accountId
	 *            account ID. Can be null. If null, then "demoSavingsAccountId" parameter specified in the configuration
	 *            file is used. If the configuration parameter is absent (or is empty) then random savings account is
	 *            retrieved. See {@link #getDemoSavingsAccount())}
	 * @return
	 * @throws MambuApiException
	 */
	public static SavingsAccount getDemoSavingsAccount(String accountId) throws MambuApiException {
		System.out.println("\nIn getDemoSavingsAccount by ID=" + accountId);
		// If provided ID is null and nothing is specified in the configuration file then get a random one
		if (accountId == null && demoSavingsAccountId == null) {
			// Both are null, use a random one
			return getDemoSavingsAccount();

		}
		// Use the provided ID if it is not null, otherwise use the one defined in the configuration file
		accountId = (accountId != null) ? accountId : demoSavingsAccountId;
		SavingsService service = MambuAPIFactory.getSavingsService();
		SavingsAccount account = service.getSavingsAccountDetails(accountId);

		return account;
	}

	/**
	 * Make new value for a CustomFieldValue with a known Custom Field Set
	 * 
	 * @param set
	 *            custom field set
	 * @param value
	 *            custom field value
	 * @return new custom field value
	 */
	public static CustomFieldValue makeNewCustomFieldValue(CustomFieldSet set, CustomFieldValue value) {
		if (value == null) {
			return new CustomFieldValue();
		}
		return makeNewCustomFieldValue(set, value.getCustomField(), value);
	}

	/**
	 * Make new value for an existent Custom Field Value
	 * 
	 * @param value
	 *            custom field value
	 * @return new custom field value
	 */
	public static CustomFieldValue makeNewCustomFieldValue(CustomFieldValue value) {
		if (value == null) {
			return new CustomFieldValue();
		}
		CustomFieldSet set = null;
		return makeNewCustomFieldValue(set, value.getCustomField(), value);
	}

	/**
	 * Helper to create a new valid custom field value based on field's data type from an existent custom field value
	 * 
	 * @param set
	 *            custom field set
	 * @param customField
	 *            custom field
	 * @param initialField
	 *            initial custom field value
	 * @return custom field value
	 */
	private static CustomFieldValue makeNewCustomFieldValue(CustomFieldSet set, CustomField customField,
			CustomFieldValue initialField) {

		if (customField == null) {
			return new CustomFieldValue();
		}
		String fieldId = customField.getId();
		CustomFieldDataType fieldType = customField.getDataType();

		CustomFieldValue value = new CustomFieldValue();
		value.setCustomFieldId(customField.getId());
		// Set group index from current value
		Integer groupIndex = (initialField == null) ? null : initialField.getCustomFieldSetGroupIndex();
		value.setCustomFieldSetGroupIndex(groupIndex);

		// For Grouped custom field values we need also to set Group Index. See MBU-7511
		if (groupIndex == null && set != null && set.getUsage() == Usage.GROUPED) {
			value.setCustomFieldSetGroupIndex(0);
		}

		String initialValue = (initialField == null) ? null : initialField.getValue();
		String newValue = null; // custom field value
		String linkedValue = null; // linked value
		switchloop: switch (fieldType) {
		case STRING:
			// Consider field's validation pattern, if defined. See MBU-8973
			String pattern = customField.getValidationPattern();
			if (pattern != null && pattern.length() > 0) {
				newValue = new String(pattern);
				newValue = newValue.replaceAll("@", "A");
				newValue = newValue.replaceAll("#", "1");
				newValue = newValue.replaceAll("\\$", "B");
			} else {
				// Set demo string with the current date
				newValue = "Updated by API on " + new Date().toString();
			}
			break;
		case NUMBER:
			// Increase current numeric value by 10
			newValue = (initialValue == null) ? "10" : String.valueOf(Float.parseFloat(initialValue) + 10);
			break;
		case CHECKBOX:
			// Change the check box's value to opposite
			newValue = (initialValue == null) ? "TRUE" : (initialValue.equals("TRUE")) ? "FALSE" : "TRUE";
			break;
		case SELECTION:
			// Change selection to any other allowed selection (if more than one is defined)
			// For selection fields use CustomFieldSelection class, available since 3.10, see MBU-7914
			List<CustomFieldSelection> selectionOptions = customField.getCustomFieldSelectionOptions();
			if (selectionOptions == null || selectionOptions.size() == 0) {
				System.out.println("WARNING: Cannot update selection value as no values are now defined for field ID="
						+ fieldId);
				newValue = null;
				break switchloop;
			}
			for (CustomFieldSelection option : selectionOptions) {
				String selectionValue = option.getValue();
				if (initialValue == null || !initialValue.equalsIgnoreCase(selectionValue)) {
					newValue = selectionValue;
					break switchloop;
				}
			}
			System.out.println("WARNING: Cannot update selection value as only one value is defined for field ID="
					+ fieldId);
			newValue = null;
			break;
		case DATE:
			// return current date as new value
			newValue = DateUtils.FORMAT.format(new Date());
			break;
		case CLIENT_LINK:
			try {
				Client client = getDemoClient();
				linkedValue = client.getEncodedKey();
			} catch (MambuApiException e) {
				linkedValue = null;
			}
			break;
		case GROUP_LINK:
			try {
				Group group = getDemoGroup();
				linkedValue = group.getEncodedKey();
			} catch (MambuApiException e) {
				linkedValue = null;
			}
			break;
		case USER_LINK:
			// USER_LINK type: see MBU-8966 in 3.12
			try {
				User user = getDemoUser();
				linkedValue = user.getEncodedKey();
			} catch (MambuApiException e) {
				linkedValue = null;
			}

			break;
		}
		value.setValue(newValue);
		value.setLinkedEntityKeyValue(linkedValue);
		return value;
	}

	/**
	 * Get custom fields of a specific type and for the specific entity type
	 * 
	 * @param set
	 *            custom field set
	 * @param entityKey
	 *            entity key for the applicable custom fields
	 * @return active fields from the custom field set applicable to the specified entityKey
	 * @throws MambuApiException
	 */
	public static List<CustomField> getForEntityCustomFields(CustomFieldSet set, String entityKey)
			throws MambuApiException {

		List<CustomField> customFields = new ArrayList<CustomField>();
		// get required and default custom fields for the specified entity key
		List<CustomField> fields = set.getCustomFields();
		if (fields == null || fields.size() == 0) {
			return customFields;
		}
		for (CustomField field : fields) {
			if (!field.isAvailableForEntity(entityKey) || field.isDeactivated()) {
				continue;
			}
			// Add this field
			customFields.add(field);
		}

		return customFields;
	}

	/**
	 * Make valid test custom field values of a specific type and for the specific entity. Delegate for
	 * {@link DemoUtil#makeForEntityCustomFieldValues(CustomFieldType, String, boolean)}
	 * 
	 * @param customFieldType
	 *            custom fields type
	 * @param entityKey
	 *            entity key
	 * @return a list of custom field values with field values set according to the custom field's data type
	 * @throws MambuApiException
	 */
	public static List<CustomFieldValue> makeForEntityCustomFieldValues(CustomFieldType customFieldType,
			String entityKey) throws MambuApiException {
		boolean requiredOnly = true;

		return makeForEntityCustomFieldValues(customFieldType, entityKey, requiredOnly);
	}

	/**
	 * Make valid test custom field values of a specific type and for the specific entity
	 * 
	 * @param customFieldType
	 *            custom fields type
	 * @param entityKey
	 *            entity key
	 * @param requiredOnly
	 *            boolean indicating if only required fields should be returned
	 * @return a list of custom field values with values set according to the custom field's data type
	 * @throws MambuApiException
	 */
	public static List<CustomFieldValue> makeForEntityCustomFieldValues(CustomFieldType customFieldType,
			String entityKey, boolean requiredOnly) throws MambuApiException {

		List<CustomFieldValue> customFieldValues = new ArrayList<CustomFieldValue>();
		// Get all Custom Field sets
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		List<CustomFieldSet> sets = organizationService.getCustomFieldSets(customFieldType);

		if (sets == null || sets.size() == 0) {
			System.out.println("No Custom Field Sets found for type=" + customFieldType);
			return customFieldValues;
		}
		// Process each set
		for (CustomFieldSet set : sets) {
			List<CustomField> forEntityCustomFields = getForEntityCustomFields(set, entityKey);
			List<CustomFieldValue> customInformation = new ArrayList<CustomFieldValue>();

			for (CustomField field : forEntityCustomFields) {
				// return only required fields if requested so
				if (requiredOnly && !field.isRequired(entityKey)) {
					continue;
				}
				//
				CustomFieldValue fieldValue = makeNewCustomFieldValue(set, field, null);
				customInformation.add(fieldValue);

			}
			customFieldValues.addAll(customInformation);

		}

		return customFieldValues;

	}

	/**
	 * Helper to log custom field values
	 * 
	 * @param customFieldValues
	 *            a list of custom field values
	 * @param name
	 *            entity name
	 * @param entityId
	 *            entity id
	 */
	public static void logCustomFieldValues(List<CustomFieldValue> customFieldValues, String name, String entityId) {
		System.out.println("\nCustom Field Values for entity " + name + " with id=" + entityId);
		for (CustomFieldValue fieldValue : customFieldValues) {

			System.out.println("\nCustom Field Name=" + fieldValue.getCustomField().getName() + "\tValue="
					+ fieldValue.getValue() + "\tAmount=" + fieldValue.getAmount() + "\tLinked Entity Key="
					+ fieldValue.getLinkedEntityKeyValue());
			Integer groupIndex = fieldValue.getCustomFieldSetGroupIndex();
			if (groupIndex != null) {
				System.out.println("Group Index=" + groupIndex);
			}

			CustomField field = fieldValue.getCustomField();
			logCustomField(field);

		}
	}

	/**
	 * Helper to Log Custom Field Set details
	 * 
	 * @param set
	 *            custom fields set
	 */
	public static void logCustomFieldSet(CustomFieldSet set) {
		List<CustomField> customFields = set.getCustomFields();
		System.out.println("\nSet Name=" + set.getName() + "\tType=" + set.getType().toString() + "  Total Fields="
				+ customFields.size() + "\tUsage=" + set.getUsage());
		System.out.println("List of fields");
		for (CustomField field : customFields) {
			DemoUtil.logCustomField(field);
		}
	}

	/**
	 * Helper to Log a Custom Field. Returns field id if the field is active. Otherwise, returns null
	 * 
	 * @param field
	 *            custom field
	 * @return field id for one of the active fields or null if not an active field
	 */
	public static String logCustomField(CustomField field) {
		if (field == null) {
			return null;
		}
		String activeId = null;
		System.out.println("Field ID=" + field.getId() + "\tField Name=" + field.getName() + "\tDataType="
				+ field.getDataType().toString() + "\tIsDefault=" + field.isDefault().toString() + "\tType="
				+ field.getType().toString() + "\tIs Active=" + !field.isDeactivated());

		// Remember one of the active CustomFields for testing testGetCustomField()
		if (!field.isDeactivated()) {
			activeId = field.getId();
		}

		// As of Mambu 3.9, settings for custom fields are per entity type, see MBU-7034
		List<CustomFieldLink> links = field.getCustomFieldLinks();
		if (links == null || links.size() == 0) {
			System.out.println("Field's CustomFieldLinks are empty");
			if (links == null) {
				links = new ArrayList<CustomFieldLink>();
			}
		}
		for (CustomFieldLink link : links) {
			LinkType linkType = link.getLinkType(); // PRODUCT or CLIENT_ROLE
			String entityLinkedKey = link.getEntityLinkedKey();
			boolean isLinkDefault = link.isDefault();
			boolean isLinkRequired = link.isRequired();
			System.out.println("Link Data. Type=" + linkType + "\tEntity Key=" + entityLinkedKey + "\tRequired="
					+ isLinkRequired + "\tDefault=" + isLinkDefault);

			// Test Get field properties for this entity
			boolean isAvailableForEntity = field.isAvailableForEntity(entityLinkedKey);
			boolean isRequiredForEntity = field.isRequired(entityLinkedKey);
			boolean isDefaultForEntity = field.isDefault(entityLinkedKey);
			System.out.println("Available =" + isAvailableForEntity + "\tRequired=" + isRequiredForEntity
					+ "\tDefault=" + isDefaultForEntity);
		}
		// Log Custom Field selection options and dependencies
		// Dependent Custom fields are available since 3.10 (see MBU-7914)
		List<CustomFieldSelection> customFieldSelectionOptions = field.getCustomFieldSelectionOptions();
		if (customFieldSelectionOptions != null && customFieldSelectionOptions.size() > 0) {
			for (CustomFieldSelection option : customFieldSelectionOptions) {
				System.out.println("\nSelection Options:");
				String value = option.getValue();
				System.out.println("Value =" + value + "\tKey=" + option.getEncodedKey());
				CustomFilterConstraint constraint = option.getConstraint();
				if (constraint != null) {
					if (!field.isDeactivated()) {
						activeId = field.getId();
					}
					System.out.println("Value =" + value + "\tdepends on field=" + constraint.getCustomFieldKey()
							+ "\twith valueKey=" + constraint.getValue());
				}
			}
		}
		return activeId;

	}

	/**
	 * Get random Transaction channel
	 * 
	 * @return transaction channel
	 * @throws MambuApiException
	 */
	public static TransactionChannel getDemoTransactionChannel() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		List<TransactionChannel> transactionChannels = organizationService.getTransactionChannels();

		if (transactionChannels == null) {
			return null;
		}
		int randomIndex = (int) Math.round(Math.random() * (transactionChannels.size() - 1));
		return transactionChannels.get(randomIndex);
	}

	/**
	 * Helper to make valid TransactionDetails object for testing account transactions. This method retrieves random
	 * transaction channel and creates TransactionDetails with the fields applicable to the channel
	 * 
	 * @return Transaction Details
	 * @throws MambuApiException
	 */
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

	/**
	 * Helper to encode File into base64 string
	 * 
	 * @param absolutePath
	 *            file's absolute path
	 * @return
	 */
	public static String encodeFileIntoBase64String(String absolutePath) {
		final String methodName = "encodeFileIntoBase64String";

		System.out.println("Encoding image file=" + absolutePath);

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(absolutePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Picture file not found. Path=" + absolutePath);
			return null;
		}

		// Convert file to bytes stream
		byte[] bytes;
		byte[] buffer = new byte[8192];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println(methodName + " IO Exception reading file=" + absolutePath + " Message=" + e.getMessage());
			return null;
		}
		bytes = output.toByteArray();

		// Encode the byte stream.
		// No need to encode URL safe for JSON. And to be consistent with Mambu (which returns standard, i.e. non
		// url-safe encoding)
		String encodedString = Base64.encodeBase64String(bytes);
		boolean isBase64 = Base64.isArrayByteBase64(encodedString.getBytes());
		System.out.println("Encoded document. Is String Base64=" + isBase64);

		// Close open streams
		try {
			inputStream.close();
			output.close();
		} catch (IOException e) {

		}
		return encodedString;

	}

	/**
	 * Helper to decode Base64 string into byte array
	 * 
	 * @param inputStringBase64
	 *            base64 string
	 * @return byte array
	 */
	public static byte[] decodeBase64IntoBytes(String inputStringBase64) {
		System.out.println("\nIn decodeBase64IntoBytes");
		if (inputStringBase64 == null) {
			System.out.println("Input is NULL");
			return null;
		}
		// Mambu API returns encoded string in chunks separated by \r\n. Remove them
		inputStringBase64 = inputStringBase64.replaceAll("(\\\\r)?\\\\n", "");

		Base64 decoder = new Base64();
		byte[] decodedBytes = decoder.decode(inputStringBase64);

		if (decodedBytes == null) {
			return null;
		}
		System.out.println("decodeBase64: Decoded byte stream length=" + decodedBytes.length);
		return decodedBytes;

	}

	/**
	 * Helper to Create BufferedImage file from the input Base64 encoded string
	 * 
	 * @param inputStringBase64
	 *            base64 string
	 * @return buffered image
	 * @throws IOException
	 */
	public static BufferedImage decodeBase64(String inputStringBase64) throws IOException {
		System.out.println("\nIn decodeBase64");

		byte[] decodedBytes = decodeBase64IntoBytes(inputStringBase64);

		if (decodedBytes == null) {
			System.out.println("decodeBase64: cannot decode string");
			return null;
		}

		// Create BufferedImage
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
		return image;
	}

	/**
	 * Create a Calendar for a UTC midnight date corresponding to the current local date
	 * 
	 * @return UTC midnight date
	 */
	public static Calendar getCalendarForMidnightUTC() {
		Calendar date = Calendar.getInstance();
		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		date.setTimeZone(TimeZone.getTimeZone("UTC"));
		return date;
	}

	/**
	 * Create a Date as a UTC midnight date corresponding to the current local date
	 * 
	 * @return UTC midnight date
	 */
	public static Date getAsMidnightUTC() {
		return getCalendarForMidnightUTC().getTime();

	}

	/**
	 * Log exception message using a standard pattern for ease of retrieving of exception messages
	 */

	public static void logException(String methodName, MambuApiException exception) {
		if (exception == null) {
			return;
		}
		System.out.println(exceptionLogPrefix + " " + methodName + " Message: " + exception.getMessage());
	}

}
