package com.mambu.apisdk.model;

import java.util.List;

import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.HasCustomFields;
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
 * @author mdanilkis
 *
 */
public class LoanAccountExpanded implements HasCustomFields {
	private LoanAccount loanAccount;
    private List<CustomFieldValue> customInformation;

    public LoanAccountExpanded()
    {

    }

    public LoanAccountExpanded(LoanAccount loanAccount)
    {
        this.loanAccount = loanAccount;
        popCustomFieldValues();
    }

    public void popCustomFieldValues()
    {
        customInformation = loanAccount.getCustomFieldValues();
        loanAccount.setCustomFieldValues(null);
    }

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

    /**
     * @return the customInformation
     */
    @Override
    @ExcludeFromGson
    public List<CustomFieldValue> getCustomFieldValues()
    {
        return getCustomInformation();
    }

    /**
     * @param customInformation
     *            the customInformation to set
     */
    @Override
    @ExcludeFromGson
    public void setCustomFieldValues(List<CustomFieldValue> customInformation)
    {
        setCustomInformation(customInformation);
    }
}
