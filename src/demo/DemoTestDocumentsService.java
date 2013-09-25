package demo;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;

/**
 * Test class to show example usage of the documents api calls
 * 
 * @author thobach
 * 
 */
public class DemoTestDocumentsService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testUploadDocument();

		} catch (MambuApiException e) {
			System.out
					.println("Exception caught in Demo Test Document Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message="
					+ e.getMessage());
		}

	}

	public static void testUploadDocument() throws MambuApiException {

		System.out.println("\nIn testUploadDocument");

		DocumentsService documentsService = MambuAPIFactory
				.getDocumentsService();

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey("8a3615ef414e97d301415007253359f7");
		document.setDescription("Sample text file");
		document.setDocumentHolderKey("8a38a2c9415022670141507a1eb4001c");
		document.setDocumentHolderType(OwnerType.CLIENT);
		document.setName("sample.txt");
		document.setOriginalFilename("sample-original.txt");
		document.setType("txt");
		jsonDocument.setDocument(document);

		String documentContent = "VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGRvY3VtZW50IGluIFVURi04IHdpdGggc3BlY2lhbCBjaGFyYWN0ZXJzIGxpa2Ugw6TDtsO8PcOpJyIu";
		jsonDocument.setDocumentContent(documentContent);

		Document documentResponse = documentsService
				.uploadDocument(jsonDocument);

		System.out.println("Document uploaded OK, ID="
				+ documentResponse.getId() + " Name= "
				+ documentResponse.getName() + " Document Holder Key="
				+ documentResponse.getDocumentHolderKey());
	}
}
