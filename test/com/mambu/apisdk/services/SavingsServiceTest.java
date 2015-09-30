/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.accounts.shared.model.AccountState;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsType;

/**
 * @author ipenciuc
 * 
 */
public class SavingsServiceTest extends MambuAPIServiceTest {

	private SavingsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new SavingsService(super.mambuApiService);
	}

	@Test
	public void createAccount() throws MambuApiException {

		SavingsAccount savingsAccount = new SavingsAccount();
		savingsAccount.setId(null);
		savingsAccount.setProductTypeKey("8a3615ef414e97d30141500808255d4d");
		savingsAccount.setAccountState(AccountState.PENDING_APPROVAL);
		savingsAccount.setAccountType(SavingsType.CURRENT_ACCOUNT);
		savingsAccount.setClientAccountHolderKey("8ad661123b36cfaf013b42c2e0f46dca");

		// Add Custom Fields
		List<CustomFieldValue> savingsAccountCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		custField1.setCustomFieldId("Interest_Deposit_Accounts");
		custField1.setValue("My Loan Purpose 5");
		custField1.setCustomFieldSetGroupIndex(null); // Set to null explicitly: since Mambu 3.13 defaults to -1
		savingsAccountCustomInformation.add(custField1);

		CustomFieldValue custField2 = new CustomFieldValue();
		custField2.setCustomFieldId("Deposit_frequency_Deposit_Accoun");
		custField2.setValue("Daily");
		custField2.setCustomFieldSetGroupIndex(null); // Set to null explicitly: since Mambu 3.13 defaults to -1
		savingsAccountCustomInformation.add(custField2);

		JSONSavingsAccount jsonSavingsAccount = new JSONSavingsAccount(savingsAccount);
		jsonSavingsAccount.setCustomInformation(savingsAccountCustomInformation);

		// Create Account in Mambu
		service.createSavingsAccount(jsonSavingsAccount);

		ParamsMap params = new ParamsMap();
		params.addParam(
				"JSON",
				"{"
						+ "\"savingsAccount\":"
						+ "{\"accountHolderKey\":\"8ad661123b36cfaf013b42c2e0f46dca\","
						+ "\"accountHolderType\":\"CLIENT\","
						+ "\"productTypeKey\":\"8a3615ef414e97d30141500808255d4d\","
						+ "\"accountType\":\"CURRENT_ACCOUNT\","
						+ "\"accountState\":\"PENDING_APPROVAL\","
						+ "\"balance\":0,\"accruedInterest\":0,"
						+ "\"overdraftInterestAccrued\":0,"
						+ "\"overdraftAmount\":0,"
						+ "\"interestDue\":0,"
						+ "\"feesDue\":0,"
						+ "\"overdraftLimit\":0,"
						+ "\"allowOverdraft\":false,"
						+ "\"lockedBalance\":0},"
						+ "\"customInformation\":"
						+ "["
						+ "{\"value\":\"My Loan Purpose 5\",\"indexInList\":-1,\"toBeDeleted\":false,\"customFieldID\":\"Interest_Deposit_Accounts\"},"
						+ "{\"value\":\"Daily\",\"indexInList\":-1,\"toBeDeleted\":false,\"customFieldID\":\"Deposit_frequency_Deposit_Accoun\"}"
						+ "]" + "}");

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/savings", params, Method.POST,
				ContentType.JSON);
	}
}
