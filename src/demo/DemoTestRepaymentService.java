package demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

			testGetRepaymentsDueFromTo();

			testGetInvestorAccountRepayments(); // Available since 3.13

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Repayment Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static List<Repayment> testGetLoanAccountRepayments() throws MambuApiException {

		System.out.println("\nIn testGetLoanAccountRepayments");

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

		System.out.println("\nIn testGetRepaymentsDueFromTo");

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

		System.out.println("\nIn testUpdateLoanRepaymentsSchedule");

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

		if (loanProductType.equals(LoanProductType.REVOLVING_CREDIT)) {
			Repayment repayment = new Repayment();
			repayment.setDueDate(new Date(now.getTime() + fiveDays));
			modifiedRepayments.add(repayment);
		}

		// Submit Update schedule API request with these updated entries
		JSONLoanRepayments loanRepayments = new JSONLoanRepayments(modifiedRepayments);
		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		System.out.println("Updating Repayments schedule for Loan Account ID=" + LOAN_ACCOUNT_ID + "\tRepayments ="
				+ modifiedRepayments.size());

		List<Repayment> updatedRepayments = repaymentService.updateLoanRepaymentsSchedule(LOAN_ACCOUNT_ID,
				loanRepayments);

		int totalReturned = (updatedRepayments == null) ? 0 : updatedRepayments.size();
		System.out.println("Total Repayments returned after update=" + totalReturned);
		// Can also see detailed update log on a Dashboard in Mambu

	}

	// Test getting repayments schedule for investor account.
	public static void testGetInvestorAccountRepayments() throws MambuApiException {

		System.out.println("\nIn testGetInvestorAccountRepayments");

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
}
