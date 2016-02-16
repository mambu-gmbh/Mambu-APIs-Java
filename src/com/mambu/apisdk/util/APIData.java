package com.mambu.apisdk.util;

//
// This class defines string constants and other constants for Mambu API services
//
//
public class APIData {

	public final static String APPLICATION_KEY = "appkey";

	// Users API

	public static final String USERS = "users";
	// Custom Views. Added in Mambu 3.7
	public static final String VIEWS = "views";
	public static final String FOR = "for";
	public static final String VIEW_FILTER = "viewfilter";
	public static final String RESULT_TYPE = "resultType"; // added in 3.14 to support what data to return
	public static final String COLUMNS = "COLUMNS"; // return custom view columns
	public static final String BASIC = "BASIC"; // returns entities without any extra details
	// "FULL_DETAILS"- already defined. Returns entities with full details

	// Loans and Savings API
	public static final String LOANS = "loans";
	public static final String SAVINGS = "savings";
	public static final String CLIENTS = "clients";
	public static final String GROUPS = "groups";
	public static final String FULL_DETAILS = "fullDetails";
	public static final String ACTIVE = "ACTIVE";
	public static final String INACTIVE = "INACTIVE";
	public static final String TRANCHES = "tranches"; // available since 3.12.3. See MBU-9996
	public static final String FUNDS = "funds"; // investor funds. available since Mambu 3.13. See MBU-9885
	public static final String FUNDING = "funding"; // investor funding. available since Mambu 3.13. See MBU-9888

	// Client Types. Added in 3.9
	public static final String CLIENT_TYPES = "clienttypes";
	// Group Role Names. Added in 3.9
	public static final String GROUP_ROLE_NAMES = "grouprolenames";

	public static final String DATE = "date";
	public static final String FIRST_REPAYMENT_DATE = "firstRepaymentDate";

	// Transaction Channels endpoint
	public static final String TRANSACTION_CHANNELS = "transactionchannels";

	public static final String TYPE = "type";
	// Transaction Channel Fields
	public static final String BANK_NUMBER = "bankNumber";
	public static final String RECEIPT_NUMBER = "receiptNumber";
	public static final String CHECK_NUMBER = "checkNumber";
	public static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
	public static final String BANK_ROUTING_NUMBER = "bankRoutingNumber";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String IDENTIFIER = "identifier";

	public static final String NOTES = "notes";
	//
	public static final String TRANSACTIONS = "transactions";
	public static final String TYPE_REPAYMENT = "REPAYMENT";
	public static final String TYPE_DISBURSEMENT = "DISBURSEMENT"; // Spelling corrected in 3.11 See MBU-7004
	public static final String TYPE_DISBURSMENT_ADJUSTMENT = "DISBURSMENT_ADJUSTMENT"; // added in 3.9
	public static final String TYPE_PENALTY_ADJUSTMENT = "PENALTY_ADJUSTMENT"; // added in 3.13. See MBU-9998
	public static final String TYPE_REQUEST_APPROVAL = "PENDING_APPROVAL"; // added in 13.3. See MBU-9814
	public static final String TYPE_APPROVAL = "APPROVAL";
	public static final String TYPE_UNDO_APPROVAL = "UNDO_APPROVAL";
	public static final String TYPE_FEE = "FEE";
	public static final String TYPE_DEPOSIT = "DEPOSIT";
	public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
	public static final String TYPE_TRANSFER = "TRANSFER";
	public static final String TYPE_LOCK = "LOCK";
	public static final String TYPE_UNLOCK = "UNLOCK";
	public static final String TYPE_INTEREST_APPLIED = "INTEREST_APPLIED";
	public static final String TYPE_WRITE_OFF = "WRITE_OFF"; // added in 3.14. See MBU-10423
	// Savings reversal transactions
	public static final String TYPE_DEPOSIT_ADJUSTMENT = "DEPOSIT_ADJUSTMENT";
	public static final String TYPE_WITHDRAWAL_ADJUSTMENT = "WITHDRAWAL_ADJUSTMENT";
	public static final String TYPE_TRANSFER_ADJUSTMENT = "TRANSFER_ADJUSTMENT";

	// Type of account closer transaction
	public static enum CLOSER_TYPE {
		REJECT, WITHDRAW
	};

	public static final String AMOUNT = "amount";

	// The PAYMENT_METHOD indicates the ID of the Transaction Channel used for the account transaction
	public static final String PAYMENT_METHOD = "method";

	public static final String REPAYMENT_NUMBER = "repayment";

	public static final String TO_SAVINGS = "toSavingsAccount";
	public static final String TO_LOAN = "toLoanAccount";
	public static final String ORIGINAL_TRANSACTION_ID = "originalTransactionId";

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

	// Interest Rate Sources source API (available since 3.10)
	public static final String INDEXRATESOURCES = "indexratesources";
	public static final String INDEXRATES = "indexrates";

