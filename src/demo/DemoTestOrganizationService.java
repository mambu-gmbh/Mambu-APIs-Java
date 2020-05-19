package demo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLAccountingRule;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.admin.shared.model.ExchangeRate;
import com.mambu.api.server.handler.settings.organization.model.JSONOrganization;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.IdentificationDocumentTemplate;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.DateFormatType;
import com.mambu.core.shared.model.GeneralSettings;
import com.mambu.core.shared.model.IndexRate;
import com.mambu.core.shared.model.ObjectLabel;
import com.mambu.core.shared.model.Organization;
import com.mambu.core.shared.model.UsageRights;
import com.mambu.core.shared.model.User;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestOrganizationService {

	private static String BRANCH_ID;
	private static String CENTRE_ID;
	private static String CUSTOM_FIELD_ID;

	private static User demoUser;
	private static Centre demoCentre;
	private static String methodName = null; // print method name on exception

	public static void main(String[] args) {

		DemoUtil.setUpWithBasicAuth();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoCentre = DemoUtil.getDemoCentre();

			testGetOrganizationDetails();// Available since 3.11

			// Test GET all currencies
			List<Currency> organizationCurrencies = testGetCurrency(); // Available since 4.2

			// Available since 4.3
			testGetCurrencyByCode();

			// Test GET exchange rates
			testGetExchangeRates(organizationCurrencies); // Available since 4.2
			// Test POST exchange rate
			testPostExchangeRate(organizationCurrencies); // Available since 4.2

			testPostExchangeRateWithNullStartDate(organizationCurrencies); // Available since 4.2

			testPostIndexInterestRate(); // Available since 3.10

			testGetTransactionChannels(); // Available since 3.7

			testGetAllBranches();

			testGetCustomFieldSetsByType();
			testGetCustomField();

			testGetCentresByPage();
			testGetCentre();

			testGetAllBranches();
			testGetCentresByBranch();

			testGetBranchesByPage();

			testGetBranch();

			testUpdateDeleteCustomFields(); // Available since 3.8

			testGetIDDocumentTemplates(); // Available since 3.10.5

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Organization Service");
			System.out.println("Exception Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetAllBranches() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "1";
		String limit = "3";
		Date d1 = new Date();
		List<Branch> branches = organizationService.getBranches(offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("All Total=" + branches.size() + " Total time=" + diff);
		for (Branch branch : branches) {
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
			Address address = branch.getAddress();
			if (address != null)
				System.out.println(" And address=" + address.getLine1());
		}
		System.out.println();

	}

	public static void testGetBranchesByPage() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "0";
		String limit = "500";
		System.out.println("\nIn " + methodName + "  Offset=" + offset + "  Limit=" + limit);

		Date d1 = new Date();
		List<Branch> branches = organizationService.getBranches(offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Branches=" + branches.size() + " Total time=" + diff);
		BRANCH_ID = null;
		for (Branch branch : branches) {
			if (BRANCH_ID == null) {
				BRANCH_ID = branch.getId();
			}
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
		}
		System.out.println();

	}

	public static void testGetBranch() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Branch branch = organizationService.getBranch(BRANCH_ID);

		if (branch != null)
			System.out.println("Branch id=" + BRANCH_ID + " found. Returned:  ID=" + branch.getId() + "   Name="
					+ branch.getName());
		else
			System.out.println("Not Found Branch id=" + BRANCH_ID);
	}

	public static void testGetCentre() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String centreId = demoCentre.getId();
		System.out.println("\nIn " + methodName + " by ID." + "  Centre ID=" + centreId);

		Date d1 = new Date();
		Centre centre = organizationService.getCentre(centreId);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Centre: Name=" + centre.getName() + " BranchId=" + centre.getAssignedBranchKey()
				+ " Total time=" + diff);

	}

	public static void testGetCentresByPage() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "0";
		String limit = "5";
		String branchId = null;
		System.out.println("\nIn " + methodName + "  Offset=" + offset + "  Limit=" + limit);

		Date d1 = new Date();
		List<Centre> centres = organizationService.getCentres(branchId, offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Centres=" + centres.size() + " Total time=" + diff);
		CENTRE_ID = null;
		for (Centre centre : centres) {
			if (CENTRE_ID == null) {
				CENTRE_ID = centre.getId();
			}
			System.out.println(" Name=" + centre.getName() + "\tId=" + centre.getId());
		}
		System.out.println();

	}

	public static void testGetCentresByBranch() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String branchId = BRANCH_ID;
		String offset = "0";
		String limit = "500";
		System.out.println("\nIn " + methodName + "  BranchID=" + branchId + "  Offset=" + offset + "  Limit=" + limit);

		Date d1 = new Date();
		List<Centre> centres = organizationService.getCentres(branchId, offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Centres=" + centres.size() + " for branch=" + branchId + ". Total time=" + diff);
		for (Centre centre : centres) {
			System.out.println(" Name=" + centre.getName() + "\tId=" + centre.getId());
		}
		System.out.println();

	}

	/**
	 * Test get all available currencies and test getting base currency only
	 * 
	 * @return a list of organization currencies
	 * @throws MambuApiException
	 */
	public static List<Currency> testGetCurrency() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// Test getting ALL currencies
		List<Currency> organizationCurrencies = organizationService.getAllCurrencies();
		// Log the results
		System.out.println("Total Currencies =" + organizationCurrencies.size());
		for (Currency currency : organizationCurrencies) {
			System.out.println("\tCurrency code=" + currency.getCode() + "   Name=" + currency.getName());
		}

		// Test getting the base currency only (for backward compatibility)
		Currency baseCurrency = organizationService.getCurrency();
		System.out.println("\tBase Currency code=" + baseCurrency.getCode() + "   Name=" + baseCurrency.getName());

		return organizationCurrencies;
	}

	/**
	 * Tests getting a currency by its currency code
	 */
	private static void testGetCurrencyByCode() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Currency baseCurrency = organizationService.getCurrency();

		Currency currency = organizationService.getCurrency(baseCurrency.getCode());

		System.out.println("\nCurrency code=" + currency.getCode() + "   Name=" + currency.getName()
				+ "  Currency symbol=" + currency.getSymbol());

	}

	// Tests creating of next exchange rate for a currency
	private static void testPostExchangeRate(List<Currency> organizationCurrencies) throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		Date start = new Date();

		// Get one currency to test POST exchange rate
		Currency currncyForTest = getRandomCurrencyOtherThanBase(organizationCurrencies);

		// return if there are no other currencies or just the base currency
		if (currncyForTest == null) {
			System.out.println("WARNING: No Foreign Currency found to test POST Exchange Rate API ");
			return;
		}

		// create the next day`s exchange rate for the currency
		ExchangeRate exchangeRate = createExchangeRate(currncyForTest);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		// POST the exchange rate in Mambu
		ExchangeRate postedExchangeRate = organizationService.createExchangeRate(currncyForTest, exchangeRate);
		System.out.println("The details of the created ExchangeRate are: ");
		logExchangeRateDetails(postedExchangeRate);

		Date end = new Date();
		System.out.println("\n" + methodName + " took " + (end.getTime() - start.getTime()) + " milliseconds");

		testGetCurrentExchangeRate(postedExchangeRate);
	}

	/**
	 * 
	 * Tests POSTing in Mambu of a exchange rate with null start date.
	 * 
	 * @param organizationCurrencies
	 *            A list of all currencies available for the organization
	 * @throws MambuApiException
	 */
	private static void testPostExchangeRateWithNullStartDate(List<Currency> organizationCurrencies)
			throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		// Get one currency to test POST exchange rate
		Currency currncyForTest = getRandomCurrencyOtherThanBase(organizationCurrencies);

		// return if there are no other currencies or just the base currency
		if (currncyForTest == null) {
			System.out.println("WARNING: No Foreign Currency found to test POST Exchange Rate API ");
			return;
		}
		ExchangeRate exchangeRate = createExchangeRate(currncyForTest);
		// set the startDate to be null
		exchangeRate.setStartDate(null);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// POST the exchange rate in Mambu
		ExchangeRate postedExchangeRate = organizationService.createExchangeRate(currncyForTest, exchangeRate);
		System.out.println("The details of the created ExchangeRate are: ");
		logExchangeRateDetails(postedExchangeRate);

		testGetCurrentExchangeRate(postedExchangeRate);
	}

	/**
	 * Tests getting the current exchange rate against the last posted exchange date passed as parameter when calling
	 * this method.
	 * 
	 * @param lastPostedExchangeRate
	 *            The last posted exchange rate. Is used in comparison to see if it really the current exchange rate.
	 * @throws MambuApiException
	 */
	private static void testGetCurrentExchangeRate(ExchangeRate lastPostedExchangeRate) throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		if (lastPostedExchangeRate == null) {
			throw new IllegalArgumentException("The lastPostedExchangeRate parameter must not be null");
		}

		// Get the current exchange rate
		List<ExchangeRate> exchangeRates = organizationService
				.getExchangeRates(lastPostedExchangeRate.getToCurrencyCode(), null, null, 0, 1);

		ExchangeRate currentExchangeRate;
		if (exchangeRates != null && !exchangeRates.isEmpty() && exchangeRates.get(0).getEndDate() == null) {
			currentExchangeRate = exchangeRates.get(0);
			logExchangeRateDetails(currentExchangeRate);
		} else {
			throw new MambuApiException(new Exception(
					"Current exchange rate for " + lastPostedExchangeRate.getToCurrencyCode() + " was not found"));
		}

		// test to see the identity of the exchange rate using encoded keys
		if (!lastPostedExchangeRate.getEncodedKey().equals(currentExchangeRate.getEncodedKey())) {
			System.out.println("POST: " + lastPostedExchangeRate.getEncodedKey());
			System.out.println("GET:  " + currentExchangeRate.getEncodedKey());
			throw new MambuApiException(new Exception("POSTed and GET(got) exchange rate is not the same"));
		}

		System.out.println("Current exchange rate was successfully retrieved from Mambu");
	}

	/**
	 * Logs to console the details for the ExchangeRrate passed passed as argument
	 * 
	 * @param exchangeRate
	 *            The ExchangeRate
	 */
	private static void logExchangeRateDetails(ExchangeRate exchangeRate) {

		if (exchangeRate != null) {
			System.out.println("Key: " + exchangeRate.getEncodedKey());
			System.out.println("SellRate: " + exchangeRate.getSellRate());
			System.out.println("BuyRate: " + exchangeRate.getBuyRate());
			System.out.println("StartDate: " + exchangeRate.getStartDate());
			System.out.println("EndDate: " + exchangeRate.getEndDate());
			System.out.println("ToCurrency: " + exchangeRate.getToCurrencyCode());
		}
	}

	/**
	 * Iterates over all the currencies for the organization and gets the first one it finds which is not base currency.
	 * 
	 * @param currencies
	 *            all organization currencies
	 * @return a currency or null if there are no other currencies than base currency or no currency at all.
	 * @throws MambuApiException
	 */
	private static Currency getRandomCurrencyOtherThanBase(List<Currency> currencies) throws MambuApiException {

		if (currencies == null) {
			System.out.println("WARNING:NULL currencies, cannot get foreign currency");
			return null;
		}
		Currency currncyForTest = null;
		// iterate over all currencies and pick one
		for (Currency currency : currencies) {
			// pick one currency that is not the base currency
			if (!currency.isBaseCurrency()) {
				currncyForTest = currency;
				break;
			}
		}
		return currncyForTest;
	}

	/**
	 * Creates the exchange rate for the current day.
	 * 
	 * @param currncy
	 *            The currency that the exchange rate is created for.
	 * @return ExchangeRate for the current day.
	 */
	private static ExchangeRate createExchangeRate(Currency currncy) {

		Calendar currentDate = Calendar.getInstance();
		ExchangeRate exchangeRate = new ExchangeRate();
		exchangeRate.setBuyRate(new BigDecimal("3.00"));
		exchangeRate.setToCurrencyCode(currncy.getCode());
		exchangeRate.setStartDate(currentDate.getTime());
		exchangeRate.setSellRate(new BigDecimal("4.50"));
		System.out.println("Created exchange Rate with Start date=" + exchangeRate.getStartDate() + "\tCurrent Time="
				+ new Date());
		return exchangeRate;
	}

	// Get Custom Field by ID
	public static void testGetCustomField() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String fieldId = CUSTOM_FIELD_ID;
		System.out.println(methodName = "\nIn " + methodName + "  Field ID=" + fieldId);

		Date d1 = new Date();

		CustomField customField = organizationService.getCustomField(fieldId);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("CustomField: ID=" + customField.getId() + "\tName=" + customField.getName()
				+ " \tData Type=" + customField.getDataType().name() + " Total time=" + diff);

	}

	// Get CustomFieldSets by Type
	public static void testGetCustomFieldSetsByType() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// E.g. CustomField.Type.CLIENT_INFO, CustomField.Type.LOAN_ACCOUNT_INFO, etc
		CustomFieldType customFieldType = CustomFieldType.TRANSACTION_CHANNEL_INFO;

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName + " for " + customFieldType);

		Date d1 = new Date();

		List<CustomFieldSet> sustomFieldSets = organizationService.getCustomFieldSets(customFieldType);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Sets returned=" + sustomFieldSets.size() + " Total time=" + diff);
		for (CustomFieldSet set : sustomFieldSets) {
			List<CustomField> customFields = set.getCustomFields();

			System.out.println("\nSet Name=" + set.getName() + "\tType=" + set.getType().toString() + "\tBuiltInType="
					+ set.getBuiltInType() + "\nTotal Fields=" + customFields.size() + "\tUsage=" + set.getUsage());
			System.out.println("List of fields");
			for (CustomField field : customFields) {
				CUSTOM_FIELD_ID = DemoUtil.logCustomField(field);
			}
		}
		System.out.println();

	}

	// Test getting transaction channels API
	// Since Mambu 4.1 this API returns also applicable custom fields for each channel. See MBU-12226
	public static void testGetTransactionChannels() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		List<TransactionChannel> transactionChannels = organizationService.getTransactionChannels();
		System.out.println("Total Channels=" + transactionChannels.size());
		for (TransactionChannel channel : transactionChannels) {
			String channelName = channel.getName();
			String channelId = channel.getId();
			System.out.println(
					"\nChannel Key=" + channel.getEncodedKey() + "\tName=" + channelName + "\tId=" + channelId);

			// Transaction channels also have UsageRights since Mambu 3.13. See MBU-9562
			String demoUserRoleKey = (demoUser.getRole() == null) ? null : demoUser.getRole().getEncodedKey();
			UsageRights rights = channel.getUsageRights();
			if (rights != null) {
				List<String> roleKeys = rights.getRoles(); // Since 3.14 Mambu returns role keys only. See MBU-9725
				int totalRoles = (roleKeys == null) ? 0 : roleKeys.size();
				System.out.println("Is Accessible By All Users=" + rights.isAccessibleByAllUsers() + "\tTotal Roles="
						+ totalRoles + "\tDemo User Role Key=" + demoUserRoleKey);
				if (roleKeys != null) {
					for (String roleKey : roleKeys) {
						System.out.println("For Role Key=" + roleKey);
					}
				}
			} else {
				System.out.println("WARNING: No UsageRights available");
			}
			// Get TransactionChannel Custom Fields. Available since Mambu 4.1. See MBU-12226
			List<CustomField> channelFields = channel.getCustomFields();
			int totalCustomFields = channelFields != null ? channelFields.size() : 0;
			System.out.println("Total Custom Fields=" + totalCustomFields);

			for (CustomField field : channelFields) {
				DemoUtil.logCustomField(field);
			}
			// Log GLAccountingRule for Get Transaction channel
			GLAccountingRule accountingRue = channel.getTransactionChannelAccountingRule();
			if (accountingRue != null) {
				GLAccount glAccount = accountingRue.getAccount();
				String accountName = glAccount.getLongName();
				System.out.println(
						"GLAccount=" + accountName + "\tFinancialResource=" + accountingRue.getFinancialResource());
			} else {
				System.out.println("No GLAccountingRule");
			}
		}
	}

	// Update Custom Field values for the demo Branch and for demo Centre and delete the first custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		// Test fields for a Branch
		DemoTestCustomFieldValueService.testUpdateDeleteEntityCustomFields(MambuEntityType.BRANCH);

		// Test fields for a Centre
		DemoTestCustomFieldValueService.testUpdateDeleteEntityCustomFields(MambuEntityType.CENTRE);
	}

	// Test Posting Index Interest Rates. Available since 3.10
	public static void testPostIndexInterestRate() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);
		// Note that there is no API yet to get Index Rate Sources. API developers need to know the rate source key to
		// post new rates. These keys can be obtained from Mambu. They can also be looked up from the getProduct API
		// response. See MBU-8059 for more details

		// Encoded key for the Index Interest Rate Source
		String indexRateSourceKey = "8a6c06384b47afd4014b480624e6003a";
		int dateOffset = (int) (Math.random() * 30) * 24 * 60 * 60 * 1000; // rate start dates cannot be duplicated. Use
																			// random offset for each test run
		Date startDate = new Date(new Date().getTime() + dateOffset);
		// Create new IndexRate
		IndexRate indexRate = new IndexRate(startDate, new BigDecimal(3.5));

		try {
			OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
			IndexRate indexRateResult = organizationService.postIndexInterestRate(indexRateSourceKey, indexRate);

			System.out.println("Interest Rate updated. New Rate=" + indexRateResult.getRate() + " for source="
					+ indexRateResult.getRateSource().getName() + " Start date=" + indexRateResult.getStartDate());
		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}

	}

	// Test getting Identification Document Templates
	public static void testGetIDDocumentTemplates() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		List<IdentificationDocumentTemplate> templates = organizationService.getIdentificationDocumentTemplates();

		System.out.println("Total Templates Returned=" + templates.size());
		// Print templates details
		for (IdentificationDocumentTemplate template : templates) {
			System.out.println("Template=" + template.getDocumentIdTemplate() + "\tType=" + template.getDocumentType()
					+ "\tAuthority=" + template.getIssuingAuthority() + "\tIs Mandatory="
					+ template.getMandatoryForClients() + "\tAllow Attachments=" + template.getAllowAttachments()
					+ "\tKey=" + template.getEncodedKey());

		}
	}

	// Get Organization details. Available since 3.11
	public static void testGetOrganizationDetails() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// Test Get organization
		JSONOrganization jsonOrganization = organizationService.getOrganization();

		Organization organization = jsonOrganization.getOrganization();
		Address address = jsonOrganization.getAddress();
		System.out.println("Address=" + address.getLine1() + " " + address.getCity());

		System.out.println("Organization=" + organization.getName() + "\tTimeZoneId=" + organization.getTimeZoneID()
				+ "\tPhone=" + organization.getPhoneNo() + "\tEmail=" + organization.getEmailAddress());

		// Test Get General Settings API
		GeneralSettings generalSettings = organizationService.getGeneralSettings();
		System.out.println("\nSettings. BirthDateRequired=" + "\tDATE_FORMAT="
				+ generalSettings.getDateFormats().get(DateFormatType.DATE_FORMAT) + "\tDATETIME_FORMAT="
				+ generalSettings.getDateFormats().get(DateFormatType.DATE_TIME_FORMAT) + "\tDecimalSeperator="
				+ generalSettings.getDecimalSeperator() + "\tOtherIdDocumentsEnabled="
				+ generalSettings.getOtherIdDocumentsEnabled());

		try {
			// Test Get Mambu Object Labels
			List<ObjectLabel> objectLabels = organizationService.getObjectLabels();
			System.out.println("\nTotal Object Labels Returned=" + objectLabels.size());
			// Print ObjectLabel details
			for (ObjectLabel label : objectLabels) {
				System.out.println("Object Label. Type=" + label.getType() + "\tSingular=" + label.getSingularValue()
						+ "\tPlural=" + label.getPluralValue() + "\tLanguage=" + label.getLanguage()
						// Note, Encoded key is not returned in response.
						+ "\tHas Custom Value=" + label.hasCustomValue());

			}
		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}
		// Test Get Organization Logo
		try {
			String logo = organizationService.getBrandingLogo();
			System.out.println("\nEncoded Logo file=" + logo);
		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}

		// Test Get Organization Icon
		try {
			String icon = organizationService.getBrandingIcon();
			System.out.println("\nEncoded Icon file=" + icon);
		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}

	}

	/**
	 * Test getting exchange rates for organization currencies
	 * 
	 * Available since Mambu 4.2. See MBU-12628
	 * 
	 * @param organizationCurrencies
	 *            available organization currencies. Must not be null
	 * @throws MambuApiException
	 */
	public static void testGetExchangeRates(List<Currency> organizationCurrencies) throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		if (organizationCurrencies == null || organizationCurrencies.size() < 2) {
			System.out.println("WARNING: cannot test GET exchange rates with no non-base currencies");
			return;
		}
		// Set test dates. Test with the endDate to be today+1 day and the startDate to be 30 days earlier
		final long oneDay = 24 * 60 * 60 * 1000L; // 1 days in msecs
		final long thirtyDays = 30 * oneDay;
		// Create startDate and endDate for our test
		Date now = new Date();
		final int futureDaysLookup = 30;
		Date futureDate = new Date(now.getTime() + futureDaysLookup * oneDay);
		// Create test start date about 30 days before now and the endDate to be tomorrow (to see all as of today
		// exchange rates)
		String startDate = DateUtils.format(new Date(now.getTime() - thirtyDays));
		String endDate = DateUtils.format(futureDate);

		// Set test pagination params
		Integer offset = 0;
		Integer limit = 100;

		// Test the API
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		for (Currency currency : organizationCurrencies) {
			if (currency.isBaseCurrency()) {
				// Skipping. We need non-base currency to get exchange rates
				continue;
			}
			String currencyCode = currency.getCode();
			System.out.println("\nGETting Exchange Rates for currency=" + currencyCode);
			List<ExchangeRate> exchangeRates = organizationService.getExchangeRates(currencyCode, startDate, endDate,
					offset, limit);
			System.out.println("Total Exchange Rates=" + exchangeRates.size() + " to " + currencyCode);
		}
	}
}
