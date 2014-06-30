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

		DemoUtil.setUp();

		try {

			testGetIndicators();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Intelligence Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetIndicators() throws MambuApiException {
		System.out.println("\nIn testGetIndicator");

		IntelligenceService intelligenceService = MambuAPIFactory.getIntelligenceService();

		for (Indicator indicator : Indicator.values()) {
			if (!indicator.isActive()) {
				continue;
			}

			// TODO: Mambu issue? The following indicators return {"returnCode":203,"returnStatus":"INVALID_INDICATORS"}
			// TOTAL_LOANS_DISBURSED and SEPERATOR (not deprecated), UNIQUE_ACCOUNTS (deprecated).
			// Skip indicators returning "INVALID_INDICATORS"
			switch (indicator) {
			case TOTAL_LOANS_DISBURSED:
			case UNIQUE_ACCOUNTS:
			case SEPERATOR:
				continue;

			}
			BigDecimal value = intelligenceService.getIndicator(indicator);
			System.out.println("Indicator=" + indicator.name() + "\tValue=" + value);
		}

	}
}
