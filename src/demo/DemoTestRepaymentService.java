package demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accountsecurity.shared.model.InvestorFund;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.RepaymentsService;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProductType;
import com.mambu.loans.shared.model.Repayment;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestRepaymentService {

	private static String LOAN_ACCOUNT_ID; //

	private static String dueFromString = "2015-02-01";
	private static String dueToString = "2015-12-05";

	private static LoanAccount demoLoanAccount;
	// Remember retrieved repayments to be used by update API
	private static List<Repayment> repayments;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			final String testAccountId = null; // use specific account id or null to get random loan account
			demoLoanAccount = DemoUtil.getDemoLoanAccount(testAccountId);
			LOAN_ACCOUNT_ID = demoLoanAccount.getId();

			repayments = testGetLoanAccountRepayments();

			testUpdateLoanRepaymentsSchedule(); // Available since 3.9

			testDeleteRepayment(); // Available since 4.3

			testGetRepaymentsDueFromTo();

			testGetInvestorAccountRepayments(); // Available since 3.13

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Repayment Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static List<Repayment> testGetLoanAccountRepayments() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		String offset = "0";
		String limit = "100";

		List<Repayment> repayemnts = repaymentService.getLoanAccountRepayments(LOAN_ACCOUNT_ID, offset, limit);

		System.out.println("Total Repayments =" + repayemnts.size() + " Offset=" + offset + "  Limit=" + limit);
		if (repayemnts.size() > 0) {
			System.out.println("First Repayment  Due date=" + repayemnts.get(0).getDueDate().toString());
			System.out.println(
					"Last  Repayment  Due date=" + repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
		}
		return repayemnts;

	}

	public static void testGetRepaymentsDueFromTo() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		String offset = "0";
		String limit = "100";
		List<Repayment> repayemnts = repaymentService.getRapaymentsDueFromTo(dueFromString, dueToString, offset, limit);

		System.out.println("Total Repayments=" + repayemnts.size() + " Offset=" + offset + "  Limit=" + limit);
		if (repayemnts.size() > 0) {
			System.out.println("First Repayment Due date=" + repayemnts.get(0).getDueDate().toString());
			System.out.println(
					"Last  Repayment Due date=" + repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
		}
	}

	public static void testUpdateLoanRepaymentsSchedule() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		if (repayments == null) {
			System.out.println("No repayments to update");
			return;
		}

		System.out.println("Account " + LOAN_ACCOUNT_ID + " has " + repayments.size() + " repayments");

		List<Repayment> modifiedRepayments = new ArrayList<Repayment>();
		final long fiveDays = 5 * 24 * 60 * 60 * 1000L; // 5 days
		int minusOrPlusOne = -1; // indicator to increase or t decrease repayment amount
		final int maxRepaymentsToUpdate = 4; // Maximum number to update
		int i = 0;
		Date now = new Date();
		for (Repayment repayment : repayments) {
			// Fully paid repayments cannot be modified
			if (repayment.wasFullyPaid()) {
				continue;
			}

			// Modify some repayment fields
			// Add 5 days to due date
			Date dueDate = repayment.getDueDate();
			if (dueDate.before(now)) {
				continue;
			}
			repayment.setDueDate(new Date(dueDate.getTime() + fiveDays));
			// Modify amounts
			Money changeAmount = new Money(5.00);
			// Trying to keep overall balance unchanged. Subtracting from one and adding to the next one
			Money newAmount = (minusOrPlusOne == -1) ? repayment.getPrincipalDue().subtract(changeAmount)
					: repayment.getPrincipalDue().add(changeAmount);
			repayment.setPrincipalDue(newAmount);

			// Add modified repayment to the list
			modifiedRepayments.add(repayment);

			// Change amount sign: update some repayments with additions and some with subtraction
			minusOrPlusOne = -minusOrPlusOne;
			i++;
			if (i >= maxRepaymentsToUpdate) {
				break;
			}

		}

		// Since revolving credit loans can have predefined schedules with only due dates, they should allow adding new
		// installments via PATCH Schedule API. See MBU-13382.
		LoansService loansService = MambuAPIFactory.getLoanService();
		String productTypeKey = demoLoanAccount.getProductTypeKey();
		LoanProductType loanProductType = loansService.getLoanProduct(productTypeKey).getLoanProductType();

		if (loanProductType.equals(LoanProductType.REVOLVING_CREDIT)
				|| loanProductType.equals(LoanProductType.DYNAMIC_TERM_LOAN)) {
			Repayment repayment = new Repayment();
			repayment.setDueDate(new Date(now.getTime() + fiveDays));
			modifiedRepayments.add(repayment);
		}

		submitEditedRepaymentsToMambu(modifiedRepayments);

	}

	// Test getting repayments schedule for investor account.
	public static void testGetInvestorAccountRepayments() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		// Get schedule for investor in a demo loan account
		LoanAccount loanAccount = demoLoanAccount;
		// Get loan account id
		String loanId = loanAccount.getId();

		// Get current funds for this account to get savings account
		List<InvestorFund> funds = loanAccount.getFunds();
		if (funds == null || funds.size() == 0) {
			System.out.println("WARNING: Cannot test get repayment schedule: Loan Account " + loanId
					+ " Has no investor funds specified");
			return;
		}

		// Get savings account ID used for loan funding
		String savingsId = null;
		for (InvestorFund fund : funds) {
			savingsId = fund.getSavingsAccountKey();
			if (savingsId != null) {
				break;
			}
		}
		if (savingsId == null) {
			System.out.println("WARNING: Cannot test get repayment schedule: Loan Account " + loanId
					+ " Has no linked savings accounts specified");
			return;
		}
		System.out.println("\nGetting repayment schedule for savings id=" + savingsId + " loanId=" + loanId);
		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		List<Repayment> repayemnts = repaymentService.getInvestorFundingRepayments(savingsId, loanId);

		// Log results
		System.out.println("Total Repayments=" + repayemnts.size());
		if (repayemnts.size() > 0) {
			System.out.println("First Repayment  Due date=" + repayemnts.get(0).getDueDate().toString());
			System.out.println(
					"Last  Repayment  Due date=" + repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
		}

	}

	// Tests deleting a repayment for a loan account
	public static void testDeleteRepayment() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println(methodName = "\nIn " + methodName);

		// This check is here only for testing reasons
		// you may also update schedule on a different state
		if (!demoLoanAccount.getState().equals(AccountState.PENDING_APPROVAL)) {
			System.out.println("Invalid account state for " + methodName);
			return;
		}

		// get repayments for the loan account
		List<Repayment> repayments = testGetLoanAccountRepayments();
		if (repayments.size() >= 2) {

			// edits the schedule
			updateScheduleByFirstRepayment(repayments);

			List<Repayment> returnedRepayments = submitEditedRepaymentsToMambu(repayments);

			// delete first of the repayments
			RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
			Boolean deleted = repaymentService.deleteLoanRepayment(LOAN_ACCOUNT_ID,
					returnedRepayments.get(0).getEncodedKey());

			System.out.println("Repayment was successfully deleted = " + deleted);
		} else {
			System.out.println(methodName + "can`t be performed due e to repayments no.");
			System.out.println("There should be at least 2 repayments");
		}
	}

	/**
	 * Submits the list of updated repayments received as parameter to this method in order to update the schedule of a
	 * loan.
	 * 
	 * @param editedRepayments
	 *            a list of updated repayments to be sent to Mambu.
	 * 
	 * @return a list of updated repayments from Mambu
	 * 
	 * @throws MambuApiException
	 */
	private static List<Repayment> submitEditedRepaymentsToMambu(List<Repayment> editedRepayments)
			throws MambuApiException {

		// Submit Update schedule API request with these updated entries
		JSONLoanRepayments loanRepayments = new JSONLoanRepayments(editedRepayments);
		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		System.out.println("Updating Repayments schedule for Loan Account ID=" + LOAN_ACCOUNT_ID + "\tRepayments ="
				+ editedRepayments.size());

		List<Repayment> updatedRepayments = repaymentService.updateLoanRepaymentsSchedule(LOAN_ACCOUNT_ID,
				loanRepayments);

		int totalReturned = (updatedRepayments == null) ? 0 : updatedRepayments.size();
		System.out.println("Total Repayments returned after update=" + totalReturned);
		// Can also see detailed update log on a Dashboard in Mambu
		return updatedRepayments;
	}

	/**
	 * Edits the list of not paid yet repayments by setting the first one`s principal to be "0"
	 * 
	 * @param notPaidRepayments
	 *            the list of not paid repayments to be adjusted.
	 */
	private static void updateScheduleByFirstRepayment(List<Repayment> notPaidRepayments) {

		BigDecimal newInstallementsNo = new BigDecimal(notPaidRepayments.size() - 1);

		Money totalPrincipalAmountBalance = demoLoanAccount.getPrincipalAmount();

		// calculate the due for remaining repayments
		BigDecimal repaymentPrincipal = new BigDecimal(totalPrincipalAmountBalance.getAmount()
				.divide(newInstallementsNo, 2, RoundingMode.HALF_UP).doubleValue());

		Money principal = new Money(repaymentPrincipal.doubleValue());
		principal.setScale(2, RoundingMode.HALF_UP);

		// update repayments (all but first and last)
		for (int i = 1; i < notPaidRepayments.size() - 1; i++) {
			notPaidRepayments.get(i).setPrincipalDue(principal);
		}

		// set the first one to be = "0"
		notPaidRepayments.get(0).setPrincipalDue(new BigDecimal("0"));

		// calculate and adjust the value for last payment
		Money lastRepaymentPrincipal = new Money(
				(totalPrincipalAmountBalance.getAmount().subtract(principal.getAmount().multiply(newInstallementsNo))
						.add(principal.getAmount())).doubleValue());

		lastRepaymentPrincipal.setScale(2, RoundingMode.HALF_UP);
		notPaidRepayments.get(notPaidRepayments.size() - 1).setPrincipalDue(lastRepaymentPrincipal);
	}

}
