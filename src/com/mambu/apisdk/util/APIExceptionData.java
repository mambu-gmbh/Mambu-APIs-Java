package com.mambu.apisdk.util;

import java.util.HashMap;

public class APIExceptionData {

	// Mambu Response Error Status codes (returnStatus)
	public static final String INVALID_BASIC_AUTHORIZATION = "INVALID_BASIC_AUTHORIZATION";
	public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
	public static final String INVALID_API_OPERATION = "INVALID_API_OPERATION";
	public static final String INVALID_PARAMETERS = "INVALID_PARAMETERS";
	public static final String METHOD_NOT_IMPLEMENTED = "METHOD_NOT_IMPLEMENTED";
	public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
	public static final String API_NOT_AUTHORIZED = "API_NOT_AUTHORIZED";
	public static final String USER_TRANSACTION_LIMIT_EXCEEDED = "USER_TRANSACTION_LIMIT_EXCEEDED";
	public static final String API_CONFIGURATION_ERROR = "API_CONFIGURATION_ERROR";
	public static final String INVALID_TENANT_ID = "INVALID_TENANT_ID";
	public static final String INVALID_PAGINATION_OFFSET_VALUE = "INVALID_PAGINATION_OFFSET_VALUE";
	public static final String OUT_OF_BOUNDS_PAGINATION_OFFSET_VALUE = "OUT_OF_BOUNDS_PAGINATION_OFFSET_VALUE";
	public static final String INVALID_PAGINATION_LIMIT_VALUE = "INVALID_PAGINATION_LIMIT_VALUE";
	public static final String OUT_OF_BOUNDS_PAGINATION_LIMIT_VALUE = "OUT_OF_BOUNDS_PAGINATION_LIMIT_VALUE";
	public static final String INVALID_PERMISSIONS = "INVALID_PERMISSIONS";
	public static final String INVALID_IP_ADDRESS = "INVALID_IP_ADDRESS";
	public static final String INACTIVE_USER = "INACTIVE_USER";
	public static final String NO_API_ACCESS = "NO_API_ACCESS";
	public static final String HARD_API_USAGE_LIMIT_EXCDEDED = "HARD_API_USAGE_LIMIT_EXCDEDED";
	public static final String INVALID_LOAN_ACCOUNT_ID = "INVALID_LOAN_ACCOUNT_ID";
	public static final String INVALID_AMOUNT = "INVALID_AMOUNT";
	public static final String INVALID_DATE = "INVALID_DATE";
	public static final String INVALID_FEE = "INVALID_FEE";
	public static final String LOAN_PRODUCT_MISMATCH = "LOAN_PRODUCT_MISMATCH";
	public static final String EXCESS_REPAYMENT_ERROR = "EXCESS_REPAYMENT_ERROR";
	public static final String INVALID_REPAYMENT_DATE_ERROR = "INVALID_REPAYMENT_DATE_ERROR";
	public static final String UNDEFINED_JOURNAL_ACTION_ERROR = "UNDEFINED_JOURNAL_ACTION_ERROR";
	public static final String INVALID_ACCOUNT_FOR_JOURNAL_ENTRY_ERROR = "INVALID_ACCOUNT_FOR_JOURNAL_ENTRY_ERROR";
	public static final String MISSING_LOAN_ID = "MISSING_LOAN_ID";
	public static final String MAXIMUM_EXPOSURE_EXCEEDED = "MAXIMUM_EXPOSURE_EXCEEDED";
	public static final String INVALID_STATE_TRANSITION = "INVALID_STATE_TRANSITION";
	public static final String NUMBER_OF_LOANS_EXCEEDD = "NUMBER_OF_LOANS_EXCEEDD";
	public static final String INVALID_REPAYMENT_DUE_DATE = "INVALID_REPAYMENT_DUE_DATE";
	public static final String INVALID_FIRST_REPAYMENT_DUE_DATE = "INVALID_FIRST_REPAYMENT_DUE_DATE";
	public static final String INVALID_REPAYMENT_DUE_DAY = "INVALID_REPAYMENT_DUE_DAY";
	public static final String INVALID_INTEREST_RATE = "INVALID_INTEREST_RATE";
	public static final String INVALID_INSTALLMENTS = "INVALID_INSTALLMENTS";
	public static final String REPAYMENT_DATE_BEFORE_LAST_PAYMENT_DATE_ERROR = "REPAYMENT_DATE_BEFORE_LAST_PAYMENT_DATE_ERROR";

