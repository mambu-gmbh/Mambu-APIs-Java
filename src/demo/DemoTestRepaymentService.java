package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.RepaymentsService;
import com.mambu.loans.shared.model.Repayment;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestRepaymentService {

	private static String LOAN_ACCOUNT_ID = "DOCF446"; // DOCF446 - Tom Hanks; QFCY911 - Irina Chernaya;

	private static String dueFromString = "2013-1-01";
	private static String dueToString = "2013-12-05";

	public static void main(String[] args) {

		try {
			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");

			testGetLoanAccountRepayments();
			
			testGetRepaymentsDueFromTo();
			
		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Repayment Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetLoanAccountRepayments() throws MambuApiException {

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();
		
		List<Repayment> repayemnts = repaymentService.getLoanAccountRepayments(LOAN_ACCOUNT_ID);

		System.out.println("Total Repayments=" + repayemnts.size());
		System.out.println("First Repayments  Due date" + repayemnts.get(0).getDueDate().toString());
		System.out
				.println("Last  Repayment   Due date" + repayemnts.get(repayemnts.size() - 1).getDueDate().toString());

	}
	public static void testGetRepaymentsDueFromTo() throws MambuApiException {

		RepaymentsService repaymentService = MambuAPIFactory.getRepaymentsService();

		List<Repayment> repayemnts = repaymentService.getRapaymentsDueFromTo(dueFromString, dueToString);
		
		System.out.println("Total Repayments=" + repayemnts.size());
		System.out.println("First Repayment Due date" + repayemnts.get(0).getDueDate().toString());
		System.out.println("Last Repayment  Due date" + repayemnts.get(repayemnts.size() - 1).getDueDate().toString());
	}

}
