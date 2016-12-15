/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.Repayment;

/**
 * Service class which handles API operations like entering and getting repayments
 * 
 * @author ipenciuc
 * 
 */
public class RepaymentsService {

	private static final String DUE_FROM = APIData.DUE_FROM;
	private static final String DUE_TO = APIData.DUE_TO;

	private static final String OFFSET = APIData.OFFSET;
	private static final String LIMIT = APIData.LIMIT;

	private ServiceExecutor serviceExecutor;

	// Create API definitions for services provided by ClientService

	private final static ApiDefinition getRepaymments = new ApiDefinition(ApiType.GET_LIST, Repayment.class);
	private final static ApiDefinition getRepaymentsForLoan = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			LoanAccount.class, Repayment.class);
	// Update Loan Repayments. PATCH JSON /api/loans/loan_id/repayments
	private static ApiDefinition updateRepaymentsForLoan = new ApiDefinition(ApiType.PATCH_OWNED_ENTITIES,
			LoanAccount.class, Repayment.class);

	/***
	 * Create a new repayments service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public RepaymentsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Get a loan account Repayments between FromDate and ToDate
	 * 
	 * @param dueFromString
	 * @param dueToString
	 * @param offset
	 *            pagination offset. Has to be >= 0 if not null. If null, Mambu default will be used
	 * @param limit
	 *            pagination limit. Has to be > 0 if not null. If null, Mambu default will be used
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getRapaymentsDueFromTo(String dueFromString, String dueToString, String offset, String limit)
			throws MambuApiException {

		// E.g. GET /api/repayments?dueFrom=2011-01-05&dueTo=2011-06-07&offset=0&limit100
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(DUE_FROM, dueFromString);
		paramsMap.put(DUE_TO, dueToString);
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);

		return serviceExecutor.execute(getRepaymments, paramsMap);
	}

	/***
	 * Get a all Repayments by Loan account id with an offset and limit parameters
	 * 
	 * @param accountId
	 *            the id of the loan account limit - last transaction number Note: if offset and limit both equal null,
	 *            all transactions are returned
	 * @param offset
	 *            pagination offset. Has to be >= 0 if not null. If null, Mambu default will be used
	 * @param limit
	 *            pagination limit. Has to be > 0 if not null. If null, Mambu default will be used
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getLoanAccountRepayments(String accountId, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);
		return serviceExecutor.execute(getRepaymentsForLoan, accountId, paramsMap);
	}

	/***
	 * Update unpaid repayment schedule for a Loan Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the Mambu Loan Account for which repayments are updated
	 * @param repayments
	 *            a list of still unpaid repayments to be updated. Paid installments can be posted, but no changes will
	 *            be performed on them. The encoded key of the repayment is mandatory. The following repayment fields
	 *            are considered for update: principal, interest, fees, penalties, due amounts and due date from the
	 *            repayments that get posted. For Revolving Credit Loans loans only due date is a considered for update
	 *            (other fields, if present, must remain unchanged)
	 * 
	 *            See MBU-6813, MBU-10245 and MBU-10546 for more details
	 * 
	 * @return updated repayments
	 * @throws MambuApiException
	 */
	public List<Repayment> updateLoanRepaymentsSchedule(String accountId, JSONLoanRepayments repayments)
			throws MambuApiException {

		// Available since Mambu 3.9. See MBU-6813. For Revolving Credit product available since 3.14. See MBU-10546
		// Available for fixed loans, when the account is in Pending/Partial state since 3.13. See MBU-10245
		// API example: PATCH -d JSONLoanRepayments_object /api/loans/loan_id/repayments. Returns list of Repayments
		// This API accepts JSON requests with the dates in "yyyy-MM-dd" format only
		updateRepaymentsForLoan.setJsonDateTimeFormat(APIData.yyyyMmddFormat);
		return serviceExecutor.executeJson(updateRepaymentsForLoan, repayments, accountId);

	}

	/***
	 * Get repayments schedule relative to an investor funding. Investors that are contributing with funds for
	 * disbursing a loan account have their own schedule, where they can see what collections they expect to have for
	 * that account.
	 * 
	 * @param savingsId
	 *            the encoded key or id of the Savings Investor account linked to the loan account. Must not be NULL
	 * 
	 * @param loanId
	 *            the encoded key or id of the Investor Loan account. Must not be NULL
	 * 
	 * @return the List of Repayments relative to an investor funding
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getInvestorFundingRepayments(String savingsId, String loanId) throws MambuApiException {

		// Example: GET endpoint "/api/savings/{SAVINGS_ID}/funding/{LOAN_ID}/repayments
		// Available since Mambu 3.13. See MBU-9888.
		if (savingsId == null || loanId == null) {
			throw new IllegalArgumentException("Savings Account ID and Loan AccounT ID  must not be NULL");
		}

		// URL path for this API has the following format: /api/savings/{SAVINGS_ID}/funding/{LOAN_ID}/repayments
		String urlPath = APIData.SAVINGS + "/" + savingsId + "/" + APIData.FUNDING + "/" + loanId + "/"
				+ APIData.REPAYMENTS;
		ApiDefinition apiDefinition = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, Repayment.class,
				ApiReturnFormat.COLLECTION);

		return serviceExecutor.execute(apiDefinition);
	}

	/**
	 * Deletes a repayment for a supplied loan account ID and a repayment ID.
	 * 
	 * NOTE: Only installments with 0 principal can be deleted.
	 * 
	 * @param accountId
	 *            The ID of the Loan account
	 * @param repaymentId
	 *            The ID of the repayment to be deleted
	 * @return true in case of success
	 * @throws MambuApiException
	 */
	public Boolean deleteLoanRepayment(String accountId, String repaymentId) throws MambuApiException {

		// Example: GET endpoint "/api/loans/{LOAN_ID}/repayments/{REPAYMENT_ID}
		// Available since Mambu 4.3

		if (accountId == null || repaymentId == null) {
			throw new IllegalArgumentException("Account ID and Repayment ID must not be null!");
		}

		return serviceExecutor.deleteOwnedEntity(MambuEntityType.LOAN_ACCOUNT, accountId, MambuEntityType.REPAYMENTS,
				repaymentId);

	}
}
