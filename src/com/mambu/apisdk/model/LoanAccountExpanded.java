package com.mambu.apisdk.model;

import java.util.List;

import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.loans.shared.model.LoanAccount;

/**
 * A class which expands the definition of the Mambu's LoanAccount to include custom fields in the format expected by
 * the Mambu's createLoanAccount() Json API. The primary purpose of this class to supply LoanAccount and custom
 * information in the format expected by this API and which can be automaticlaly parsed by Gson to/from json.
 * 
 * Example of the expected json format for creating LoanAccount (Mambu 3.2)
 * 
 * {"loanAccount":{.....}, "customInformation":[{field1},{field2}]}
 * 
 * The class provides just basic getters and setters loanAccount and customInformation. It's primary purpose is to
 * represent a Json object for LoanAccount creation API.
 * 
 * @deprecated This class is deprecated as of Mambu 4.0. The JSONLoanAccount class from the Mambu model library can be
 *             used internally instead when needed.
 * 
 * @author mdanilkis
 * 
 */
@Deprecated
public class LoanAccountExpanded {

	private LoanAccount loanAccount;
	private List<CustomFieldValue> customInformation;

	/**
	 * @param loanAccount
	 *            the loanAccount to set
	 */
	public void setLoanAccount(LoanAccount loanAccount) {
		this.loanAccount = loanAccount;
	}

	/**
	 * @return the loanAccount
	 */
	public LoanAccount getLoanAccount() {
		return loanAccount;
	}

	/**
	 * @param customInformation
	 *            the customInformation to set
	 */
	public void setCustomInformation(List<CustomFieldValue> customInformation) {
		this.customInformation = customInformation;
	}

	/**
	 * @return the customInformation
	 */
	public List<CustomFieldValue> getCustomInformation() {
		return customInformation;
	}

}
