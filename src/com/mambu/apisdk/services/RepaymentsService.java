/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.loans.shared.model.Repayment;

/**
 * Service class which handles API operations like entering and getting repayments
 * 
 * @author ipenciuc
 * 
 */
public class RepaymentsService {

	private static final String REPAYMENTS = APIData.REPAYMENTS;
	private static final String LOANS = APIData.LOANS;

	private static final String DUE_FROM = APIData.DUE_FROM;
	private static final String DUE_TO = APIData.DUE_TO;

	private static final String OFFSET = APIData.OFFSET;
	private static final String LIMIT = APIData.LIMIT;

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new repayments service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public RepaymentsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * Get a all Repayments by Loan account id
	 * 
	 * @param accountId
	 *            the id of the loan account
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getLoanAccountRepayments(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId) + "/" + REPAYMENTS);
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<Repayment>>() {
		}.getType();
		List<Repayment> repayments = GsonUtils.createGson().fromJson(jsonResposne, collectionType);
		return repayments;
	}

	/***
	 * Get a loan account Repayments between FromDate and ToDate
	 * 
	 * @param dueFomString
	 * @param dueToString
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getRapaymentsDueFromTo(String dueFomString, String dueToString) throws MambuApiException {
		// E.g. GET /api/repayments?dueFrom=2011-01-05&dueTo=2011-06-07

		String urlString = new String(mambuAPIService.createUrl(REPAYMENTS + "/"));
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(DUE_FROM, dueFomString);
		paramsMap.put(DUE_TO, dueToString);

		String jsonResposne = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);
		Type collectionType = new TypeToken<List<Repayment>>() {
		}.getType();

		List<Repayment> repayments = GsonUtils.createGson().fromJson(jsonResposne, collectionType);

		return repayments;
	}

	/***
	 * Get a all Repayments by Loan account id with an offset and limit params
	 * 
	 * @param accountId
	 *            the id of the loan account limit - last transaction number Note: if offset and limit both equal null,
	 *            all transactions are returned
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	// TODO: The offset and limit params are not supported by repayments. API returns all. To be investigated further
	public List<Repayment> getLoanAccountRepayments(String accountId, String offset, String limit)
			throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId) + "/" + REPAYMENTS);

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);

		String jsonResposne = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<Repayment>>() {
		}.getType();
		List<Repayment> repayments = GsonUtils.createGson().fromJson(jsonResposne, collectionType);
		return repayments;
	}

}
