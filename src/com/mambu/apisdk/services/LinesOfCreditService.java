package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.Account.Type;
import com.mambu.api.server.handler.linesofcredit.model.JSONLineOfCredit;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.linesofcredit.shared.model.AccountsFromLineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCreditExpanded;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * Service class which handles API operations for Lines of Credit (LoC)
 * 
 * The following APIs are supported:
 * 
 * Get all lines of credit
 * 
 * Get a specific line of credit by its ID/key
 * 
 * Get all lines of credits for a client or a group
 * 
 * Get all loan and savings accounts for a specific line of credit
 * 
 * Add accounts to lines of credit
 * 
 * Remove accounts from lines of credit
 * 
 * * More details in MBU-8607, MBU-8413, MBU-8414, MBU-8415, MBU-8417, MBU-9864, MBU-9873
 * 
 * 
 * @author mdanilkis
 * 
 */
public class LinesOfCreditService {

	private ServiceExecutor serviceExecutor;
	// MambuEntity managed by this service
	private static final MambuEntityType serviceEntity = MambuEntityType.LINE_OF_CREDIT;
	// Create line of credit API definition. Example: POST {"lineOfCredit" :{LoC fields}"} /api/linesofcredit
	private final static ApiDefinition createLineOfCredit = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONLineOfCredit.class, LineOfCredit.class);

	// Patch Line of credit:
	// PATCH{"lineOfCredit":{"id":"LOC1468393266785","startDate":"2016-07-13T10:01:06+0300","expireDate":"2017-01-13T10:01:06+0200",
	// "creationDate":"2016-07-13T10:01:11+0300","amount":105000,"notes":"some notes"}}'
	// /api/linesofcredit/LOC_ID
	private final static ApiDefinition patchLineOfCredit = new ApiDefinition(ApiType.PATCH_ENTITY, LineOfCredit.class);

	/***
	 * Create a new Lines Of Credit service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public LinesOfCreditService(MambuAPIService mambuAPIService) {

		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Get all lines of credit defined for all clients and groups
	 * 
	 * Call sample:
	 *  GET api/linesofcredit
	 *  Available since 3.11. See MBU-8414
	 * 
	 * @param offset
	 *            pagination offset. If null, Mambu default (zero) will be used
	 * @param limit
	 *            pagination limit. If null, Mambu default will be used
	 * @return a list of all Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getAllLinesOfCredit(Integer offset, Integer limit) throws MambuApiException {

		return serviceExecutor.getPaginatedList(serviceEntity, offset, limit);
	}

	/**
	 * Get all lines of credit defined for all clients and groups with all the details (including custom fields)
	 * 
	 * Call sample:
	 * 	GET api/linesofcredit?fullDetails=true
	 *	Available since 4.5. See JSDK-88
	 * 
	 * @param offset
	 *  pagination offset. If null, Mambu default (zero) will be used
	 * @param limit
	 *  pagination limit. If null, Mambu default will be used
	 * @return a list of all Lines of Credit having all the details
	 * 
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getAllLinesOfCreditWithDetails(Integer offset, Integer limit) throws MambuApiException {

		return serviceExecutor.getPaginatedList(serviceEntity, offset, limit, true);
	}

	
	/***
	 * Get line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * 
	 * @return Line of Credit
	 * @throws MambuApiException
	 */
	public LineOfCredit getLineOfCredit(String lineOfCreditId) throws MambuApiException {

		// GET api/linesofcredit/{id}
		// Response example: {"lineOfCredit":{"encodedKey":"abc123","id":"FVT160", "amount":"5000",.. }}
		// Available since 3.11. See MBU-8417

		// This API returns LineOfCreditExpanded object
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_ENTITY, LineOfCreditExpanded.class);
		LineOfCreditExpanded lineOfCreditExpanded = serviceExecutor.execute(apiDefinition, lineOfCreditId);

		// Return as the LineOfCredit
		return lineOfCreditExpanded.getLineOfCredit();
	}
	
	
	/***
	 * Gets the details of a line of credit including custom fields information
	 *  GET api/linesofcredit/{id}?fullDetails=true
	 *  Response example: {"lineOfCredit":{"encodedKey":"abc123","id":"FVT160", "amount":"5000",.. }}
	 *	Available since 4.5. 
	 *
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * 
	 * @return Line of Credit with all the details including custom field values
	 * @throws MambuApiException
	 */
	public LineOfCredit getLineOfCreditDetails(String lineOfCreditId) throws MambuApiException {

		// This API returns LineOfCreditExpanded object
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LineOfCreditExpanded.class);
		LineOfCreditExpanded lineOfCreditExpanded = serviceExecutor.execute(apiDefinition, lineOfCreditId);

		// Return as the LineOfCredit
		return lineOfCreditExpanded.getLineOfCredit();
	}

	/***
	 * Get lines of credit for a Client or a Group
	 * 
	 * @param customerType
	 *            customer type (Client or Group). Must be either MambuEntityType.CLIENT or MambuEntityType.GROUP.
	 *            Mandatory. Must not be null.
	 * @param customerId
	 *            the encoded key or id of the customer. Mandatory. Must not be null.
	 * @param offset
	 *            pagination offset. If null, Mambu default (zero) will be used
	 * @param limit
	 *            pagination limit. If null, Mambu default will be used
	 * @return the List of Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getLinesOfCredit(MambuEntityType customerType, String customerId, Integer offset,
			Integer limit, boolean requiresFullDetails) throws MambuApiException {

		// Example: GET /api/clients/{clientId}/linesofcredit or GET /api/groups/{groupId}/linesofcredit
		// Available since 3.11. See MBU-8413

		if (customerType == null) {
			throw new IllegalArgumentException("Customer type cannot be null");
		}
		switch (customerType) {
		case CLIENT:
		case GROUP:
			return serviceExecutor.getOwnedEntities(customerType, customerId, serviceEntity, offset, limit, requiresFullDetails);
		default:
			throw new IllegalArgumentException("Lines Of Credit Supported only for Clients and Groups");
		}

	}

	/***
	 * Convenience method to Get lines of credit for a Client
	 * 
	 * @param clientId
	 *            client ID. Mandatory. Must not be null.
	 * @param offset
	 *            pagination offset.
	 * @param limit
	 *            pagination limit.
	 * @return the List of Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getClientLinesOfCredit(String clientId, Integer offset, Integer limit)
			throws MambuApiException {

		// Example: GET /api/clients/{clientId}/linesofcredit
		// Available since 3.11. See MBU-8413

		return getLinesOfCredit(MambuEntityType.CLIENT, clientId, offset, limit, false);
	}
	
	/**
	 * Convenience method to Get lines of credit with all details (including custom fields) for a Client
	 *
	 * Example: GET /api/clients/{clientId}/linesofcredit?fullDetails=true Available since 4.5. See JSDK-88
	 * 
	 * @param clientId
	 *            the ID of the client
	 * @param offset
	 *            pagination offset.
	 * @param limit
	 *            pagination limit.
	 * @return the List of Lines of Credit with full details
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getClientLinesOfCreditDetails(String clientId, Integer offset, Integer limit)
			throws MambuApiException {

		return getLinesOfCredit(MambuEntityType.CLIENT, clientId, offset, limit, true);
	}
	
	/***
	 * Convenience method to Get lines of credit for a Group
	 * 
	 * Example: GET /api/groups/{groupId}/linesofcredit
	 * Available since 3.11. See MBU-8413
	 * 
	 * @param groupId
	 *            group ID. Mandatory. Must not be null.
	 * @param offset
	 *            pagination offset.
	 * @param limit
	 *            pagination limit.
	 * @return the List of Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getGroupLinesOfCredit(String groupId, Integer offset, Integer limit)
			throws MambuApiException {

		return getLinesOfCredit(MambuEntityType.GROUP, groupId, offset, limit, false);
	}
	
	/**
	 * Convenience method to Get lines of credit for a Group with all the details (including custom fields)
	 * 
	 * Example: GET /api/groups/{groupId}/linesofcredit?fullDetails=true Available since 4.5. 
	 * See JSDK-88
	 * 
	 * @param groupId
	 *            group ID. Mandatory. Must not be null.
	 * @param offset
	 *            pagination offset.
	 * @param limit
	 *            pagination limit.
	 * @return he List of Lines of Credit with full details
	 * 
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getGroupLinesOfCreditDetails(String groupId, Integer offset, Integer limit)
			throws MambuApiException {

		return getLinesOfCredit(MambuEntityType.GROUP, groupId, offset, limit, true);
	}


	/***
	 * Get all accounts for a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * 
	 * @return accounts for the line of credit
	 * @throws MambuApiException
	 */
	public AccountsFromLineOfCredit getAccountsForLineOfCredit(String lineOfCreditId) throws MambuApiException {

		// Example: GET /api/linesofcredit/{ID}/accounts
		// Available since 3.11. See MBU-8415

		ApiDefinition getAccountForLoC = new ApiDefinition(ApiType.GET_OWNED_ENTITY, LineOfCredit.class,
				AccountsFromLineOfCredit.class);
		return serviceExecutor.execute(getAccountForLoC, lineOfCreditId);
	}

	/**
	 * Add Loan Account to a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * @param loanAccountId
	 *            the id or the encoded key of a Loan Account. Mandatory. Must not be null
	 * @return added loan account
	 */
	public LoanAccount addLoanAccount(String lineOfCreditId, String loanAccountId) throws MambuApiException {

		// Example: POST /api/linesofcredit/{LOC_ID}/loans/{ACCOUNT_ID}
		// Available since 3.12.2. See MBU-9864

		if (loanAccountId == null) {
			throw new IllegalArgumentException("Account ID must not be null");
		}
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.POST_OWNED_ENTITY, LineOfCredit.class,
				LoanAccount.class);

		return serviceExecutor.execute(apiDefinition, lineOfCreditId, loanAccountId, null);
	}

	/**
	 * Add Savings Account to a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * @param savingsAccountId
	 *            the id or the encoded key of a Savings Account. Mandatory. Must not be null
	 * @return added savings account
	 */
	public SavingsAccount addSavingsAccount(String lineOfCreditId, String savingsAccountId) throws MambuApiException {

		// Example: POST /api/linesofcredit/{LOC_ID}/savings/{ACCOUNT_ID}
		// Available since 3.12.2. See MBU-9864

		if (savingsAccountId == null) {
			throw new IllegalArgumentException("Account ID must not be null");
		}
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.POST_OWNED_ENTITY, LineOfCredit.class,
				SavingsAccount.class);

		return serviceExecutor.execute(apiDefinition, lineOfCreditId, savingsAccountId, null);
	}

	/**
	 * Delete Account from a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * @param accountType
	 *            account type. Must not be null
	 * @param accountId
	 *            the id or the encoded key of the Account. Mandatory. Must not be null
	 * @return true if success
	 */
	public boolean deleteAccount(String lineOfCreditId, Type accountType, String accountId) throws MambuApiException {

		if (accountType == null || accountId == null) {
			throw new IllegalArgumentException("Account Type and Account ID must not be null. Type=" + accountType
					+ " Id=" + accountId);
		}
		MambuEntityType ownedEentityType = (accountType == Type.LOAN) ? MambuEntityType.LOAN_ACCOUNT
				: MambuEntityType.SAVINGS_ACCOUNT;

		return serviceExecutor.deleteOwnedEntity(MambuEntityType.LINE_OF_CREDIT, lineOfCreditId, ownedEentityType,
				accountId);

	}

	/**
	 * Creates a line of credit (Credit arrangement) for a client or a group, depending on the key set on the
	 * LineOfCredit.
	 * 
	 * @param lineOfCredit
	 *            The line of credit to be created in Mambu. Must not be null.
	 * @return Newly created line of credit
	 * 
	 * @throws MambuApiException
	 */
	public LineOfCredit createLineOfCredit(LineOfCredit lineOfCredit) throws MambuApiException {

		// Example: POST /api/linesofcredit
		// Request example:{"lineOfCredit":{"id": "XXX007","clientKey": "8a80862b5590e1cb015591b12d100e8c",
		// "startDate": "2016-07-20T00:00:00+0000","expireDate": "2016-10-30T00:00:00+0000","amount": "100000",
		// "notes": "some line of credit notes"}}
		// Available since 4.2. See MBU-13767
		if (lineOfCredit == null) {
			throw new IllegalArgumentException("Line of credit must not be null.");
		}

		JSONLineOfCredit lineOfCreditJson = new JSONLineOfCredit(lineOfCredit);
		return serviceExecutor.executeJson(createLineOfCredit, lineOfCreditJson);
	}

	/**
	 * Convenience method to Delete Loan Account from a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * @param loanAccountId
	 *            the id or the encoded key of a Loan Account. Mandatory. Must not be null
	 * @return true if success
	 */
	public boolean deleteLoanAccount(String lineOfCreditId, String loanAccountId) throws MambuApiException {

		// Example: DELETE /api/linesofcredit/{LOC_ID}/loans/{ACCOUNT_ID}
		// Available since 3.12.2. See MBU-9873
		return deleteAccount(lineOfCreditId, Type.LOAN, loanAccountId);
	}

	/**
	 * Convenience method to Delete Savings Account from a line of credit
	 * 
	 * @param lineOfCreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * @param savingsAccountId
	 *            the id or the encoded key of a Savings Account. Mandatory. Must not be null
	 * @return true if success
	 */
	public boolean deleteSavingsAccount(String lineOfCreditId, String savingsAccountId) throws MambuApiException {

		// Example: DELETE /api/linesofcredit/{LOC_ID}/savings/{ACCOUNT_ID}
		// Available since 3.12.2. See MBU-9873

		return deleteAccount(lineOfCreditId, Type.SAVINGS, savingsAccountId);

	}
	
	/**
	 * Patches a line of credit.
	 * 
	 * @param lineOfCredit
	 *            The line of credit object to be patched. LineOfCredit object must NOT be null. 
	 *            Note that either the encodedKey or the ID must NOT be null for PATCHing a line of credit
	 * @return true if the PATCH operation succeeded.
	 */
	public boolean patchLinesOfCredit(LineOfCredit lineOfCredit) throws MambuApiException {

		// PATCH /api/linesofcredit/{LOC_ID}
		// Example: PATCH /api/linesofcredit/8a80863a55e2d9f40155e30f2dee0106
		// Available since 4.3 See MBU-14628
		if(lineOfCredit == null){
			throw new IllegalArgumentException("Line of Credit must not be null");
		} 
		
		String locEncodedKey = lineOfCredit.getEncodedKey();
		String locId = lineOfCredit.getId();
		
		if (locEncodedKey == null && locId == null) {
			throw new IllegalArgumentException("Line Of Credit cannot be updated, the encodedKey or ID must be NOT null");
		}
		
		String id = locEncodedKey != null ? locEncodedKey : locId;
		JSONLineOfCredit lineOfCreditJson = new JSONLineOfCredit(lineOfCredit);
		return serviceExecutor.executeJson(patchLineOfCredit, lineOfCreditJson, id);
	}

}
