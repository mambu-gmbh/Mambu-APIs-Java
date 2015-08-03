package demo;

import java.util.HashSet;
import java.util.List;

import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;

/**
 * Helper demo test class to retrieve keys and ids for demo entities needed for testing
 * 
 * @author mdanilkis
 * 
 */

public class DemoEntityParams {

	private final static HashSet<MambuEntityType> demoEntities;
	static {
		demoEntities = new HashSet<MambuEntityType>();
		demoEntities.add(MambuEntityType.CLIENT);
		demoEntities.add(MambuEntityType.GROUP);
		demoEntities.add(MambuEntityType.LOAN_ACCOUNT);
		demoEntities.add(MambuEntityType.SAVINGS_ACCOUNT);
		demoEntities.add(MambuEntityType.LOAN_PRODUCT);
		demoEntities.add(MambuEntityType.SAVINGS_PRODUCT);
		demoEntities.add(MambuEntityType.USER);
		demoEntities.add(MambuEntityType.BRANCH);
		demoEntities.add(MambuEntityType.CENTRE);

	}

	private String name;
	private String encodedKey;
	private String id;

	DemoEntityParams(String name, String encodedKey, String id) {
		this.name = name;
		this.encodedKey = encodedKey;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getEncodedKey() {
		return encodedKey;
	}

	public String getId() {
		return id;
	}

	/**
	 * Retrieve demo entity and return DemoEntityParams for it.
	 * 
	 * @param mambuEntity
	 *            Mambu demo entity
	 * @return params for the demo entity, including it's name, key and id
	 */
	public static DemoEntityParams getEntityParams(MambuEntityType mambuEntity) throws MambuApiException {
		if (mambuEntity == null) {
			throw new IllegalArgumentException("Demo entity  cannot be null");
		}
		if (!demoEntities.contains(mambuEntity)) {
			throw new IllegalArgumentException("Demo entity " + mambuEntity + " is not supported");
		}

		// Get Demo entities using IDs specified in the configuration file (set requested ID to null)
		switch (mambuEntity) {
		case CLIENT:
			Client client = DemoUtil.getDemoClient(null);
			return new DemoEntityParams(client.getFullName(), client.getEncodedKey(), client.getId());
		case GROUP:
			Group group = DemoUtil.getDemoGroup(null);
			return new DemoEntityParams(group.getGroupName(), group.getEncodedKey(), group.getId());
		case LOAN_ACCOUNT:
			LoanAccount loan = DemoUtil.getDemoLoanAccount(null);
			return new DemoEntityParams(loan.getName(), loan.getEncodedKey(), loan.getId());
		case SAVINGS_ACCOUNT:
			SavingsAccount savings = DemoUtil.getDemoSavingsAccount(null);
			return new DemoEntityParams(savings.getName(), savings.getEncodedKey(), savings.getId());
		case LOAN_PRODUCT:
			LoanProduct lProduct = DemoUtil.getDemoLoanProduct(null);
			return new DemoEntityParams(lProduct.getName(), lProduct.getEncodedKey(), lProduct.getId());
		case SAVINGS_PRODUCT:
			SavingsProduct sProduct = DemoUtil.getDemoSavingsProduct(null);
			return new DemoEntityParams(sProduct.getName(), sProduct.getEncodedKey(), sProduct.getId());
		case USER:
			User user = DemoUtil.getDemoUser();
			return new DemoEntityParams(user.getFullName(), user.getEncodedKey(), user.getId());
		case BRANCH:
			Branch branch = DemoUtil.getDemoBranch();
			return new DemoEntityParams(branch.getName(), branch.getEncodedKey(), branch.getId());
		case CENTRE:
			Centre centre = DemoUtil.getDemoCentre();
			return new DemoEntityParams(centre.getName(), centre.getEncodedKey(), centre.getId());

		default:
			throw new IllegalArgumentException("Demo entity  " + mambuEntity
					+ " implementation is missing in DemoEntityParams");
		}

	}

	/**
	 * Retrieve custom field values for demo Mambu Entity
	 * 
	 * @param mambuEntity
	 *            Mambu demo entity
	 * @param entityParams
	 *            demo entity params
	 * @return custom field values
	 */
	public static List<CustomFieldValue> getCustomFieldValues(MambuEntityType mambuEntity, DemoEntityParams entityParams)
			throws MambuApiException {
		if (mambuEntity == null) {
			throw new IllegalArgumentException("Demo entity  cannot be null");
		}
		if (entityParams == null) {
			throw new IllegalArgumentException("Entity params cannot be null");
		}
		if (!demoEntities.contains(mambuEntity)) {
			throw new IllegalArgumentException("Demo entity " + mambuEntity + " is not supported");
		}

		String entityId = entityParams.getId();
		switch (mambuEntity) {
		case CLIENT:
			ClientExpanded client = DemoUtil.getDemoClientDetails(entityId);
			return client.getCustomFieldValues();
		case GROUP:
			GroupExpanded group = DemoUtil.getDemoGroupDetails(entityId);
			return group.getCustomFieldValues();
		case LOAN_ACCOUNT:
			LoanAccount loan = DemoUtil.getDemoLoanAccount(entityId);
			return loan.getCustomFieldValues();
		case SAVINGS_ACCOUNT:
			SavingsAccount savings = DemoUtil.getDemoSavingsAccount(entityId);
			return savings.getCustomFieldValues();
		case LOAN_PRODUCT:
		case SAVINGS_PRODUCT:
			throw new IllegalArgumentException("Custom Fields are not supported for " + mambuEntity);
		case USER:
			User user = DemoUtil.getDemoUser();
			return user.getCustomFieldValues();
		case BRANCH:
			Branch branch = DemoUtil.getDemoBranch();
			return branch.getCustomFieldValues();
		case CENTRE:
			Centre centre = DemoUtil.getDemoCentre();
			return centre.getCustomFieldValues();
		default:
			throw new IllegalArgumentException("Custom Filed Value for  " + mambuEntity + " implementation is missing");
		}

	}
}