	// Comments (available since 3.11)
	public static final String COMMENTS = "comments";

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
	// Parameters for posting GL Journal Entries. See MBU-1737. Available since 2.2
	public static String DEBIT_ACCOUNT = "debitAccount";
	public static String DEBIT_AMOUNT = "debitAmount";
	public static String CREDIT_ACCOUNT = "creditAccount";
	public static String CREDIT_AMOUNT = "creditAmount";

	// Intelligence API
	public static final String INDICATORS = "indicators";

	// Organization services
	public static String BRANCHES = "branches";
	public static String CENTRES = "centres";
	public static String CURRENCIES = "currencies";

	// Custom fields and Custom Field Sets
	public static String CUSTOM_FIELDS = "customfields";
	public static String CUSTOM_FIELD_SETS = "customfieldsets";
	public static String CUSTOM_INFORMATION = "custominformation";
	public static String CUSTOM_FIELD_SETS_TYPE = "type";

	// Repayments
	public static final String REPAYMENTS = "repayments";

	// Schedule for Loan Products endpoint. Added in 3.9
	public static final String SCHEDULE = "schedule";
	// Parameters supported by the loan product schedule API (MBU-6789) and loan terms patch API (MBU-7758)
	public static final String LOAN_AMOUNT = "loanAmount";
	public static final String ANTICIPATE_DISBURSEMENT = "anticipatedDisbursement";
	public static final String EXPECTED_DISBURSEMENT_DATE = "expectedDisbursementDate";
	// "firstRepaymentDate" - is already defined as FIRST_REPAYMENT_DATE
	public static final String INTEREST_RATE = "interestRate";
	public static final String INTEREST_RATE_SPREAD = "interestSpread";
	public static final String REPAYMENT_INSTALLMENTS = "repaymentInstallments";
	public static final String GRACE_PERIOD = "gracePeriod";
	public static final String REPAYMENT_PERIOD_UNIT = "repaymentPeriodUnit";
	public static final String REPAYMENT_PERIOD_COUNT = "repaymentPeriodCount";
	public static final String PRNICIPAL_REPAYMENT_INTERVAL = "principalRepaymentInterval";
	public static final String PERIODIC_PAYMENT = "periodicPayment";
	public static final String PENALTY_RATE = "penaltyRate";
	public static final String FIXED_DAYS_OF_MONTH = "fixedDaysOfMonth"; // supported since 3.14. See MBU-10802

	// Parameters supported by SavingsAccount PATCH API
	public static final String OVERDRAFT_LIMIT = "overdraftLimit";
	public static final String OVERDRAFT_INTEREST_RATE = "overdraftInterestRate";
	public static final String OVERDRAFT_SPREAD = "overdraftInterestSpread";
	public static final String OVERDRAFT_EXPIRY_DATE = "overdraftExpiryDate";
	public static final String MAX_WITHDRAWAL_AMOUNT = "maxWidthdrawlAmount";
	public static final String RECOMMENDED_DEPOSIT_AMOUNT = "recommendedDepositAmount";
	public static final String TARGET_AMOUNT = "targetAmount";

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

	// Document Template API
	public static String TEMPLATES = "templates"; // /api/loans/{LOAN_ID}/templates/
	public static String START_DATE = "startDate";
	public static String END_DATE = "endDate";

	// API endpoints for uploading client profile picture and signature. Available since 3.9
	public static String PROFILE_PICTURE = "PROFILE_PICTURE";
	public static String SIGNATURE = "SIGNATURE";
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
	public static String BIRTH_DATE = "birthdate";
	public static String ID_DOCUMENT = "idDocument";

	// Api endpoint for Organisational Settings. Available since 3.10.5
	public static String SETTINGS = "settings";
	// Api endpoint for ID Document Templates. Available since 3.10.5
	public static String ID_DOCUMENT_TEMPLATES = "iddocumenttemplates"; // /api/settings/iddocumenttemplates
	// Api endpoints for organization. Available since 3.11
	public static String ORGANIZATION = "organization"; // /api/settings/organization
	public static String BRANDING = "branding";
	public static String LOGO = "logo"; // /api/settings/branding/logo
	public static String ICON = "icon"; // /api/settings/branding/icon
	public static String LABELS = "labels"; // /api/settings/labels
	public static String GENERAL = "general"; // /api/settings/general

	// Api endpoint for Lines of Credit. Available since 3.11
	public static String LINES_OF_CREDIT = "linesofcredit"; // /api/linesofcredit
	public static String ACCOUNTS = "accounts"; // /api/linesofcredit/{id}/accounts

	// Use Roles API
	public static String USER_ROLES = "userroles"; // api/userroles

	// Notification Messages Search API
	public static String NOTIFICATIONS = "notifications";
	public static String MESSAGES = "messages"; // /api/notifications/messages/search. Available since 3.14

	// Added to support Json object creation API
	public static String yyyyMmddFormat = DateUtils.DATE_FORMAT;// ISO_8601_FORMAT_DATE "yyyy-MM-dd";
	public static String JSON_OBJECT = "JSON";

	// Base64 encoded strings header's terminator in API responses
	public static String BASE64_ENCODING_INDICATOR = ";base64,";

}
