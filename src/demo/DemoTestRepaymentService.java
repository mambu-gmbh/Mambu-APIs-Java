package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.RepaymentsService;
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

	private static String dueFromString = "2014-02-01";
	private static String dueToString = "2014-07-05";

	private static LoanAccount demoLoanAccount;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoLoanAccount = DemoUtil.getDemoLoanAccount();
			LOAN_ACCOUNT_ID = demoLoanAccount.getId();

			testGetLoanAccountRepayments();

			testGetLoanAccountRepaymentsWithLimit();

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

	public static void testGetLoanAccountRepaymentsWithLimit() throws MambuApiException {
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
}
