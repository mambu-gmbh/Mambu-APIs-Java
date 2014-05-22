package com.mambu.apisdk.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

//
// This class defines string constants and other constants for Mambu API services
//
//
public class APIData {

	public final static String APPLICATION_KEY = "appkey";

	//
	public static enum ACCOUNT_TYPE {
		LOAN, SAVINGS
	};

	// ----- Date Formatting -----
	public static final DateFormat URLDATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	// Users API

	public static final String USERS = "users";

	// Loans and Savings API
	public static final String LOANS = "loans";
	public static final String SAVINGS = "savings";
	public static final String CLIENTS = "clients";
	public static final String GROUPS = "groups";
	public static final String FULL_DETAILS = "fullDetails";

	public static final String DATE = "date";
	public static final String FIRST_REPAYMENT_DATE = "firstRepaymentDate";

	public static final String TYPE = "type";
	public static final String BANK_NUMBER = "bankNumber";
	public static final String RECEIPT_NUMBER = "receiptNumber";
	public static final String CHECK_NUMBER = "checkNumber";
	public static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
	public static final String BANK_ROUTING_NUMBER = "bankRoutingNumber";
	public static final String NOTES = "notes";
	//
	public static final String TRANSACTIONS = "transactions";
	public static final String TYPE_REPAYMENT = "REPAYMENT";
	public static final String TYPE_DISBURSMENT = "DISBURSMENT";
	public static final String TYPE_APPROVAL = "APPROVAL";
	public static final String TYPE_UNDO_APPROVAL = "UNDO_APPROVAL";
	public static final String TYPE_REJECT = "REJECT";
	public static final String TYPE_FEE = "FEE";
	public static final String TYPE_DEPOSIT = "DEPOSIT";
	public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
	public static final String TYPE_TRANSFER = "TRANSFER";
	public static final String TYPE_LOCK = "LOCK";
	public static final String TYPE_UNLOCK = "UNLOCK";

	// Type of account closer transaction
	public static enum CLOSER_TYPE {
		REJECT, WITHDRAW
	};

	public static final String AMOUNT = "amount";

	public static final String PAYMENT_METHOD = "method";
	public static final String CASH_METHOD = "CASH";
	public static final String RECEIPT_METHOD = "RECEIPT";
	public static final String CHECK_METHOD = "CHECK";
	public static final String BANK_TRANSFER_METHOD = "BANK_TRANSFER";
	public static final String REPAYMENT_NUMBER = "repayment";

	public static final String TO_SAVINGS = "toSavingsAccount";
	public static final String TO_LOAN = "toLoanAccount";

	// Filters
	public static final String BRANCH_ID = "branchId";
	public static final String CREDIT_OFFICER_USER_NAME = "creditOfficerUsername";
	public static final String ACCOUNT_STATE = "accountState";
	public static final String CLIENT_STATE = "state";

	public static final String OFFSET = "offset";
	public static final String LIMIT = "limit";

	// Products
	public static final String LOANPRODUCTS = "loanproducts";
	public static final String SAVINGSRODUCTS = "savingsproducts";

	// Tasks
	public static final String TITLE = "title";
	public static final String USERNAME = "username";
	public static final String DESCRIPTION = "description";
	public static final String DUE_DATE = "duedate";
	public static final String CLIENT_ID = "clientid";
	public static final String GROUP_ID = "groupid";

	// Accounting API
	public static final String GLACCOUNTS = "glaccounts";
	public static final String GLJOURNALENTRIES = "gljournalentries";

	// Intelligence API
	public static final String INDICATORS = "indicators";

	// Organization services
	public static String BRANCHES = "branches";
	public static String CENTRES = "centres";
	public static String CURRENCIES = "currencies";

	// Custom fields and Custom Field Sets
	public static String CUSTOM_FIELDS = "customfields";
	public static String CUSTOM_FIELD_SETS = "customfieldsets";
	public static String CUSTOM_FIELD_SETS_TYPE = "type";

	// Repayments
	public static final String REPAYMENTS = "repayments";

	public static final String DUE_FROM = "dueFrom";
	public static final String DUE_TO = "dueTo";

	// Search API
	// API's endpoint
	public static String SEARCH = "search";
	public static String QUERY = "query";
	public static String SEARCH_TYPES = "type";

	// Tasks API
	// API's endpoint
	public static String TASKS = "tasks";
	public static String STATUS = "status";

	// Documents API
	// API's endpoint
	public static String DOCUMENTS = "documents";

	// Image downloads
	// API end point
	public static String IMAGES = "images";
	// Params for Image downloads
	public static String SIZE = "size";

	public enum IMAGE_SIZE_TYPE {
		LARGE, MEDIUM, SMALL_THUMB, TINY_THUMB
	}

	// Activities API
	public static final String ACTIVITIES = "activities";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String CENTRE_ID = "centreID";
	public static final String SAVINGS_ACCOUNT_ID = "savingsAccountID";
	public static final String LOAN_ACCOUNT_ID = "loanAccountID";
	public static final String LOAN_PRODUCT_ID = "loanProductID";
	public static final String SAVINGS_PRODUCT_ID = "savingsProductID";
	public static final String USER_ID = "userID";

	// Client fields
	public static String FIRST_NAME = "firstName";
	public static String LAST_NAME = "lastName";
	public static String HOME_PHONE = "homephone";
	public static String MOBILE_PHONE = "mobilephone";
	public static String GENDER = "gender";
	public static String BIRTH_DATE = "birthdate";
	public static String EMAIL_ADDRESS = "email";
	public static String ID_DOCUMENT = "idDocument";

	// Added to support Json object creation API
	public static String yyyyMmddFormat = "yyyy-MM-dd";
	public static String JSON_OBJECT = "JSON";

	// Base64 encoded strings header's terminator in API responses
	public static String BASE64_ENCODING_INDICATOR = ";base64,";

}
