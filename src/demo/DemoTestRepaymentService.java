package demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.RepaymentsService;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.LoanAccount;
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
	private static List<Repayment> repayemnts;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			final String testAccountId = null; // use specific account id or null to get random loan account
			demoLoanAccount = DemoUtil.getDemoLoanAccount(testAccountId);
			LOAN_ACCOUNT_ID = demoLoanAccount.getId();

			testGetLoanAccountRepayments();

			repayemnts = testGetLoanAccountRepaymentsWithLimit();

			testUpdateLoanRepaymentsSchedule(); // Available since 3.9

			testGetRepaymentsDueFromTo();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Repayment Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetLoanAccountRepayments() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountRepayments");

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();

		List<Repayment> repayemnts = repaymentService.getLoanAccountRepayments(LOAN_ACCOUNT_ID);

		System.out.println("Total Repayments=" + repayemnts.size());
		if (repayemnts.size() > 0) {
			System.out.println("First Repayment  Due date=" + repayemnts.get(0).getDueDate().toString());
			System.out.println("Last  Repayment  Due date="
					+ repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
		}

	}

	public static List<Repayment> testGetLoanAccountRepaymentsWithLimit() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountRepaymentsWithLimit");

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		String offset = "0";
		String limit = "100";

		List<Repayment> repayemnts = repaymentService.getLoanAccountRepayments(LOAN_ACCOUNT_ID, offset, limit);

		System.out.println("Total Repayments =" + repayemnts.size() + " Offset=" + offset + "  Limit=" + limit);
		if (repayemnts.size() > 0) {
			System.out.println("First Repayment  Due date=" + repayemnts.get(0).getDueDate().toString());
			System.out.println("Last  Repayment  Due date="
					+ repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
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
			System.out.println("Last  Repayment Due date="
					+ repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
		}
	}

	public static void testUpdateLoanRepaymentsSchedule() throws MambuApiException {
		System.out.println("\nIn testUpdateLoanRepaymentsSchedule");

		if (repayemnts == null) {
			System.out.println("No repayments to update");
			return;
		}

		System.out.println("Account " + LOAN_ACCOUNT_ID + " has " + repayemnts.size() + " repayments");

		List<Repayment> modifiedRepayments = new ArrayList<Repayment>();
		final long fiveDays = 5 * 24 * 60 * 60 * 1000; // 5 days
		int minusOrPlusOne = -1; // indicator to increase or t decrease repayment amount
		final int maxRepaymentsToUpdate = 4; // Maximum number to update
		int i = 0;
		for (Repayment repayment : repayemnts) {
			// Fully paid repayments cannot be modified
			if (repayment.wasFullyPaid()) {
				continue;
			}
			// Modify some repayment fields
			// Add 5 days to due date
			Date dueDate = repayment.getDueDate();
			repayment.setDueDate(new Date(dueDate.getTime() + fiveDays));
			// Modify amounts
			Money changeAmount = new Money(5.00);
			// Trying to keep overall balance unchanged. Subtracting from one and adding to the next one
			Money newAmount = (minusOrPlusOne == -1) ? repayment.getPrincipalDue().subtract(changeAmount) : repayment
					.getPrincipalDue().add(changeAmount);
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
}
