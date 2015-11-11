package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;

/**
 * Service class which handles API operations for retrieving document templates for Mambu Entities. Getting Templates
 * for the following entities is currently supported: LoanAccount and SavingsAccount
 * 
 * Document Templates API currently supports:
 * 
 * GET Populated Account Templates (see MBU-10631 - As a Developer, I want to get populated Account Document Templates
 * via APIs) and
 * 
 * GET Populated Transaction Templates (see MBU-10632 - As a Developer, I want to get populated Transaction Document
 * Templates via API )
 * 
 * @author mdanilkis
 * 
 */

public class DocumentTemplatesService {

	// Service helper
	protected ServiceExecutor serviceExecutor;

	// Specify Mambu entities supported by the Templates API
	// GET Templates are supported for LoanAccount and SavingsAccount
	private final static MambuEntityType[] supportedEntities = new MambuEntityType[] { MambuEntityType.LOAN_ACCOUNT,
			MambuEntityType.SAVINGS_ACCOUNT };

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public DocumentTemplatesService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Get populated account document template for a loan account and a given template key
	 * 
	 * @param loanAccountId
	 *            id or encoded key for the loan account. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param startDate
	 *            Start date. Mandatory for product documents that "Include Transactions History". Can only be specified
	 *            for documents that "Include Transactions History". Must be less or equal to endDate
	 * @param endDate
	 *            End date. Mandatory for product documents that "Include Transactions History". Can only be specified
	 *            for documents that "Include Transactions History"
	 * @return populated HTML template for the loan account
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedLoanTemplate(String loanAccountId, String templateKey, String startDate, String endDate)
			throws MambuApiException {
		// Example: GET /api/loans/{LOAN_ID}/templates/{TEMPLATE_ID}?startDate=2015-06-28&endDate=2015-07-30
		// Available since Mambu 3.14. See MBU-10631 for more details

		return getPopulatedEntityTemplate(MambuEntityType.LOAN_ACCOUNT, loanAccountId, templateKey, startDate, endDate);

	}

	/***
	 * Get populated account document template for a savings account and a given template key
	 * 
	 * @param savingsAccountId
	 *            id or encoded key for the savings account. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param startDate
	 *            Start date. Mandatory for product documents that "Include Transactions History". Can only be specified
	 *            for documents that "Include Transactions History". Must be less or equal to endDate
	 * @param endDate
	 *            End date. Mandatory for product documents that "Include Transactions History". Can only be specified
	 *            for documents that "Include Transactions History"
	 * @return populated HTML template for the savings account
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedSavingsTemplate(String savingsAccountId, String templateKey, String startDate,
			String endDate) throws MambuApiException {
		// Example:GET /api/savings/{SAVING_ID}/templates/{TEMPLATE_ID}?startDate=2015-06-28&endDate=2015-07-30
		// Available since Mambu 3.14. See MBU-10631 for more details

		return getPopulatedEntityTemplate(MambuEntityType.SAVINGS_ACCOUNT, savingsAccountId, templateKey, startDate,
				endDate);

	}

	/***
	 * Get populated transaction document template for a loan account
	 * 
	 * @param loanAccountId
	 *            id or encoded key for the loan account. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param transactionId
	 *            a valid transaction ID or encoded key. Must not be null
	 * @return populated HTML template for the transaction
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedLoanTransactionTemplate(String loanAccountId, String templateKey, String transactionId)
			throws MambuApiException {
		// Example: /api/loans/{ACCOUNT_ID}/transactions/{TRANSACTION_ID}/templates/{TEMPLATE_ID}
		// Available since Mambu 3.14. See MBU-10632 for more details
		return getPopulatedEntityTransactionTemplate(MambuEntityType.LOAN_ACCOUNT, loanAccountId, templateKey,
				transactionId);
	}

	/***
	 * Get populated transaction document template for a savings account
	 * 
	 * @param savingsAccountId
	 *            entity id or encoded key for the savings account. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param transactionId
	 *            a valid transaction ID or encoded key. Must not be null
	 * @return populated HTML template for the transaction
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedSavingsTransactionTemplate(String savingsAccountId, String templateKey,
			String transactionId) throws MambuApiException {
		// Example: api/savings/{ACCOUNT_ID}/transactions/{TRANSACTION_ID}/templates/{TEMPLATE_ID}
		// Available since Mambu 3.14. See MBU-10632 for more details
		return getPopulatedEntityTransactionTemplate(MambuEntityType.SAVINGS_ACCOUNT, savingsAccountId, templateKey,
				transactionId);
	}

	/**
	 * Get supported entity types
	 * 
	 * @return all supported entities
	 */
	public static MambuEntityType[] getSupportedEntities() {
		return supportedEntities;
	}

	/**
	 * Is parent entity type supported by the Document Templates API
	 * 
	 * @param entityType
	 *            Mambu Entity type
	 * @return true if supported
	 */
	public static boolean isSupported(MambuEntityType entityType) {
		if (entityType == null) {
			return false;
		}

		Set<MambuEntityType> set = new HashSet<MambuEntityType>(Arrays.asList(supportedEntities));
		return (set.contains(entityType)) ? true : false;

	}

