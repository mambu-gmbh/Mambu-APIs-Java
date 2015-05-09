package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.linesofcredit.shared.model.AccountsFromLineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCredit;

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
 * * More details in MBU-8607, MBU-8413, MBU-8414, MBU-8415, MBU-8417
 * 
 * @author mdanilkis
 * 
 */
public class LinesOfCreditService {

	private ServiceExecutor serviceExecutor;
	// MambuEntity managed by this service
	private static final MambuEntity serviceEntity = MambuEntity.LINE_OF_CREDIT;

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
	 * @param offset
	 *            pagination offset. If null, Mambu default (zero) will be used
	 * @param limit
	 *            pagination limit. If null, Mambu default will be used
	 * @return a list of all Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getAllLinesOfCredit(Integer offset, Integer limit) throws MambuApiException {
		// GET api/linesofcredit
		// Available since 3.11. See MBU-8414

		return serviceExecutor.getPaginatedList(serviceEntity, offset, limit);
	}

	/***
	 * Get line of credit
	 * 
	 * @param lineofcreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * 
	 * @return Line of Credit
	 * @throws MambuApiException
	 */
	public LineOfCredit getLineOfCredit(String lineofcreditId) throws MambuApiException {
		// GET api/linesofcredit/{id}
		// Available since 3.11. See MBU-8417

		// This API returns JSONLineOfCredit object
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_ENTITY, JSONLineOfCredit.class);
		JSONLineOfCredit jsonLineOfCredit = serviceExecutor.execute(apiDefinition, lineofcreditId);

		// Return LineOfCredit
		return (jsonLineOfCredit == null) ? null : jsonLineOfCredit.getLineOfCredit();
	}

	/***
	 * Get lines of credit for a Client or a Group
	 * 
	 * @param customerType
	 *            customer type (Client or Group). Mandatory. Must not be null.
	 * @param customerId
	 *            the encoded key or id of the customer. Mandatory. Must not be null.
	 * @param offset
	 *            pagination offset. If null, Mambu default (zero) will be used
	 * @param limit
	 *            pagination limit. If null, Mambu default will be used
	 * @return the List of Lines of Credit
	 * @throws MambuApiException
	 */
	public List<LineOfCredit> getCustomerLinesOfCredit(AccountHolderType customerType, String customerId,
			Integer offset, Integer limit) throws MambuApiException {
		// Example: GET /api/clients/{clientId}/linesofcredit or GET /api/groups/{groupId}/linesofcredit
		// Available since 3.11. See MBU-8413

		if (customerType == null) {
			throw new IllegalArgumentException("Customer type cannot be null");
		}

		MambuEntity customerEntity = (customerType == AccountHolderType.CLIENT) ? MambuEntity.CLIENT
				: MambuEntity.GROUP;
		return serviceExecutor.getOwnedEntities(customerEntity, customerId, serviceEntity, offset, limit);
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

		return getCustomerLinesOfCredit(AccountHolderType.CLIENT, clientId, offset, limit);
	}

	/***
	 * Convenience method to Get lines of credit for a Group
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
		// Example: GET /api/groups/{groupId}/linesofcredit
		// Available since 3.11. See MBU-8413

		return getCustomerLinesOfCredit(AccountHolderType.GROUP, groupId, offset, limit);
	}

	/***
	 * Get all accounts for a line of credit
	 * 
	 * @param lineofcreditId
	 *            the id or the encoded key of a Line Of Credit. Mandatory. Must not be null
	 * 
	 * @return accounts for the line of credit
	 * @throws MambuApiException
	 */
	public AccountsFromLineOfCredit getAccountsForLineOfCredit(String lineofcreditId) throws MambuApiException {
		// Example: GET /api/linesofcredit/{ID}/accounts
		// Available since 3.11. See MBU-8415

		ApiDefinition getAccountForLoC = new ApiDefinition(ApiType.GET_OWNED_ENTITY, LineOfCredit.class,
				AccountsFromLineOfCredit.class);
		return serviceExecutor.execute(getAccountForLoC, lineofcreditId);
	}
}