	public static final String PREPAYMENT_NOT_ALLOWED_ERROR = "PREPAYMENT_NOT_ALLOWED_ERROR";
	public static final String INVALID_DISBURSEMENT_DATE = "INVALID_DISBURSEMENT_DATE";
	public static final String LINE_OF_CREDIT_EXCEEDED = "LINE_OF_CREDIT_EXCEEDED";
	public static final String LINE_OF_CREDIT_EXPIRED = "LINE_OF_CREDIT_EXPIRED";
	public static final String INVALID_ACCOUNT_STATE_FOR_REPAYMENTS = "INVALID_ACCOUNT_STATE_FOR_REPAYMENTS";
	public static final String DISBURSEMENT_FEES_EXCEED_LOAN_AMOUNT = "DISBURSEMENT_FEES_EXCEED_LOAN_AMOUNT";
	public static final String INTEREST_CANNOT_BE_APPLIED = "INTEREST_CANNOT_BE_APPLIED";
	public static final String ENTRY_DATE_BEFORE_OTHER_TRANSACTIONS = "ENTRY_DATE_BEFORE_OTHER_TRANSACTIONS";
	public static final String INCONSISTENT_SCHEDULE_PRINIPAL_DUE = "INCONSISTENT_SCHEDULE_PRINIPAL_DUE";
	public static final String MISSING_REPAYMENT_NUMBER = "MISSING_REPAYMENT_NUMBER";
	public static final String INVALID_REPAYMENT_NUMBER = "INVALID_REPAYMENT_NUMBER";
	public static final String UNKNOWN_LOAN_ACCOUNT_ERROR = "UNKNOWN_LOAN_ACCOUNT_ERROR";

	public static final String MISSING_GROUP_ID = "MISSING_GROUP_ID";
	public static final String INVALID_GROUP_ID = "INVALID_GROUP_ID";
	public static final String INVALID_FULL_DETAILS = "INVALID_FULL_DETAILS";
	public static final String INVALID_INDICATORS = "INVALID_INDICATORS";
	public static final String INVALID_PRODUCT_ID = "INVALID_PRODUCT_ID";
	public static final String INVALID_PARAMATERS_FOR_PRODUCT = "INVALID_PARAMATERS_FOR_PRODUCT";
	public static final String UNKNOWN_GROUP_ERROR = "UNKNOWN_GROUP_ERROR";

	public static final String MISSING_CLIENT_ID = "MISSING_CLIENT_ID";
	public static final String INVALID_CLIENT_ID = "INVALID_CLIENT_ID";
	public static final String UNKNOWN_CLIENT_ERROR = "UNKNOWN_CLIENT_ERROR";

	public static final String INVALID_SAVINGS_ACCOUNT_ID = "INVALID_SAVINGS_ACCOUNT_ID";
	public static final String BALANCE_BELOW_ZERO = "BALANCE_BELOW_ZERO";
	public static final String MISSING_SAVINGS_ID = "MISSING_SAVINGS_ID";
	public static final String BACKDATE_BEFORE_ACTIVATION = "BACKDATE_BEFORE_ACTIVATION";
	public static final String BACKDATE_BEFORE_OTHER_OPERATION = "BACKDATE_BEFORE_OTHER_OPERATION";
	public static final String INVALID_DEPOSIT_ACCOUNT_STATE = "INVALID_DEPOSIT_ACCOUNT_STATE";
	public static final String UNKNOWN_SAVINGS_ACCOUNT_ERROR = "UNKNOWN_SAVINGS_ACCOUNT_ERROR";