	/***
	 * Convenience method to get populated document template for a given parent entity and a given template id
	 * 
	 * @param parentEntity
	 *            MambuEntityType for which document templates are retrieved. Example, MambuEntityType.LOAN_ACCOUNT.
	 *            Must not be null. Currently can be only MambuEntityType.LOAN_ACCOUNT or
	 *            MambuEntityType.SAVINGS_ACCOUNT
	 * @param entityId
	 *            entity id or encoded key for the parent entity. Example, loan account id for a
	 *            MambuEntityType.LOAN_ACCOUNT. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param startDate
	 *            Start date. Mandatory for product documents that "Include Transactions History". Can only be be
	 *            specified for documents that "Include Transactions History". startDate <= endDate
	 * @param endDate
	 *            End date. Mandatory for product documents that "Include Transactions History". Can only be be
	 *            specified for documents that "Include Transactions History". startDate <= endDate
	 * @return populated HTML template for the parent entity
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedEntityTemplate(MambuEntityType parentEntity, String entityId, String templateKey,
			String startDate, String endDate) throws MambuApiException {
		// Example: GET /api/loans/{LOAN_ID}/templates/{TEMPLATE_ID}?startDate=2015-06-28&endDate=2015-07-30
		// Example:GET /api/savings/{SAVING_ID}/templates/{TEMPLATE_ID}?startDate=2015-06-28&endDate=2015-07-30
		// Available since Mambu 3.14. See MBU-10631 for more details

		if (!isSupported(parentEntity)) {
			throw new IllegalArgumentException("Get Populated Document templates is not supported for" + parentEntity);
		}
		if (entityId == null || templateKey == null) {
			throw new IllegalArgumentException("Entity ID and Template key must not be null");
		}
		ParamsMap params = new ParamsMap();
		params.put(APIData.START_DATE, startDate);
		params.put(APIData.END_DATE, endDate);

		// Make API urlString. Example: /api/loans/accountID/templates/templateKey
		String urlString = ApiDefinition.getApiEndPoint(parentEntity.getEntityClass()) + "/" + entityId + "/"
				+ APIData.TEMPLATES + "/" + templateKey;
		ApiDefinition apiDefinition = new ApiDefinition(urlString, ContentType.WWW_FORM, Method.GET, String.class,
				ApiReturnFormat.RESPONSE_STRING);

		return serviceExecutor.execute(apiDefinition, params);

	}

	/***
	 * Convenience method to get populated transaction document template for a given parent entity (LoanAccount or
	 * SavingsAccont)
	 * 
	 * @param parentEntity
	 *            MambuEntityType for which document templates are retrieved. Example, MambuEntityType.LOAN_ACCOUNT.
	 *            Must not be null. Currently can be only MambuEntityType.LOAN_ACCOUNT or
	 *            MambuEntityType.SAVINGS_ACCOUNT
	 * @param entityId
	 *            entity id or encoded key for the parent entity. Example, loan account id for a
	 *            MambuEntityType.LOAN_ACCOUNT. Must not be null
	 * @param templateKey
	 *            template encoded key. Should be a valid product document template encoded key. Must not be null
	 * @param transactionId
	 *            a valid transaction ID or encoded key. Must not be null
	 * @return populated HTML template for the transaction
	 * 
	 * @throws MambuApiException
	 */
	public String getPopulatedEntityTransactionTemplate(MambuEntityType parentEntity, String entityId,
			String templateKey, String transactionId) throws MambuApiException {
		// Example: /api/loans/{ACCOUNT_ID}/transactions/{TRANSACTION_ID}/templates/{TEMPLATE_ID}
		// Example: api/savings/{ACCOUNT_ID}/transactions/{TRANSACTION_ID}/templates/{TEMPLATE_ID}
		// Available since Mambu 3.14. See MBU-10632 for more details

		if (!isSupported(parentEntity)) {
			throw new IllegalArgumentException("Get Transaction Document templates is not supported for" + parentEntity);
		}

		if (entityId == null || templateKey == null || transactionId == null) {
			throw new IllegalArgumentException("Entity ID=" + entityId + " Template key=" + templateKey
					+ "and Transaction Id=" + transactionId + " must not be null");
		}

		// Make API urlString. Example: /api/loans/accountID/transactions/transactionId/templates/templateKey
		String urlString = ApiDefinition.getApiEndPoint(parentEntity.getEntityClass()) + "/" + entityId + "/"
				+ APIData.TRANSACTIONS + "/" + transactionId + "/" + APIData.TEMPLATES + "/" + templateKey;

		ApiDefinition apiDefinition = new ApiDefinition(urlString, ContentType.WWW_FORM, Method.GET, String.class,
				ApiReturnFormat.RESPONSE_STRING);
		return serviceExecutor.execute(apiDefinition);

	}
}
