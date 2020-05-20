package demo;

import java.math.BigDecimal;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.IntelligenceService;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * Test class to show example usage of the api calls for Intelligence service
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestIntelligenceService {

	public static void main(String[] args) {

		DemoUtil.setUpWithBasicAuth();

		try {

			testGetIndicators();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Intelligence Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	private static void testGetIndicators() throws MambuApiException {
		System.out.println("\nIn testGetIndicators");

		IntelligenceService intelligenceService = MambuAPIFactory.getIntelligenceService();

		for (Indicator indicator : Indicator.values()) {
			if (!indicator.isActive()) {
				continue;
			}

			// Note that the some indicators are not supported by API and return
			// {"returnCode":203,"returnStatus":"INVALID_INDICATORS"}
			// See http://developer.mambu.com/customer/portal/articles/1162281-indicators-api
			// Skip indicators not supported by API
			switch (indicator) {
			case TOTAL_LOANS_DISBURSED:
			case UNIQUE_ACCOUNTS:
			case SEPERATOR:
				continue;

			default:
				// Get indicator
				BigDecimal value = intelligenceService.getIndicator(indicator);
				System.out.println("Indicator=" + indicator.name() + "\tValue=" + value);
				break;
			}
		}

	}
}