	public static final String INVALID_TARGET_ACCOUNTING_STATE = "INVALID_TARGET_ACCOUNTING_STATE";
	public static final String INVALID_GL_ACCOUNT_ID = "INVALID_GL_ACCOUNT_ID";
	public static final String INVALID_GL_ACCOUNT_TYPE = "INVALID_GL_ACCOUNT_TYPE";
	public static final String JOURNAL_ENTRY_BEFORE_CLOSURE = "JOURNAL_ENTRY_BEFORE_CLOSURE";
	public static final String DEBITS_DO_NOT_MATCH_CREDITS = "DEBITS_DO_NOT_MATCH_CREDITS";
	public static final String INVALID_JOURNAL_ENTRY_DATE = "INVALID_JOURNAL_ENTRY_DATE";
	public static final String GL_ACCOUNT_IS_HEADER = "GL_ACCOUNT_IS_HEADER";
	public static final String GL_ACCOUNT_DOES_NOT_SUPPORT_MANUALLY_ENTRIES = "GL_ACCOUNT_DOES_NOT_SUPPORT_MANUALLY_ENTRIES";

	public static final String INVALID_USER_NAME = "INVALID_USER_NAME";
	public static final String INVALID_USER_ID = "INVALID_USER_ID";

	public static final String INVALID_BRANCH_ID = "INVALID_BRANCH_ID";
	public static final String INVALID_CENTRE_ID = "INVALID_CENTRE_ID";

	public static final String CUSTOM_FIELD_NOT_FOUND = "CUSTOM_FIELD_NOT_FOUND";
	public static final String CUSTOM_FIELD_NOT_VALID = "CUSTOM_FIELD_NOT_VALID";
	public static final String REQUIRED_CUSTOM_FIELD_MISSING = "REQUIRED_CUSTOM_FIELD_MISSING";
	public static final String INVALID_CUSTOM_FIELD_ID = "INVALID_CUSTOM_FIELD_ID";
	public static final String INVALID_ID_DOCUMENT = "INVALID_ID_DOCUMENT";

