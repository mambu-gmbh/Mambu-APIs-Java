package demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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

	// Get Demo client by ID
	public static Client getDemoClient(String clientId) throws MambuApiException {
		System.out.println("\nIn getDemoClient for id=" + clientId);

		ClientsService clientsService = MambuAPIFactory.getClientService();
		Client client = clientsService.getClient(clientId);

		return client;

	}

	// Get Demo client details by ID
	public static ClientExpanded getDemoClientDetails(String clientId) throws MambuApiException {
		System.out.println("\nIn getDemoClient with details for id=" + clientId);

		ClientsService clientsService = MambuAPIFactory.getClientService();
		ClientExpanded client = clientsService.getClientDetails(clientId);

		return client;

	}

	// Get Demo group
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

	// Get Demo group by ID
	public static Group getDemoGroup(String groupId) throws MambuApiException {
		System.out.println("\nIn getDemoGroup for id=" + groupId);

		ClientsService clientsService = MambuAPIFactory.getClientService();
		Group group = clientsService.getGroup(groupId);

		return group;

	}

	// Get Demo group details by ID
	public static GroupExpanded getDemoGroupDetails(String groupId) throws MambuApiException {
		System.out.println("\nIn getDemoGroup for id=" + groupId);

		ClientsService clientsService = MambuAPIFactory.getClientService();
		GroupExpanded group = clientsService.getGroupDetails(groupId);

		return group;

	}

	// Get random active loan product.
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
		return products.get(randomIndex);

	}

	// Get specific loan product by ID
	public static LoanProduct getDemoLoanProduct(String productId) throws MambuApiException {
		System.out.println("\nIn getDemoLoanProduct by ID=" + productId);

		if (productId == null) {
			return getDemoLoanProduct();
		}
		LoansService service = MambuAPIFactory.getLoanService();
		LoanProduct product = service.getLoanProduct(productId);

		return product;

	}

	// Get random active savings product
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
		return products.get(randomIndex);

	}

	// Get specific savings product by ID
	public static SavingsProduct getDemoSavingsProduct(String productId) throws MambuApiException {
		System.out.println("\nIn getDemoSavingsProduct by ID=" + productId);

		if (productId == null) {
			return getDemoSavingsProduct();
		}

		SavingsService service = MambuAPIFactory.getSavingsService();
		SavingsProduct product = service.getSavingsProduct(productId);

		return product;
	}

	// Get random loan account
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

	// Get specific loan account by ID
	public static LoanAccount getDemoLoanAccount(String accountId) throws MambuApiException {
		System.out.println("\nIn getDemoLoanAccount by ID-" + accountId);

		if (accountId == null) {
			return getDemoLoanAccount();
		}
		LoansService service = MambuAPIFactory.getLoanService();
		LoanAccount account = service.getLoanAccountDetails(accountId);

		return account;
	}

	// Get random savings product
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

	// Get specific savings account by ID
	public static SavingsAccount getDemoSavingsAccount(String accountId) throws MambuApiException {
		System.out.println("\nIn getDemoSavingsAccount by ID=" + accountId);

		if (accountId == null) {
			return getDemoSavingsAccount();
		}

		SavingsService service = MambuAPIFactory.getSavingsService();
		SavingsAccount account = service.getSavingsAccountDetails(accountId);

		return account;
	}

	// Make new value for a CustomFieldValue with a known CustomFieldSet
	public static CustomFieldValue makeNewCustomFieldValue(CustomFieldSet set, CustomFieldValue value) {
		if (value == null) {
			return new CustomFieldValue();
		}
		return makeNewCustomFieldValue(set, value.getCustomField(), value);
	}

	// Make new value for a CustomFieldValue when only original value is available
	public static CustomFieldValue makeNewCustomFieldValue(CustomFieldValue value) {
		if (value == null) {
			return new CustomFieldValue();
		}
		CustomFieldSet set = null;
		return makeNewCustomFieldValue(set, value.getCustomField(), value);
	}

	// Helper to create a new, valid test value for a custom field value based on field's data type and initial value
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
			// Set demo string with the current date
			newValue = "Updated by API on " + new Date().toString();
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
		}
		value.setValue(newValue);
		value.setLinkedEntityKeyValue(linkedValue);
		return value;
	}

	// Get custom fields of a specific type and for the specific entity
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

	// Make valid test custom field values of a specific type and for the specific entity
	public static List<CustomFieldValue> makeForEntityCustomFieldValues(CustomFieldType customFieldType,
			String entityKey) throws MambuApiException {
		boolean requiredOnly = true;

		return makeForEntityCustomFieldValues(customFieldType, entityKey, requiredOnly);
	}

	// Make valid test custom field values of a specific type and for the specific entity
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

	// Helper to log custom field values
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

	// Helper to Log Custom Field set details
	public static void logCustomFieldSet(CustomFieldSet set) {
		List<CustomField> customFields = set.getCustomFields();
		System.out.println("\nSet Name=" + set.getName() + "\tType=" + set.getType().toString() + "  Total Fields="
				+ customFields.size() + "\tUsage=" + set.getUsage());
		System.out.println("List of fields");
		for (CustomField field : customFields) {
			DemoUtil.logCustomField(field);
		}
	}

	// Helper to Log CustomField - return field id for any existent active field
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

	// Get valid Transaction channel for testing account transactions
	public static TransactionChannel getDemoTransactionChannel() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		List<TransactionChannel> transactionChannels = organizationService.getTransactionChannels();

		if (transactionChannels == null) {
			return null;
		}
		int randomIndex = (int) Math.round(Math.random() * (transactionChannels.size() - 1));
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

	// Helper to encode File into base64 string
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

	// Helper to decode Base64 string into bytes
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

	// Helper to Create BufferedImage file from the input Base64 encoded string
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

}
