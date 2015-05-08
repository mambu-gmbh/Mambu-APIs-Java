package com.mambu.apisdk.util;

import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.Comment;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.User;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;

/**
 * MambuEntity class is an enum for Mambu entities supported by the API wrapper library. This class is used to specify
 * Mambu entity for an API operation
 * 
 * @author mdanilkis
 * 
 */
public enum MambuEntity {

	// A list of entities supported by the API wrapper library. To be extended as needed
	CLIENT(Client.class), GROUP(Group.class), LOAN_ACCOUNT(LoanAccount.class), SAVINGS_ACCOUNT(SavingsAccount.class), LOAN_PRODUCT(
			LoanProduct.class), SAVINGS_PRODUCT(SavingsProduct.class), BRANCH(Branch.class), CENTRE(Centre.class), USER(
			User.class), COMMENT(Comment.class), CUSTOM_FIELD_VALUE(CustomFieldValue.class), LINE_OF_CREDIT(
			LineOfCredit.class);

	// Map MambuEntity enum to a corresponding Java class in Mambu model. For example, CLIENT enum is for Client.class.
	// Java classes for MambuEntity are to be used by the {@link ApiDefintion} and {@link ServiceExecutor}
	private MambuEntity(Class<?> clazz) {
		this.entityClass = clazz;
	}

	// Mambu class for the entity
	private Class<?> entityClass;

	// Get Mambu Class
	public Class<?> getEntityClass() {
		return entityClass;
	}
}