	private static HashMap<String, Integer> API_ERROR_CODES_MAP;
	static {
		API_ERROR_CODES_MAP = new HashMap<String, Integer>();

		// Adding all mappings of a returnStatus to an returnCode
		// By knowing the returnStatus we can derive the returnCode

		API_ERROR_CODES_MAP.put(INVALID_BASIC_AUTHORIZATION, 1); // 1 INVALID_BASIC_AUTHORIZATION
		API_ERROR_CODES_MAP.put(INVALID_CREDENTIALS, 2); // 2 INVALID_CREDENTIALS
		API_ERROR_CODES_MAP.put(INVALID_API_OPERATION, 3); // 3 INVALID_API_OPERATION
		API_ERROR_CODES_MAP.put(INVALID_PARAMETERS, 4); // 4 INVALID_PARAMETERS

		API_ERROR_CODES_MAP.put(METHOD_NOT_IMPLEMENTED, 5); // 5 METHOD_NOT_IMPLEMENTED
		API_ERROR_CODES_MAP.put(INTERNAL_ERROR, 6); // 6 INTERNAL_ERROR

		API_ERROR_CODES_MAP.put(API_NOT_AUTHORIZED, 7); // 7 API_NOT_AUTHORIZED
		API_ERROR_CODES_MAP.put(USER_TRANSACTION_LIMIT_EXCEEDED, 8); // 8 USER_TRANSACTION_LIMIT_EXCEEDED
		API_ERROR_CODES_MAP.put(API_CONFIGURATION_ERROR, 9); // 9 API_CONFIGURATION_ERROR
		API_ERROR_CODES_MAP.put(INVALID_TENANT_ID, 10); // 10 INVALID_TENANT_ID
		API_ERROR_CODES_MAP.put(INVALID_PAGINATION_OFFSET_VALUE, 11); // 11 INVALID_PAGINATION_OFFSET_VALUE
		API_ERROR_CODES_MAP.put(OUT_OF_BOUNDS_PAGINATION_OFFSET_VALUE, 12); // 12 OUT_OF_BOUNDS_PAGINATION_OFFSET_VALUE
		API_ERROR_CODES_MAP.put(INVALID_PAGINATION_LIMIT_VALUE, 13); // 13 INVALID_PAGINATION_LIMIT_VALUE
		API_ERROR_CODES_MAP.put(OUT_OF_BOUNDS_PAGINATION_LIMIT_VALUE, 14); // 14 OUT_OF_BOUNDS_PAGINATION_LIMIT_VALUE
		API_ERROR_CODES_MAP.put(INVALID_PERMISSIONS, 15); // 15 INVALID_PERMISSIONS
		API_ERROR_CODES_MAP.put(INVALID_IP_ADDRESS, 16); // 16 INVALID_IP_ADDRESS
		API_ERROR_CODES_MAP.put(INACTIVE_USER, 17); // 17 INACTIVE_USER
		API_ERROR_CODES_MAP.put(NO_API_ACCESS, 18); // 18 NO_API_ACCESS
		API_ERROR_CODES_MAP.put(HARD_API_USAGE_LIMIT_EXCDEDED, 19); // 19 HARD_API_USAGE_LIMIT_EXCDEDED

		// 1xx
		API_ERROR_CODES_MAP.put(INVALID_LOAN_ACCOUNT_ID, 100); // 100 INVALID_LOAN_ACCOUNT_ID
		API_ERROR_CODES_MAP.put(INVALID_AMOUNT, 101); // 101 INVALID_AMOUNT
		API_ERROR_CODES_MAP.put(INVALID_DATE, 102); // 102 INVALID_DATE
		API_ERROR_CODES_MAP.put(INVALID_FEE, 106); // 106 INVALID_FEE
		API_ERROR_CODES_MAP.put(LOAN_PRODUCT_MISMATCH, 107); // 107 LOAN_PRODUCT_MISMATCH
		API_ERROR_CODES_MAP.put(EXCESS_REPAYMENT_ERROR, 110); // 110 EXCESS_REPAYMENT_ERROR
		API_ERROR_CODES_MAP.put(INVALID_REPAYMENT_DATE_ERROR, 111); // 111 INVALID_REPAYMENT_DATE_ERROR
		API_ERROR_CODES_MAP.put(UNDEFINED_JOURNAL_ACTION_ERROR, 112); // 112 UNDEFINED_JOURNAL_ACTION_ERROR
		API_ERROR_CODES_MAP.put(INVALID_ACCOUNT_FOR_JOURNAL_ENTRY_ERROR, 113); // 113
																				// INVALID_ACCOUNT_FOR_JOURNAL_ENTRY_ERROR
		API_ERROR_CODES_MAP.put(MISSING_LOAN_ID, 114); // 114 MISSING_LOAN_ID
		API_ERROR_CODES_MAP.put(MAXIMUM_EXPOSURE_EXCEEDED, 115); // 115 MAXIMUM_EXPOSURE_EXCEEDED
		API_ERROR_CODES_MAP.put(INVALID_STATE_TRANSITION, 116); // 116 INVALID_STATE_TRANSITION
		API_ERROR_CODES_MAP.put(NUMBER_OF_LOANS_EXCEEDD, 117); // 117 NUMBER_OF_LOANS_EXCEEDD
		API_ERROR_CODES_MAP.put(INVALID_REPAYMENT_DUE_DATE, 118); // 118 INVALID_REPAYMENT_DUE_DATE
		API_ERROR_CODES_MAP.put(INVALID_FIRST_REPAYMENT_DUE_DATE, 118); // 118 INVALID_FIRST_REPAYMENT_DUE_DATE
		API_ERROR_CODES_MAP.put(INVALID_REPAYMENT_DUE_DAY, 119); // 119 INVALID_REPAYMENT_DUE_DAY
		API_ERROR_CODES_MAP.put(INVALID_INTEREST_RATE, 120); // 120 INVALID_INTEREST_RATE
		API_ERROR_CODES_MAP.put(INVALID_INSTALLMENTS, 121); // 121 INVALID_INSTALLMENTS
		API_ERROR_CODES_MAP.put(REPAYMENT_DATE_BEFORE_LAST_PAYMENT_DATE_ERROR, 122); // 122
																						// REPAYMENT_DATE_BEFORE_LAST_PAYMENT_DATE_ERROR
		API_ERROR_CODES_MAP.put(PREPAYMENT_NOT_ALLOWED_ERROR, 123); // 123 PREPAYMENT_NOT_ALLOWED_ERROR
		API_ERROR_CODES_MAP.put(INVALID_DISBURSEMENT_DATE, 125); // 125 INVALID_DISBURSEMENT_DATE
		API_ERROR_CODES_MAP.put(LINE_OF_CREDIT_EXCEEDED, 126); // 126 LINE_OF_CREDIT_EXCEEDED
		API_ERROR_CODES_MAP.put(LINE_OF_CREDIT_EXPIRED, 127); // 127 LINE_OF_CREDIT_EXPIRED
		API_ERROR_CODES_MAP.put(INVALID_ACCOUNT_STATE_FOR_REPAYMENTS, 128); // 128 INVALID_ACCOUNT_STATE_FOR_REPAYMENTS
		API_ERROR_CODES_MAP.put(DISBURSEMENT_FEES_EXCEED_LOAN_AMOUNT, 129); // 129 DISBURSEMENT_FEES_EXCEED_LOAN_AMOUNT
		API_ERROR_CODES_MAP.put(INTEREST_CANNOT_BE_APPLIED, 130); // 130 INTEREST_CANNOT_BE_APPLIED
		API_ERROR_CODES_MAP.put(ENTRY_DATE_BEFORE_OTHER_TRANSACTIONS, 131); // 131 ENTRY_DATE_BEFORE_OTHER_TRANSACTIONS
		API_ERROR_CODES_MAP.put(INCONSISTENT_SCHEDULE_PRINIPAL_DUE, 131); // 131 INCONSISTENT_SCHEDULE_PRINIPAL_DUE
		API_ERROR_CODES_MAP.put(INVALID_REPAYMENT_NUMBER, 170); // 170 INVALID_REPAYMENT_NUMBER
		API_ERROR_CODES_MAP.put(MISSING_REPAYMENT_NUMBER, 171); // 171 MISSING_REPAYMENT_NUMBER
		API_ERROR_CODES_MAP.put(UNKNOWN_LOAN_ACCOUNT_ERROR, 199); // 199 UNKNOWN_LOAN_ACCOUNT_ERROR

		// 2xx
		API_ERROR_CODES_MAP.put(MISSING_GROUP_ID, 200); // 200 MISSING_GROUP_ID
		API_ERROR_CODES_MAP.put(INVALID_GROUP_ID, 201); // 201 INVALID_GROUP_ID
		API_ERROR_CODES_MAP.put(INVALID_FULL_DETAILS, 202); // 202 INVALID_FULL_DETAILS
		API_ERROR_CODES_MAP.put(INVALID_INDICATORS, 203); // 203 INVALID_INDICATORS
		API_ERROR_CODES_MAP.put(INVALID_PRODUCT_ID, 204); // 204 INVALID_PRODUCT_ID
		API_ERROR_CODES_MAP.put(INVALID_PARAMATERS_FOR_PRODUCT, 205); // 205 INVALID_PARAMATERS_FOR_PRODUCT
		API_ERROR_CODES_MAP.put(UNKNOWN_GROUP_ERROR, 299); // 299 UNKNOWN_GROUP_ERROR

		// 3xx
		API_ERROR_CODES_MAP.put(MISSING_CLIENT_ID, 300); // 300 MISSING_CLIENT_ID
		API_ERROR_CODES_MAP.put(MISSING_CLIENT_ID, 301); // 301 INVALID_CLIENT_ID
		API_ERROR_CODES_MAP.put(UNKNOWN_CLIENT_ERROR, 399); // 399 UNKNOWN_CLIENT_ERROR

		// 4xx
		API_ERROR_CODES_MAP.put(INVALID_SAVINGS_ACCOUNT_ID, 400); // 400 INVALID_SAVINGS_ACCOUNT_ID
		API_ERROR_CODES_MAP.put(BALANCE_BELOW_ZERO, 401); // 401 BALANCE_BELOW_ZERO
		API_ERROR_CODES_MAP.put(MISSING_SAVINGS_ID, 402); // 402 MISSING_SAVINGS_ID
		API_ERROR_CODES_MAP.put(BACKDATE_BEFORE_ACTIVATION, 403); // 403 BACKDATE_BEFORE_ACTIVATION
		API_ERROR_CODES_MAP.put(BACKDATE_BEFORE_OTHER_OPERATION, 404); // 404 BACKDATE_BEFORE_OTHER_OPERATION
		API_ERROR_CODES_MAP.put(INVALID_DEPOSIT_ACCOUNT_STATE, 407); // 407, INVALID_DEPOSIT_ACCOUNT_STATE
		API_ERROR_CODES_MAP.put(UNKNOWN_SAVINGS_ACCOUNT_ERROR, 499); // 499 UNKNOWN_SAVINGS_ACCOUNT_ERROR

		// 5xx
		API_ERROR_CODES_MAP.put(INVALID_TARGET_ACCOUNTING_STATE, 502); // 502 INVALID_TARGET_ACCOUNTING_STATE

		// 6xx
		API_ERROR_CODES_MAP.put(INVALID_GL_ACCOUNT_ID, 600); // 600 INVALID_GL_ACCOUNT_ID
		API_ERROR_CODES_MAP.put(INVALID_GL_ACCOUNT_TYPE, 601); // 601 INVALID_GL_ACCOUNT_TYPE
		API_ERROR_CODES_MAP.put(JOURNAL_ENTRY_BEFORE_CLOSURE, 602); // 602 JOURNAL_ENTRY_BEFORE_CLOSURE
		API_ERROR_CODES_MAP.put(DEBITS_DO_NOT_MATCH_CREDITS, 603); // 603 �DEBITS_DO_NOT_MATCH_CREDITS
		API_ERROR_CODES_MAP.put(INVALID_JOURNAL_ENTRY_DATE, 604); // 604 �INVALID_JOURNAL_ENTRY_DATE
		API_ERROR_CODES_MAP.put(GL_ACCOUNT_IS_HEADER, 605); // 605 �GL_ACCOUNT_IS_HEADER
		API_ERROR_CODES_MAP.put(GL_ACCOUNT_DOES_NOT_SUPPORT_MANUALLY_ENTRIES, 606); // 606
																					// �GL_ACCOUNT_DOES_NOT_SUPPORT_MANUALLY_ENTRIES

		// 7xx
		API_ERROR_CODES_MAP.put(INVALID_USER_NAME, 700); // 700 INVALID_USER_NAME
		API_ERROR_CODES_MAP.put(INVALID_USER_ID, 701); // 701 INVALID_USER_ID

		// 8xx
		API_ERROR_CODES_MAP.put(INVALID_BRANCH_ID, 800); // 800 INVALID_BRANCH_ID
		API_ERROR_CODES_MAP.put(INVALID_CENTRE_ID, 851); // 851 INVALID_CENTRE_ID

		// 9xx
		API_ERROR_CODES_MAP.put(CUSTOM_FIELD_NOT_FOUND, 900); // 900 CUSTOM_FIELD_NOT_FOUND
		API_ERROR_CODES_MAP.put(CUSTOM_FIELD_NOT_VALID, 901); // 901 CUSTOM_FIELD_NOT_VALID
		API_ERROR_CODES_MAP.put(REQUIRED_CUSTOM_FIELD_MISSING, 902); // 902 REQUIRED_CUSTOM_FIELD_MISSING
		API_ERROR_CODES_MAP.put(INVALID_CUSTOM_FIELD_ID, 903); // 903 INVALID_CUSTOM_FIELD_ID
		API_ERROR_CODES_MAP.put(INVALID_ID_DOCUMENT, 950); // 950 INVALID_ID_DOCUMENT

	}

	// Look up the returnCode code by the returnStatus. The Result Status string is unique in Mambu's API error
	// messages. (There are some duplicate error codes (e.g. of duplicates: 118, 131))

	public static int getResponseCode(String returnStatus) {
		if (API_ERROR_CODES_MAP.containsKey(returnStatus)) {
			return API_ERROR_CODES_MAP.get(returnStatus);
		}
		return -1;

	}

	// TODO:
	public static String getUserMessage(String returnStatus) {
		// Return localized user message for API_ERROR_CODES_MAP.put(API_NOT_AUTHORIZED, 7);. E,g for INVALID_USER_NAME
		String userMessage = "";
		return userMessage;
	}

}
