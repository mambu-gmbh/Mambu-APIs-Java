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
import com.mambu.linesofcredit.shared.model.LineOfCredit;
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
		demoEntities.add(MambuEntityType.LINE_OF_CREDIT);
		

	}

	private String name;
	private String encodedKey;
	private String id;
	private String linkedTypeKey; // e.g. product key or client type or channel key

	/**
	 * Constructor specifying entity name, encoded key and id
	 * 
	 * @param name
	 *            entity name
	 * @param encodedKey
	 *            entity encoded key
	 * @param id
	 *            entity id
	 */
	DemoEntityParams(String name, String encodedKey, String id) {
		this.name = name;
		this.encodedKey = encodedKey;
		this.id = id;
		this.linkedTypeKey = null;
	}

	/**
	 * Constructor specifying entity name, encoded key, id and linked entity key.
	 * 
	 * @param name
	 *            entity name
	 * @param encodedKey
	 *            entity encoded key
	 * @param id
	 *            entity id
	 * @param linkedTypeKey
	 *            . linked type key. For example, for clients this is a client role key, for loans and savings this is a
	 *            product type key, and for transactions this is a transaction channel key
	 */
	DemoEntityParams(String name, String encodedKey, String id, String linkedTypeKey) {
		this.name = name;
		this.encodedKey = encodedKey;
		this.id = id;
		this.linkedTypeKey = linkedTypeKey;
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

	public String getLinkedTypeKey() {
		return linkedTypeKey;
	}

	public void setLinkedTypeKey(String linkedTypeKey) {
		this.linkedTypeKey = linkedTypeKey;
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
			Client client = DemoUtil.getDemoClient(DemoUtil.demoClientId);
			return new DemoEntityParams(client.getFullName(), client.getEncodedKey(), client.getId(), client
					.getClientRole().getEncodedKey());
		case GROUP:
			Group group = DemoUtil.getDemoGroup(DemoUtil.demoGroupId);
			return new DemoEntityParams(group.getGroupName(), group.getEncodedKey(), group.getId(), group
					.getClientRole().getEncodedKey());
		case LOAN_ACCOUNT:
			LoanAccount loan = DemoUtil.getDemoLoanAccount(DemoUtil.demoLaonAccountId);
			return new DemoEntityParams(loan.getName(), loan.getEncodedKey(), loan.getId(), loan.getProductTypeKey());
		case SAVINGS_ACCOUNT:
			SavingsAccount savings = DemoUtil.getDemoSavingsAccount(DemoUtil.demoSavingsAccountId);
			return new DemoEntityParams(savings.getName(), savings.getEncodedKey(), savings.getId(),
					savings.getProductTypeKey());
		case LOAN_PRODUCT:
			LoanProduct lProduct = DemoUtil.getDemoLoanProduct(DemoUtil.demoLaonProductId);
			return new DemoEntityParams(lProduct.getName(), lProduct.getEncodedKey(), lProduct.getId());
		case SAVINGS_PRODUCT:
			SavingsProduct sProduct = DemoUtil.getDemoSavingsProduct(DemoUtil.demoSavingsProductId);
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
		case LINE_OF_CREDIT:
			LineOfCredit loc = DemoUtil.getDemoLineOfCredit(DemoUtil.demoLineOfCreditId);
			return new DemoEntityParams(loc.getClass().getName(), loc.getEncodedKey(), loc.getId());

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
		case LINE_OF_CREDIT:
			LineOfCredit loc = DemoUtil.getDemoLineOfCredit(entityId);
			return loc.getCustomFieldValues();
		default:
			throw new IllegalArgumentException("Custom Filed Value for  " + mambuEntity + " implementation is missing");
		}

	}
}
