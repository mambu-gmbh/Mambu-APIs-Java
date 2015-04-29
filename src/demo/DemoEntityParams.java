package demo;

import java.util.HashSet;

import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
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

	private final static HashSet<MambuEntity> demoEntities;
	static {
		demoEntities = new HashSet<MambuEntity>();
		demoEntities.add(MambuEntity.CLIENT);
		demoEntities.add(MambuEntity.GROUP);
		demoEntities.add(MambuEntity.LOAN_ACCOUNT);
		demoEntities.add(MambuEntity.SAVINGS_ACCOUNT);
		demoEntities.add(MambuEntity.LOAN_PRODUCT);
		demoEntities.add(MambuEntity.SAVINGS_PRODUCT);
		demoEntities.add(MambuEntity.USER);
		demoEntities.add(MambuEntity.BRANCH);
		demoEntities.add(MambuEntity.CENTRE);

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
	public static DemoEntityParams getEntityParams(MambuEntity mambuEntity) throws MambuApiException {
		if (mambuEntity == null) {
			throw new IllegalArgumentException("Demo entity  cannot be null");
		}
		if (!demoEntities.contains(mambuEntity)) {
			throw new IllegalArgumentException("Demo entity " + mambuEntity + " is not supported");
		}

		switch (mambuEntity) {
		case CLIENT:
			Client client = DemoUtil.getDemoClient();
			return new DemoEntityParams(client.getFullName(), client.getEncodedKey(), client.getId());
		case GROUP:
			Group group = DemoUtil.getDemoGroup();
			return new DemoEntityParams(group.getGroupName(), group.getEncodedKey(), group.getId());
		case LOAN_ACCOUNT:
			LoanAccount loan = DemoUtil.getDemoLoanAccount();
			return new DemoEntityParams(loan.getName(), loan.getEncodedKey(), loan.getId());
		case SAVINGS_ACCOUNT:
			SavingsAccount savings = DemoUtil.getDemoSavingsAccount();
			return new DemoEntityParams(savings.getName(), savings.getEncodedKey(), savings.getId());
		case LOAN_PRODUCT:
			LoanProduct lProduct = DemoUtil.getDemoLoanProduct();
			return new DemoEntityParams(lProduct.getName(), lProduct.getEncodedKey(), lProduct.getId());
		case SAVINGS_PRODUCT:
			SavingsProduct sProduct = DemoUtil.getDemoSavingsProduct();
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

}
