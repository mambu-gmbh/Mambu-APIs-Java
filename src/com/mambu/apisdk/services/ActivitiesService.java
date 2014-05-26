package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;

/**
 * Service class which handles activities API operations like getting activities for Mambu entities. See full Activities
 * API documentation at @http://api.mambu.com/customer/portal/articles/1453628-activities-api?b_id=874
 * 
 * @author mdanilkis
 * 
 */
public class ActivitiesService {

	private MambuAPIService mambuAPIService;

	private static String ACTIVITIES = APIData.ACTIVITIES;
	private static String FROM = APIData.FROM;
	private static String TO = APIData.TO;

	/***
	 * Create a new activities service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public ActivitiesService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * GET all activity feed items within a specified date interval and (optionally) for a specified Mambu entity Allows
	 * retrieving a list of activities within a date range which can be filtered by entity key.
	 * 
	 * 
	 * @param fromDate
	 *            starting date for the time interval (mandatory). Only the full date without time is used, the date is
	 *            inclusive
	 * @param toDate
	 *            end date for the time interval (mandatory). Only the full date without time is used,the date is
	 *            inclusive
	 * @param mambuEntity
	 *            Mambu Entity for requested activities. If Mambu entity is null then all available activities for all
	 *            entities supported by API are returned. The following classes are currently supported: Client, Group,
	 *            Centre, Branch, LoanProduct, SavingsProduct, LoanAccount, SvaingsAccount, User
	 * 
	 * @param entityId
	 *            the Id for the Mambu entity for requested activities
	 * 
	 * 
	 * @return a list of JSONActivities
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("rawtypes")
	public List<JSONActivity> getActivities(Date fromDate, Date toDate, Class mambuEntity, String entityId)
			throws MambuApiException {

		// From Date and To Date are mandatory
		if (fromDate == null) {
			throw new IllegalArgumentException("From Date must not be NULL");
		}
		if (toDate == null) {
			throw new IllegalArgumentException("To Date must not be NULL");
		}

		// Mambu Entity class and its ID must be either both NULL or both NOT NULL
		if ((mambuEntity == null && entityId != null) || (mambuEntity != null && entityId == null)) {
			throw new IllegalArgumentException(
					"Mambu Entity class and its ID must be either both NULL or both NOT NULL");
		}

		// Format dates as API requirements: "yyyy-MM-dd
		final String dateTimeFormat = APIData.yyyyMmddFormat;

		ParamsMap params = new ParamsMap();

		try {
			String formattedFromDate = new SimpleDateFormat(dateTimeFormat).format(fromDate);
			params.put(FROM, formattedFromDate);

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid From Date");
		}

		try {
			String formattedToDate = new SimpleDateFormat(dateTimeFormat).format(toDate);
			params.put(TO, formattedToDate);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid To Date");
		}

		// Get the name of the ID parameter based on the requested Mambu Class and add id to the ParamsMap
		if (mambuEntity != null) {
			params.put(getIdParameterName(mambuEntity), entityId);
		}

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(ACTIVITIES + "/"));

		String activitiesResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<JSONActivity>>() {
		}.getType();
		List<JSONActivity> activities = GsonUtils.createGson().fromJson(activitiesResposne, collectionType);

		return activities;
	}

	/***
	 * A convenience method to GET All activities within a specified date interval
	 * 
	 * @param fromDate
	 *            starting date for the time interval (mandatory).Only the full date without time is used, the date is
	 *            inclusive
	 * @param toDate
	 *            end date for the time interval (mandatory). Only the full date without time is used,the date is
	 *            inclusive
	 * 
	 * @return a list of JSONActivities
	 * 
	 * @throws MambuApiException
	 */
	public List<JSONActivity> getActivities(Date fromDate, Date toDate) throws MambuApiException {

		return getActivities(fromDate, toDate, null, null);
	}

	// Private helper
	/***
	 * Get the name of the ID parameter for the entity ID value. The ID Name in the GET request must be one of the
	 * following: clientID, groupID, centreID, branchID, loanProductID, savingsProductID, loanAccountID,
	 * savingsAccountID or userID
	 * 
	 * @param mambuClass
	 *            the class of the mambu entity for which the ID is provided. The following classes are currently
	 *            supported: Client, Group, Centre, Branch, LoanProduct, SavingsProduct, LoanAccount, SvaingsAccount,
	 *            User
	 * 
	 * @return idParameterName the name of the parameter to be used in the API request (e.g clientID, loanAccountID,
	 *         etc.)
	 * 
	 * @throws illegalArgumentException
	 *             thrown if the class is not one of the supported by the Activities API
	 */
	@SuppressWarnings("rawtypes")
	private String getIdParameterName(Class mambuClass) {

		// Clients and Groups
		if (mambuClass.equals(Client.class)) {
			return APIData.CLIENT_ID;
		}
		if (mambuClass.equals(Group.class)) {
			return APIData.GROUP_ID;
		}
		// Branches and Centres
		if (mambuClass.equals(Branch.class)) {
			return APIData.BRANCH_ID;
		}
		if (mambuClass.equals(Centre.class)) {
			return APIData.CENTRE_ID;
		}

		// Loan Accounts and Savings Accounts
		if (mambuClass.equals(LoanAccount.class)) {
			return APIData.LOAN_ACCOUNT_ID;
		}
		if (mambuClass.equals(SavingsAccount.class)) {
			return APIData.SAVINGS_ACCOUNT_ID;
		}

		// Loan Products and Savings Products
		if (mambuClass.equals(LoanProduct.class)) {
			return APIData.LOAN_PRODUCT_ID;
		}
		if (mambuClass.equals(SavingsProduct.class)) {
			return APIData.SAVINGS_PRODUCT_ID;
		}

		// Users
		if (mambuClass.equals(SavingsProduct.class)) {
			return APIData.USER_ID;
		}

		// Other types of activities are not supported by the API (e.g GLACCOUNTING, DATA, DOCUMENTS)
		// See http://api.mambu.com/customer/portal/articles/1453628-activities-api
		throw new IllegalArgumentException("Mambu Entity " + mambuClass.getSimpleName()
				+ " is NOT supported by Activities API");

	}

}