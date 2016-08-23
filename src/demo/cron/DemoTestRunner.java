package demo.cron;

import java.util.TimerTask;

import demo.DemoTestAccountingService;
import demo.DemoTestActivitiesService;
import demo.DemoTestClientService;
import demo.DemoTestCommentsService;
import demo.DemoTestCustomFiledValueService;
import demo.DemoTestDocumentTemplatesService;
import demo.DemoTestDocumentsService;
import demo.DemoTestIntelligenceService;
import demo.DemoTestLoCService;
import demo.DemoTestLoanService;
import demo.DemoTestMultiTenantService;
import demo.DemoTestOrganizationService;
import demo.DemoTestRepaymentService;
import demo.DemoTestSavingsService;
import demo.DemoTestSearchService;
import demo.DemoTestTasksService;
import demo.DemoTestUsersService;

/**
 * Test runner class. Executes the main method of each demo test class.
 * 
 * @author acostros
 *
 */

public class DemoTestRunner extends TimerTask {

	@Override
	public void run() {

		System.out.println("Starting DemoTestRunner");

		DemoTestAccountingService.main(null);

		DemoTestActivitiesService.main(null);

		DemoTestClientService.main(null);

		DemoTestCommentsService.main(null);

		DemoTestCustomFiledValueService.main(null);

		DemoTestDocumentsService.main(null);

		DemoTestDocumentTemplatesService.main(null);

		DemoTestIntelligenceService.main(null);

		DemoTestIntelligenceService.main(null);

		DemoTestLoanService.main(null);

		DemoTestLoCService.main(null);

		DemoTestMultiTenantService.main(null);

		DemoTestOrganizationService.main(null);

		DemoTestRepaymentService.main(null);

		DemoTestSavingsService.main(null);

		DemoTestSearchService.main(null);

		DemoTestTasksService.main(null);

		DemoTestUsersService.main(null);

		System.out.println("DemoTestRunner finished its job");
	}

}
