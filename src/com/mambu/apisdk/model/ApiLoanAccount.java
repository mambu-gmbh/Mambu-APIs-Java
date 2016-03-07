package com.mambu.apisdk.model;

import java.util.List;

import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * A class which extends the definition of the Mambu's LoanAccount to include any additional fields that can be sent by
 * Mambu API within the LoanAccount but which are not part of the LoanAccount class.
 * 
 * As of Mambu 4.0 this is needed only for the API to GET Settlement Accounts for a Loan Account. When getting
 * LoanAccount with full details Mambu response may now include also settlement accounts for the loan account (if the
 * loan has a settlement account)
 * 
 * See MBU-11206- As a Developer, I want to have the settlement account returned when getting a loan via API with full
 * details. Here is an example of the response json message when getting LoanAccount with full detail and with
 * settlement accounts optionally included too (Mambu 4.0)
 * 
 * {"encodedKey":"fdsafasdfsadfasdfsda", ... "settlementAccounts":[{"encodedKey":"43927490231479231704"...}] }
 * 
 * The class provides just the basic getters and setters for the fields added to the ApiLoanAccount (e.g.
 * settlementAccounts). It's primary purpose is to represent a Json object returned for a LoanAccount with additional
 * details.
 * 
 * @author mdanilkis
 * 
 */
public class ApiLoanAccount extends LoanAccount {

	private static final long serialVersionUID = 1L;

	private List<SavingsAccount> settlementAccounts;

	/**
	 * Set Settlement Accounts
	 * 
	 * @param settlementAccounts
	 *            a list of settlement accounts for Loan Account
	 */
	public void setSettlemetAccounts(List<SavingsAccount> settlementAccounts) {
		this.settlementAccounts = settlementAccounts;
	}

	/**
	 * Get Settlement Accounts
	 * 
	 * @return savings settlement accounts
	 */
	public List<SavingsAccount> getSettlementAccounts() {
		return settlementAccounts;
	}

}
