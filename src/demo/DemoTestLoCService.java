package demo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.LinesOfCreditService;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.linesofcredit.shared.model.AccountsFromLineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * Test class to show example usage for Line Of Credit (LoC) API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestLoCService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// tests creating LoC for a client
			testCreateLineOfCreditForAnAccountHolder(AccountHolderType.CLIENT, false); // Available since 4.2
			// tests creating LoC for a group
			testCreateLineOfCreditForAnAccountHolder(AccountHolderType.GROUP, false); // Available since 4.2
			testGetLinesOfCredit(); // Available since 3.11

			testGetCustomerLinesOfCredit(); // Available since 3.11

			// test get accounts for LoC
			AccountsFromLineOfCredit locAccounts = testGetAccountsForLineOfCredit();// Available since 3.11

			// test add and remove accounts from LoC.
			testAddAndRemoveAccountsForLineOfCredit(locAccounts);// Available since 3.12.2
			
			testPatchLineOfCredit(); // Available since v4.3
			
			testGetDetailsForLineOfCredit(); // Available since 4.5
			
			testGetAllLinesOfCreditWithDetails(); //Available since 4.5
			
			testGetClientLinesOfCreditWithDetails(); //Available since 4.5
			
			testGetGroupLinesOfCreditWithDetails(); //Available since 4.5
			
			testCreateLineOfCreditForAnAccountHolder(AccountHolderType.CLIENT, true); //Available since 4.5

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Lines of Credit Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}


	private static void testGetGroupLinesOfCreditWithDetails() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();

		String groupId = DemoUtil.demoGroupId;
		List<LineOfCredit> fetchedLinesOfCredit = null;

		if (null == groupId) {
			System.out.println("WARNING: " + methodName
					+ "no group ID is supplied in the properties file. This test can not be executed");
		} else {
			fetchedLinesOfCredit = linesOfCreditService.getGroupLinesOfCreditDetails(groupId, 0, 5);
		}

		if (CollectionUtils.isEmpty(fetchedLinesOfCredit)) {
			System.out.println("WARNING: there were no lines of credit in Mambu in order to be fetched");
			return;
		}

		logLinesOfCreditAndDetails(fetchedLinesOfCredit);

	}

	private static void testGetClientLinesOfCreditWithDetails() throws MambuApiException {
		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		
		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		
		String clientId = DemoUtil.demoClientId;
		List <LineOfCredit> fetchedLinesOfCredit = null;
		
		if(null == clientId){
			System.out.println("WARNING: " + methodName + "no client ID is supplied in the properties file. This test can not be executed");
		}else{
		  fetchedLinesOfCredit = linesOfCreditService.getClientLinesOfCreditDetails(clientId, 0, 5);
		}
		
		if(CollectionUtils.isEmpty(fetchedLinesOfCredit)){
			System.out.println("WARNING: there were no lines of credit in Mambu in order to be fetched");
			return;
		}
		
		logLinesOfCreditAndDetails(fetchedLinesOfCredit);
		
	}

	private static void testGetAllLinesOfCreditWithDetails() throws MambuApiException {
		
		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		
		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		
		List <LineOfCredit> fetchedLinesOfCredit = linesOfCreditService.getAllLinesOfCreditWithDetails(0, 100);

		if(CollectionUtils.isEmpty(fetchedLinesOfCredit)){
			System.out.println("WARNING: there were no lines of credit in Mambu in order to be fetched");
			return;
		}
		
		logLinesOfCreditAndDetails(fetchedLinesOfCredit);
	}

	/**
	 * Tests creating a line of credit for the account holder passed as parameter to this method. Currently it supports
	 * the GROUP and CLIENT as AccountHolderType.
	 * 
	 * @param accountHolderType
	 *            account holder type. Must not be null
	 * @param shouldHaveCustomFields
	 *            indicates whether the line of credit should be created with or without custom field values
	 * @throws MambuApiException
	 */
	private static void testCreateLineOfCreditForAnAccountHolder(AccountHolderType accountHolderType,
			boolean shouldHaveCustomFields) throws MambuApiException {

		System.out.println("\nIn testCreateLineOfCreditForAGroup");

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();

		LineOfCredit lineOfCredit = createLineOfCreditObjectForPost();

		// Set owner's key: setClientKey for a Client and setGroupKey for a Group
		switch (accountHolderType) {
		case CLIENT:
			lineOfCredit.setClientKey(DemoUtil.getDemoClient(null).getClientKey());
			break;
		case GROUP:
			lineOfCredit.setGroupKey(DemoUtil.getDemoGroup(null).getGroupKey());
			break;
		}

		if (shouldHaveCustomFields) {
			lineOfCredit.setCustomFieldValues(getCustomFieldsValuesFromDemoLoc());
		}

		// POST the LoC in Mambu
		System.out.println("Creating LoC with Start Date=" + lineOfCredit.getStartDate() + "\tExpiry Date="
				+ lineOfCredit.getExpireDate());
		LineOfCredit postedLineOfCredit = linesOfCreditService.createLineOfCredit(lineOfCredit);
		// log the details to the console
		logLineOfCreditDetails(postedLineOfCredit);
		System.out.println("LineOfCredit created today: " + new Date());

	}

	/**
	 * Helper method, gets the custom field values from an existing Line of credit
	 * 
	 * @return a list of custom field values
	 * @throws MambuApiException
	 */
	private static List<CustomFieldValue> getCustomFieldsValuesFromDemoLoc() throws MambuApiException {

		return DemoUtil.getDemoLineOfCredit(DemoUtil.demoLineOfCreditId).getCustomFieldValues();
	}


	/**
	 * Helper method, creates a new line of credit object, set some test data on it.
	 * 
	 * @return a newly line of credit test object
	 */
	private static LineOfCredit createLineOfCreditObjectForPost() {

		LineOfCredit lineOfCredit = new LineOfCredit();

		Calendar now = Calendar.getInstance();

		String notes = "Line of credit note created via API " + now.getTime();
		lineOfCredit.setId("LOC" + now.getTimeInMillis());
		lineOfCredit.setStartDate(now.getTime());
		now.add(Calendar.MONTH, 6);
		lineOfCredit.setExpiryDate(now.getTime());
		lineOfCredit.setNotes(notes);
		lineOfCredit.setAmount(new Money(100000));

		return lineOfCredit;
	}
	
	
	/**
	 * Helper method, prints to the console details for a List of Lines of credit received as parameter to this method.
	 * 
	 * @param linesOfCredit
	 *            A list containing lines of credit whose details will be printed to the console.
	 */
	public static void logLinesOfCreditAndDetails(List<LineOfCredit> linesOfCredit){
		
		if(CollectionUtils.isEmpty(linesOfCredit)){
			System.out.println("WARNING: No lines of credit was povided in order to log its details");
		}else{
			for(LineOfCredit loc: linesOfCredit){
				logLineOfCreditDetails(loc);
			}
		}
	}

	/**
	 * Helper method, prints to the console details of the LineOfCredit received as parameter to this method.
	 * 
	 * @param lineOfCredit
	 *            The line of credit whose details will be printed to the console
	 */
	private static void logLineOfCreditDetails(LineOfCredit lineOfCredit) {

		if (lineOfCredit != null) {
			System.out.println("Line of credit details:");
			System.out.println("\tID:" + lineOfCredit.getId());
			System.out.println("\tClientKey:" + lineOfCredit.getClientKey());
			System.out.println("\tGroupKey:" + lineOfCredit.getGroupKey());
			System.out.println("\tStartDate:" + lineOfCredit.getStartDate());
			System.out.println("\tExpireDate:" + lineOfCredit.getExpireDate());
			System.out.println("\tAmount:" + lineOfCredit.getAmount());
			System.out.println("\tState:" + lineOfCredit.getState());
			System.out.println("\tCreationDate:" + lineOfCredit.getCreationDate());
			System.out.println("\tLastModifiedDate:" + lineOfCredit.getLastModifiedDate());
			System.out.println("\tNotes:" + lineOfCredit.getNotes());
		
			// log the CFs details
			logCustomFieldValuesDetails(lineOfCredit.getCustomFieldValues());
		}
	}

	private static void logCustomFieldValuesDetails(List<CustomFieldValue> customFieldValues) {
		
		if(CollectionUtils.isNotEmpty(customFieldValues)){
			System.out.println("\tLine of credit details of custom field values:");
			for(CustomFieldValue value : customFieldValues){
				System.out.println("\t\tID: " + value.getCustomFieldId() );
				System.out.println("\t\tCustom field key: " + value.getCustomFieldKey() );
				System.out.println("\t\tEncoded key: " + value.getEncodedKey() );
				System.out.println("\t\tValue: " + value.getValue());
				System.out.println("\t\tParent key: " + value.getParentKey() );
				System.out.println("\t\tIndex in list: " + value.getIndexInList() );
				System.out.println("\t\tAmount: " + value.getAmount());
				System.out.println("\t\tLinked entity key: " + value.getLinkedEntityKeyValue() );
				System.out.println("\t\tCustom field grouped index: " + value.getCustomFieldSetGroupIndex() );
				System.out.println("\t\tEntity name:" + value.getEntityName());
			}
		}
		
	}

	/**
	 * Test Get paginated list of all lines of credit and LoC details
	 * 
	 * @throws MambuApiException
	 */
	public static void testGetLinesOfCredit() throws MambuApiException {

		System.out.println("\nIn testGetLinesOfCredit");

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		Integer offset = 0;
		Integer limit = 5;

		// Test getting all lines of credit
		List<LineOfCredit> linesOfCredit = linesOfCreditService.getAllLinesOfCredit(offset, limit);
		System.out.println("Total Lines of Credit=" + linesOfCredit.size());
		if (linesOfCredit.size() == 0) {
			System.out.println("*** No Lines of Credit to test ***");
			return;
		}
		for (LineOfCredit loc : linesOfCredit) {
			System.out.println("\tID=" + loc.getId() + "\tAmount=" + loc.getAmount() + "\tAvailable Credit Amount="
					+ loc.getAvailableCreditAmount());
		}
		// Test get Line Of Credit details
		String lineofcreditId = linesOfCredit.get(0).getId();
		System.out.println("Getting details for Line of Credit ID=" + lineofcreditId);
		LineOfCredit lineOfCredit = linesOfCreditService.getLineOfCredit(lineofcreditId);
		// Log returned LoC
		System.out.println("Line of Credit. ID=" + lineOfCredit.getId() + "\tAmount=" + lineOfCredit.getAmount()
				+ "\tOwnerType=" + lineOfCredit.getOwnerType() + "\tHolderKey="
				+ lineOfCredit.getAccountHolder().getAccountHolderKey());
		
	}
	
	/**
	 * Tests GETting the lines of credit with details, including custom fields 
	 * @throws MambuApiException 
	 * 
	 */
	public static void testGetDetailsForLineOfCredit() throws MambuApiException {
		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		
		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		Integer offset = 0;
		Integer limit = 100;
		
		List<LineOfCredit> linesOfCredit = linesOfCreditService.getAllLinesOfCredit(offset, limit);
		
		if(CollectionUtils.isNotEmpty(linesOfCredit)){

			/* Get all details for first line of credit found */
			LineOfCredit firstLineOfCredit = linesOfCredit.get(0);
			
			System.out.println("Getting all details for Line of Credit ID= " + firstLineOfCredit.getEncodedKey());
			
			LineOfCredit lineOfCreditDetails = linesOfCreditService.getLineOfCreditDetails(firstLineOfCredit.getEncodedKey());
			
			// Log returned LoC details
			logLineOfCreditDetails(lineOfCreditDetails);
		}else{
			System.out.println("WARNING: No Credit lines were found in order to run test " + methodName);
		}
		
	}

	/**
	 * Test Get lines of credit for a Client and Group
	 * 
	 */
	public static void testGetCustomerLinesOfCredit() throws MambuApiException {

		System.out.println("\nIn testGetCustomerLinesOfCredit");

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		Integer offset = 0;
		Integer limit = 30;
		// Test Get line of credit for a Client
		// Get Demo Client ID first

		final String clientId = DemoUtil.getDemoClient().getId();
		// Get Lines of Credit for a client
		List<LineOfCredit> clientLoCs = linesOfCreditService.getClientLinesOfCredit(clientId, offset, limit);
		System.out.println(clientLoCs.size() + " lines of credit  for Client " + clientId);

		// Test Get line of credit for a Group
		// Get Demo Group ID first
		final String groupId = DemoUtil.getDemoGroup().getId();
		// Get Lines of Credit for a group
		List<LineOfCredit> groupLoCs = linesOfCreditService.getGroupLinesOfCredit(groupId, offset, limit);
		System.out.println(groupLoCs.size() + " lines of credit for Group " + groupId);

	}

	/**
	 * Test Get Accounts for a line of Credit
	 * 
	 * @return accounts for a line of credit
	 * @throws MambuApiException
	 */
	public static AccountsFromLineOfCredit testGetAccountsForLineOfCredit() throws MambuApiException {

		System.out.println("\nIn testGetAccountsForLineOfCredit");
		// Test Get Accounts for a line of credit

		String lineOfCreditId = DemoUtil.demoLineOfCreditId;
		if (lineOfCreditId == null) {
			System.out.println("WARNING: No Demo Line of credit defined");
			return null;
		}

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();

		System.out.println("\nGetting all accounts for LoC with ID= " + lineOfCreditId);
		AccountsFromLineOfCredit accountsForLoC = linesOfCreditService.getAccountsForLineOfCredit(lineOfCreditId);
		// Log returned results
		List<LoanAccount> loanAccounts = accountsForLoC.getLoanAccounts();
		List<SavingsAccount> savingsAccounts = accountsForLoC.getSavingsAccounts();
		System.out.println("Total Loan Accounts=" + loanAccounts.size() + "\tTotal Savings Accounts="
				+ savingsAccounts.size() + " for LoC=" + lineOfCreditId);

		return accountsForLoC;
	}

	// Test deleting and adding accounts to a Line of Credit
	public static void testAddAndRemoveAccountsForLineOfCredit(AccountsFromLineOfCredit accountsForLoC)
			throws MambuApiException {

		System.out.println("\nIn testAddAndRemoveAccountsForLineOfCredit");

		String lineOfCreditId = DemoUtil.demoLineOfCreditId;
		if (lineOfCreditId == null) {
			System.out.println("WARNING: No Demo Line of credit defined");
			return;
		}

		// Test remove and add for Loan Accounts
		List<LoanAccount> loanAccounts = accountsForLoC.getLoanAccounts();
		testdeleteAndAddLoanAccounts(lineOfCreditId, loanAccounts);

		// Test remove and add for Savings Accounts
		List<SavingsAccount> savingsAccounts = accountsForLoC.getSavingsAccounts();
		testdeleteAndAddSavingsAccounts(lineOfCreditId, savingsAccounts);
	}

	// For each Loan account associated with a credit line test deleting and adding it back
	private static void testdeleteAndAddLoanAccounts(String lineOfCreditId, List<LoanAccount> accounts)
			throws MambuApiException {

		System.out.println("\nIn testdeleteAndAddLoanAccounts for LoC=" + lineOfCreditId);

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		String accountId = null;
		if (accounts == null || accounts.size() == 0) {
			System.out.println("WARNING: no Loan Account to remove for LoC=" + lineOfCreditId);
		} else {
			// We have assigned accounts. Test remove and then test adding it back
			// Test removing Loan Account. Accounts requiring LoC cannot be removed, so try until can
			for (LoanAccount account : accounts) {
				accountId = account.getId();
				try {
					boolean deleted = linesOfCreditService.deleteLoanAccount(lineOfCreditId, accountId);
					System.out.println("Removed Status=" + deleted + "\tAccount with ID=" + accountId
							+ " deleted from LoC=" + lineOfCreditId);
					// Deleted OK, now add the same back
					LoanAccount addedLoan = linesOfCreditService.addLoanAccount(lineOfCreditId, accountId);
					System.out.println("Added Loan Account with ID=" + addedLoan.getId() + " to LoC=" + lineOfCreditId);
				} catch (MambuApiException e) {
					System.out.println("Failed to remove account " + accountId + "\tMessage=" + e.getErrorMessage());
				}

			}

		}
	}

	// For each Savings account associated with a credit line test deleting and adding it back
	private static void testdeleteAndAddSavingsAccounts(String lineOfCreditId, List<SavingsAccount> accounts)
			throws MambuApiException {

		System.out.println("\nIn testdeleteAndAddSavingsAccounts for LoC=" + lineOfCreditId);

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		String accountId = null;
		if (accounts == null || accounts.size() == 0) {
			System.out.println("WARNING: no Savings Account to remove for LoC=" + lineOfCreditId);
		} else {
			// We have assigned accounts. Test remove and then test adding it back
			// Test removing Savings Account. Accounts requiring LoC cannot be removed, so try until can
			for (SavingsAccount account : accounts) {
				accountId = account.getId();
				try {
					boolean deleted = linesOfCreditService.deleteSavingsAccount(lineOfCreditId, accountId);
					System.out.println("Removed Status=" + deleted + "\tAccount with ID=" + accountId
							+ " deleted from LoC=" + lineOfCreditId);
					;
					// Deleted OK, now add the same back
					SavingsAccount addedAccount = linesOfCreditService.addSavingsAccount(lineOfCreditId, accountId);
					System.out.println("Added Savings Account with ID=" + addedAccount.getId() + " to LoC="
							+ lineOfCreditId);
				} catch (MambuApiException e) {
					System.out.println("Failed to remove account " + accountId + "\tMessage=" + e.getErrorMessage());
				}

			}

		}
	}
	
	// tests PATCHing a line of credit
	public static void testPatchLineOfCredit() throws MambuApiException{
		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		
		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();

		final String clientId = DemoUtil.getDemoClient().getId();
		// Get Lines of Credit for a client
		List<LineOfCredit> clientLoCs = linesOfCreditService.getClientLinesOfCredit(clientId, 0, 5);
		System.out.println(clientLoCs.size() + " lines of credit  for Client " + clientId);
		
		if(clientLoCs.isEmpty() || clientLoCs.get(0) == null){
			System.out.println("WARNING: " + methodName + " could not be tested because there are no LoCs to be patched");
		}else{
			LineOfCredit lineOfCredit = clientLoCs.get(0);
			
			//change some values on the allowed patch fields
			Calendar patchDate = Calendar.getInstance();
			
			lineOfCredit.setId("LOC" + System.currentTimeMillis());
			patchDate.add(Calendar.DAY_OF_MONTH, 1);
			lineOfCredit.setStartDate(patchDate.getTime());
			
			patchDate.add(Calendar.YEAR, 2);
			lineOfCredit.setExpiryDate(patchDate.getTime());
			
			lineOfCredit.setAmount(lineOfCredit.getAmount().add(new Money("5000")));
			lineOfCredit.setNotes("Note updated through APIs today " + new Date());

			boolean patchResult = linesOfCreditService.patchLinesOfCredit(lineOfCredit);
			System.out.println("PATCH LoC result is = "  + patchResult);
			
			// retrieve PATCHed line of credit
			lineOfCredit = linesOfCreditService.getLineOfCredit(lineOfCredit.getEncodedKey());
			
			// log details of the PATCHed line of credit
			logLineOfCreditDetails(lineOfCredit);
		}
		
	}
}
