/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.admin.shared.model.ExchangeRate;
import com.mambu.api.server.handler.indexratesources.model.JsonIndexRate;
import com.mambu.api.server.handler.settings.organization.model.JSONOrganization;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.clients.shared.model.IdentificationDocumentTemplate;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.GeneralSettings;
import com.mambu.core.shared.model.IndexRate;
import com.mambu.core.shared.model.IndexRateSource;
import com.mambu.core.shared.model.ObjectLabel;
import com.mambu.core.shared.model.Organization;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

/**
 * Service class which handles API operations available for the organizations like getting it's currency
 * 
 * @author ipenciuc
 * 
 */
public class OrganizationService {

	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;

	private ServiceExecutor serviceExecutor;

	// Create API definitions for services provided by ClientService

	private final static ApiDefinition getBranchDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, Branch.class);
	private final static ApiDefinition getBranches = new ApiDefinition(ApiType.GET_LIST, Branch.class);

	private final static ApiDefinition getCentreDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, Centre.class);
	private final static ApiDefinition getCentres = new ApiDefinition(ApiType.GET_LIST, Centre.class);

	private final static ApiDefinition getCustomField = new ApiDefinition(ApiType.GET_ENTITY, CustomField.class);
	private final static ApiDefinition getCustomFieldSets = new ApiDefinition(ApiType.GET_LIST, CustomFieldSet.class);

	private final static ApiDefinition getCurrencies = new ApiDefinition(ApiType.GET_LIST, Currency.class);

	private final static ApiDefinition getTransactionChannels = new ApiDefinition(ApiType.GET_LIST,
			TransactionChannel.class);
	// Post Index Interest Rate
	private final static ApiDefinition postIndexInterestRate = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			IndexRateSource.class, IndexRate.class);
	
	// Post exchange rates. Example: /api/currencies/{currencyCode}/rates (e.g. POST {"buyRate":"3.41231232",
	// "sellRate":"3.4256546","startDate":"2016-02-12T00:00:00+0000"} /api/currencies/EUR/rates) 
	// For more details see MBU-12629.
	private final static ApiDefinition postExchangeRates = new ApiDefinition(ApiType.POST_OWNED_ENTITY, 
			Currency.class, ExchangeRate.class);

	// Get exchange rates. Example: /api/currencies/{currencyCode}/rates. See MBU-12628
	private final static ApiDefinition getExchangeRates = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Currency.class,
			ExchangeRate.class);

	/***
	 * Create a new organization service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public OrganizationService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Requests organization currencies. Either getting all organization currencies or getting just the base currency
	 * can be requested
	 * 
	 * @param getAllCurrencies
	 *            a boolean indicating if all currencies should be returned (true) or if only the base currency needs to
	 *            be returned (false)
	 * @return the list of all currencies or only the base currency depending on a flag
	 * 
	 * @throws MambuApiException
	 */
	public List<Currency> getCurrencies(boolean getAllCurrencies) throws MambuApiException {
		// Getting all currencies is available since 4.2. See MBU-4128 and MBU-13420
		// Example: GET api/currencies?includeForeign=true or GET api/currencies?includeForeign=false

		// Create Params Map specifying the "includeForeign" flag
		String includeForeignParam = getAllCurrencies ? APIData.TRUE : APIData.FALSE;

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(APIData.INCLUDE_FOREIGN, includeForeignParam);
		// Execute API request
		return serviceExecutor.execute(getCurrencies, paramsMap);
	}

	/**
	 * Convenience method to request the organization's base currency only
	 * 
	 * @return the Mambu base currency
	 * 
	 * @throws MambuApiException
	 */
	public final static String baseCurrencyMustBeDefined = "Base Currency must be defined";

	public Currency getCurrency() throws MambuApiException {

		// Delegate the call to GET all currencies method and request only the base currency
		List<Currency> currencies = getCurrencies(false);
		if (currencies != null && currencies.size() > 0) {
			return currencies.get(0);
		} else {
			// At least base currency must be defined for an organization
			throw new MambuApiException(-1, baseCurrencyMustBeDefined);
		}
	}

	/**
	 * Convenience method to GET all organization currencies
	 * 
	 * @return the list of all currencies defined for the organization
	 * 
	 * @throws MambuApiException
	 */
	public List<Currency> getAllCurrencies() throws MambuApiException {
		// Getting all currencies is available since 4.2. See MBU-4128 and MBU-13420
		// Example: GET api/currencies?includeForeign=true

		// Delegate the call to GET all currencies method and request all currencies
		return getCurrencies(true);
	}

	/**
	 * Get a paginated list of branches
	 * 
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return List<Branch>
	 * 
	 * @throws MambuApiException
	 */
	public List<Branch> getBranches(String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();

		params.put(OFFSET, offset);
		params.put(LIMIT, limit);
		return serviceExecutor.execute(getBranches, params);
	}

	/**
	 * Requests a branch by their Mambu ID
	 * 
	 * @param branchId
	 * 
	 * @return the Mambu branch model
	 * 
	 * @throws MambuApiException
	 */
	public Branch getBranch(String branchId) throws MambuApiException {
		return serviceExecutor.execute(getBranchDetails, branchId);
	}

	/**
	 * Requests a centre details by their Mambu ID
	 * 
	 * @param centreId
	 * 
	 * @return the Mambu centre model (with full details)
	 * 
	 * @throws MambuApiException
	 */
	public Centre getCentre(String centreId) throws MambuApiException {
		return serviceExecutor.execute(getCentreDetails, centreId);
	}

	/**
	 * Get paginated list of centres
	 * 
	 * @param branchId
	 *            Centers for the specified branch are returned. If NULL, all centres are searched
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return an array of Centres
	 * 
	 * @throws MambuApiException
	 */
	public List<Centre> getCentres(String branchId, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(APIData.BRANCH_ID, branchId); // if branchId is null then all centres are searched
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		return serviceExecutor.execute(getCentres, params);
	}

	/**
	 * Get CustomField object details by Custom Field ID
	 * 
	 * @param fieldId
	 *            The id of the required CustomField
	 * 
	 * @return CustomField
	 * 
	 * @throws MambuApiException
	 */
	public CustomField getCustomField(String fieldId) throws MambuApiException {
		return serviceExecutor.execute(getCustomField, fieldId);
	}

	/**
	 * Get Custom Field Sets
	 * 
	 * @param customFieldType
	 *            The type of the required CustomField Set. Example CLIENT_INFO, GROUP_INFO, LOAN_ACCOUNT_INFO,
	 *            SAVINGS_ACCOUNT_INFO, BRANCH_INFO, USER_INFO Can be null - all types requested.
	 * 
	 * @return List of CustomFieldSet sets
	 * 
	 * @throws MambuApiException
	 */
	public List<CustomFieldSet> getCustomFieldSets(CustomFieldType customFieldType) throws MambuApiException {

		ParamsMap params = null;
		// if customFieldType is null then all types are requested
		if (customFieldType != null) {
			// Add Custom Filed Type Param
			params = new ParamsMap();
			params.addParam(APIData.CUSTOM_FIELD_SETS_TYPE, customFieldType.name());
		}

		return serviceExecutor.execute(getCustomFieldSets, params);
	}

	/**
	 * Get Transaction Channels
	 * 
	 * Note: since Mambu 4.1 the returned transaction channels also contain a list of custom fields applicable to this
	 * channel and channel's accounting rule. See MBU-12226- As a Developer, I want to GET transaction channels with
	 * custom fields via APIs
	 * 
	 * @return List of all Transaction Channels for the organization
	 * 
	 * @throws MambuApiException
	 */
	public List<TransactionChannel> getTransactionChannels() throws MambuApiException {
		// Example: GET /api/transactionchannels
		// See MBU-6407 and MBU-12226
		// Since Mambu 4.1 the returned transaction channels also include custom field definitions applicable to each
		// channel and the accounting rule. See MBU-12226

		ParamsMap params = null;
		return serviceExecutor.execute(getTransactionChannels, params);
	}

	/**
	 * Post Index Interest Rate
	 * 
	 * @param indexRateSourceKey
	 *            the encoded key of the Interest Rate Source
	 * @param indexRate
	 *            index rate object
	 * @return index rate
	 * @throws MambuApiException
	 */
	public IndexRate postIndexInterestRate(String indexRateSourceKey, IndexRate indexRate) throws MambuApiException {

		// Example: POST JsonIndexRate /api/indexratesources/40288a164bda92a4014bda9358ee0001/indexrates
		// Available since 3.10. See MBU-8059

		// indexRateSourceKey is validated by the serviceExecutor
		if (indexRate == null) {
			throw new IllegalArgumentException("Index Rate must not  be null");
		}

		JsonIndexRate jsonIndexRate = new JsonIndexRate(indexRate);
		// This API expects JSON content. The dates are expected in "yyyy-MM-dd" format
		postIndexInterestRate.setContentType(ContentType.JSON);
		postIndexInterestRate.setJsonDateTimeFormat(APIData.yyyyMmddFormat);
		return serviceExecutor.executeJson(postIndexInterestRate, jsonIndexRate, indexRateSourceKey);
	}

	/**
	 * Get all Identification Document Templates
	 * 
	 * @return a list of all Identification Document Templates defined for an organization. This API doesn't support
	 *         pagination.
	 * 
	 * @throws MambuApiException
	 */
	public List<IdentificationDocumentTemplate> getIdentificationDocumentTemplates() throws MambuApiException {
		// Example: GET /api/settings/iddocumenttemplates
		// Available since 3.10.5. See MBU-8780

		String urlPath = APIData.SETTINGS + "/" + APIData.ID_DOCUMENT_TEMPLATES;
		ApiDefinition getDocumentTemplates = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET,
				IdentificationDocumentTemplate.class, ApiReturnFormat.COLLECTION);
		return serviceExecutor.execute(getDocumentTemplates);
	}

	/**
	 * Get Organization details
	 * 
	 * @return Mambu organization definition details
	 * 
	 * @throws MambuApiException
	 */
	public JSONOrganization getOrganization() throws MambuApiException {
		// GET /api/settings/organization
		// Response example:{”name”:”Org name”,”timeZoneID":"PST",... "address":{"line1":"1st Rd.","city":"City"}
		// Available since 3.11. See MBU-8776

		// TODO: There is no Mambu model class for this JSON response. Cannot use JSONOrganization because Mambu
		// response does not contain "organization:" prefix in front of organization fields
		// Temporary Solution: GET as a String and create JSONOrganization Mambu model object in the wrapper

		String urlPath = APIData.SETTINGS + "/" + APIData.ORGANIZATION;
		ApiDefinition getOrganization = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, String.class,
				ApiReturnFormat.RESPONSE_STRING);

		String jsonResponse = serviceExecutor.execute(getOrganization);
		if (jsonResponse == null) {
			return null;
		}
		// Parse as Organization and then parse the same response as JSONOrganization to get just the Address.
		Organization organization = GsonUtils.createGson().fromJson(jsonResponse, Organization.class);
		JSONOrganization jsonOrganization = GsonUtils.createGson().fromJson(jsonResponse, JSONOrganization.class);
		Address address = (jsonOrganization == null) ? null : jsonOrganization.getAddress();
		// Return as JSONOrganization
		return new JSONOrganization(organization, address);
	}

	/**
	 * Get organization's general settings
	 * 
	 * @return Mambu organization general settings
	 * 
	 * @throws MambuApiException
	 */
	public GeneralSettings getGeneralSettings() throws MambuApiException {
		// GET /api/settings/general
		// Available since 3.11. See MBU-8779

		String urlPath = APIData.SETTINGS + "/" + APIData.GENERAL;
		ApiDefinition getGeneralSettings = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET,
				GeneralSettings.class, ApiReturnFormat.OBJECT);
		return serviceExecutor.execute(getGeneralSettings);
	}

	/**
	 * Get Object labels
	 * 
	 * @return a list of all object labels defined for all languages supported by Mambu
	 * 
	 * @throws MambuApiException
	 */
	public List<ObjectLabel> getObjectLabels() throws MambuApiException {
		// GET /api/settings/labels
		// Available since 3.11. See MBU-8778

		String urlPath = APIData.SETTINGS + "/" + APIData.LABELS;
		ApiDefinition getObjectLabels = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, ObjectLabel.class,
				ApiReturnFormat.COLLECTION);
		return serviceExecutor.execute(getObjectLabels);
	}

	/**
	 * Get Organization Logo
	 * 
	 * @return string with base64 encoded logo image, Example: data:image/PNG;base64,iVBORw0...
	 * 
	 * @throws MambuApiException
	 */
	public String getBrandingLogo() throws MambuApiException {
		// GET /api/settings/branding/logo
		// Available since 3.11. See MBU-8777

		String urlPath = APIData.SETTINGS + "/" + APIData.BRANDING + "/" + APIData.LOGO;
		ApiDefinition getLogo = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, String.class,
				ApiReturnFormat.OBJECT);
		return serviceExecutor.execute(getLogo);
	}

	/**
	 * Get Organization Icon
	 * 
	 * @return string with base64 encoded logo image, Example: data:image/PNG;base64,iVBORw0...
	 * 
	 * @throws MambuApiException
	 */
	public String getBrandingIcon() throws MambuApiException {
		// GET /api/settings/branding/icon
		// Available since 3.11. See MBU-8777

		String urlPath = APIData.SETTINGS + "/" + APIData.BRANDING + "/" + APIData.ICON;
		ApiDefinition getIcon = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, String.class,
				ApiReturnFormat.OBJECT);
		return serviceExecutor.execute(getIcon);
	}
	
	
	/**
	 * Convenience method to create ExchangeRate.
	 * 
	 * @param currency
	 *            the Currency object. Must not be null or have a null currency code.
	 * @param exchangeRate
	 *            the ExchangeRate to be created in Mambu. Must not be null.
	 * @return newly created ExchangeRate 
	 * @throws MambuApiException
	 */
	public ExchangeRate createExchangeRate(Currency currency, ExchangeRate exchangeRate) throws MambuApiException{
		// POST /api/curencies/{curencyCode}/rates 
		// Available since 4.2. See MBU-12629 
	
		if(currency == null || currency.getCode() == null){
			throw new IllegalArgumentException("Currency and currency code must not be null");
		}
		
		// Delegates execution
		return createExchangeRate(currency.getCode(), exchangeRate);
	}
		
	/**
	 * Creates a new exchange rate using a currency code and ExchangeRate object and sends it as a JSON api. 
	 *  
	 * @param currencyCode
	 *            the currency code. Must not be null. 
	 * @param exchangeRate
	 *            the exchange rate to be created. Must not be null.
	 * @return newly created ExchangeRate
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public ExchangeRate createExchangeRate(String currencyCode, ExchangeRate exchangeRate) throws MambuApiException{
		// POST /api/curencies/{curencyCode}/rates 
		// e.g. POST {"buyRate":"3.41231232","sellRate":"3.4256546","startDate":"2016-02-12T00:00:00+0000"} /api/currencies/EUR/rates
		// Available since 4.2. See MBU-12629 

		if (currencyCode == null || exchangeRate == null) {
			throw new IllegalArgumentException("Currency code and Exchange rate must not  be null");
		}
		
		// set the content type to be JSON
		postExchangeRates.setContentType(ContentType.JSON);
		
		// executes POST currency API
		return serviceExecutor.executeJson(postExchangeRates, exchangeRate, currencyCode);
	}

	/**
	 * Get paginated list of available exchange rates for a base currency
	 * 
	 * @param toCurrencyCode
	 *            currency code to exchange to from the base currency. Must not be null. It should be a valid non-base
	 *            currency code for a currency used by the organization
	 * @param startDate
	 *            start date in a "yyyy-MM=dd" format
	 * @param endDate
	 *            end date in a "yyyy-MM=dd" format. Note, startDate should be <= endDate, If both the startDate and the
	 *            endDate are not specified the all available exchange rates are returned
	 * @param offset
	 *            the pagination offset of the response. If not set a value of 0 is used by default by Mambu
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default by Mambu
	 * 
	 * @return a list of Exchange Rates
	 * 
	 * @throws MambuApiException
	 */
	public List<ExchangeRate> getExchangeRates(String toCurrencyCode, String startDate, String endDate, Integer offset,
			Integer limit) throws MambuApiException {
		// Available since Mambu 4.2. See MBU-12628
		// Example: GET /api/currencies/{toCurrencyCode}/rates?from=2016-05-10&to=2016-05-14&offset=0&limit=40

		if (startDate == null && endDate != null || startDate != null && endDate == null) {
			throw new IllegalArgumentException("Start and End date must be either both present or both absent");
		}

		ParamsMap params = new ParamsMap();
		// Add start and end dates. Expected
		params.put(APIData.FROM, startDate);
		params.put(APIData.TO, endDate);

		// Add pagination params
		if (offset != null) {
			params.addParam(APIData.OFFSET, String.valueOf(offset));
		}
		if (limit != null) {
			params.addParam(APIData.LIMIT, String.valueOf(limit));
		}

		// Execute API. The "toCurrencyCode" is the ID for the currency
		return serviceExecutor.execute(getExchangeRates, toCurrencyCode, params);
	}

}
