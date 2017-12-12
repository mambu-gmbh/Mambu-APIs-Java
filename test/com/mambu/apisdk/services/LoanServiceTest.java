
/**
 * 
 */
package com.mambu.apisdk.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanAccount.RepaymentPeriodUnit;

/**
 * @author ipenciuc
 * 
 */
public class LoanServiceTest extends MambuAPIServiceTest {

	private LoansService service;

	@Override
	public void setUp() throws MambuApiException {

		super.setUp();

		service = new LoansService(super.mambuApiService);
	}

	@Test
	public void createAccount() throws MambuApiException {

		LoanAccount account = new LoanAccount();
		account.setId(null);
		account.setAccountHolderKey("8ad661123b36cfaf013b42c2e0f46dca"); // CLIENT_ID
																			// "8ad661123b36cfaf013b42c2e0f46dca"
		account.setAccountHolderType(AccountHolderType.CLIENT);
		account.setProductTypeKey("8ad661123b36cfaf013b42cbcf2c6dd3");// "8ad661123b36cfaf013b42cbcf2c6dd3"
		account.setLoanAmount(new Money(7500.00));
		account.setInterestRate(new BigDecimal("3.2"));
		account.setRepaymentInstallments(20);
		// From Product
		account.setRepaymentPeriodUnit(RepaymentPeriodUnit.DAYS);
		account.setRepaymentPeriodCount(1);

		// ADd Custom Fields

		List<CustomFieldValue> clientCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		String customFieldId = "Loan_Purpose_Loan_Accounts";
		String customFieldValue = "My Loan Purpose 5";

		custField1.setCustomFieldId(customFieldId);
		custField1.setValue(customFieldValue);
		custField1.setCustomFieldSetGroupIndex(null); // Set to null explicitly: since Mambu 3.13 defaults to -1
		custField1.setSkipUniqueValidation(null); // Set to null explicitly: new in Mambu 4.1, defaults to false
		// Add new field to the list
		clientCustomInformation.add(custField1);
		// Field #2
		// Loan_Originator_Loan_Accounts
		CustomFieldValue custField2 = new CustomFieldValue();
		customFieldId = "Loan_Originator_Loan_Accounts";
		customFieldValue = "Trust";

		custField2.setCustomFieldId(customFieldId);
		custField2.setValue(customFieldValue);
		custField2.setCustomFieldSetGroupIndex(null); // Set to null explicitly: since Mambu 3.13 defaults to -1
		custField2.setSkipUniqueValidation(null); // Set to null explicitly: since Mambu 4.1 defaults to false
		// Add new field to the list
		clientCustomInformation.add(custField2);

		// Add All custom fields
		account.setCustomFieldValues(clientCustomInformation);

		// Create Account in Mambu
		service.createLoanAccount(account);

		ParamsMap params = new ParamsMap();
		params.addParam(
				"JSON",
				"{\"loanAccount\":"
						+ "{"
						+ "\"accountHolderKey\":\"8ad661123b36cfaf013b42c2e0f46dca\","
						+ "\"accountHolderType\":\"CLIENT\","
						+ "\"accountState\":\"PENDING_APPROVAL\","
						+ "\"productTypeKey\":\"8ad661123b36cfaf013b42cbcf2c6dd3\","
						+ "\"loanAmount\":7500,"
						+ "\"periodicPayment\":0,"
						+ "\"principalDue\":0,"
						+ "\"principalPaid\":0,"
						+ "\"principalBalance\":0,"
						+ "\"redrawBalance\":0,"
						+ "\"interestDue\":0,"
						+ "\"interestPaid\":0,"
						+ "\"interestFromArrearsBalance\":0,"
						+ "\"interestFromArrearsDue\":0,"
						+ "\"interestFromArrearsPaid\":0,"
						+ "\"interestBalance\":0,"
						+ "\"feesDue\":0,"
						+ "\"feesPaid\":0,"
						+ "\"feesBalance\":0,"
						+ "\"penaltyDue\":0,"
						+ "\"penaltyPaid\":0,"
						+ "\"penaltyBalance\":0,"
						+ "\"scheduleDueDatesMethod\":\"INTERVAL\","
						+ "\"prepaymentAcceptance\":\"ACCEPT_PREPAYMENTS\","
						+ "\"futurePaymentsAcceptance\":\"NO_FUTURE_PAYMENTS\","
						+ "\"hasCustomSchedule\":false,"
						+ "\"repaymentPeriodCount\":1,"
						+ "\"repaymentPeriodUnit\":\"DAYS\","
						+ "\"repaymentInstallments\":20,"
						+ "\"gracePeriod\":0,"
						+ "\"interestRate\":3.2,"
						+ "\"interestBalanceCalculationMethod\":\"PRINCIPAL_ONLY\","
						+ "\"accrueInterestAfterMaturity\":false," //Added in 4.3: defaults to false in the model
						+ "\"principalRepaymentInterval\":1,"
						+ "\"interestRateSource\":\"FIXED_INTEREST_RATE\","
						+ "\"accruedInterest\":0,"
						+ "\"interestFromArrearsAccrued\":0,"	
						+ "\"accruedPenalty\":0,\"loanPenaltyCalculationMethod\":\"NONE\","
						+ "\"arrearsTolerancePeriod\":0," // Added in 4.2: defaults to zero in the model
						+ "\"allowOffset\":false}," // Added in 4.5: defaults to false in the model
						+ "\"customInformation\":["
						+ "{\"value\":\"My Loan Purpose 5\",\"indexInList\":-1,\"toBeDeleted\":false,\"customFieldID\":\"Loan_Purpose_Loan_Accounts\"},"
						+ "{\"value\":\"Trust\",\"indexInList\":-1,\"toBeDeleted\":false,\"customFieldID\":\"Loan_Originator_Loan_Accounts\"}"
						+ "]" + "}");

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/loans", params, Method.POST,
				ContentType.JSON);
	}

	@Test
	public void rejectAccount() throws MambuApiException {

		// Create Account in Mambu
		service.rejectLoanAccount("8ad661123b36cfaf013b42c2e0f46dca", "The automated approval failed.");

		ParamsMap params = new ParamsMap();
		params.addParam("type", "REJECT");
		params.addParam("notes", "The automated approval failed.");

		// verify
		Mockito.verify(executor).executeRequest(
				"https://demo.mambutest.com/api/loans/8ad661123b36cfaf013b42c2e0f46dca/transactions", params,
				Method.POST, ContentType.WWW_FORM);
	}
}