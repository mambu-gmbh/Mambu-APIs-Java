package demo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;
import com.mambu.loans.shared.model.LoanAccount;

/**
 * Test class to show example usage of the documents api calls
 * 
 * @author thobach
 * 
 */
public class DemoTestDocumentsService {

	private static User demoUser;
	private static Client demoClient;
	private static LoanAccount demoLoanAccount;

	private static String UPLOADED_DOCUMENT_KEY;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoClient = DemoUtil.getDemoClient(DemoUtil.demoClientId);
			demoLoanAccount = DemoUtil.getDemoLoanAccount(DemoUtil.demoLaonAccountId);

			testUploadDocumentFromFile();

			testUploadDocument();

			testGetImage();

			// Available since 3.6
			// Test Get documents
			Map<MambuEntityType, List<Document>> docsMap = testGetDocuments();

			// Test Get document file
			testGetDocument(docsMap);

			testDeleteDocument();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Document Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	/**
	 * Test upload document. Document content is hardcoded
	 * 
	 * @throws MambuApiException
	 */
	public static void testUploadDocument() throws MambuApiException {

		System.out.println("\nIn testUploadDocument");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey(demoUser.getEncodedKey());
		document.setDescription("Sample text file");
		document.setDocumentHolderKey(demoClient.getEncodedKey());
		document.setDocumentHolderType(OwnerType.CLIENT);

		document.setName("sample 123");
		document.setOriginalFilename("sample-original.txt");
		document.setType("txt");

		jsonDocument.setDocument(document);

		String documentContent = "VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGRvY3VtZW50IGluIFVURi04IHdpdGggc3BlY2lhbCBjaGFyYWN0ZXJzIGxpa2Ugw6TDtsO8PcOpJyIu";
		jsonDocument.setDocumentContent(documentContent);

		Document documentResponse = documentsService.uploadDocument(jsonDocument);
		// Save the key
		UPLOADED_DOCUMENT_KEY = documentResponse.getEncodedKey();

		System.out.println("Document uploaded OK, ID=" + documentResponse.getId() + "\tName= "
				+ documentResponse.getName() + "\tHolder Type=" + documentResponse.getDocumentHolderType()
				+ "\tHolder Key=" + documentResponse.getDocumentHolderKey());
	}

	/**
	 * Test upload document. Document is retrieved from a file
	 * 
	 * @throws MambuApiException
	 */
	public static void testUploadDocumentFromFile() throws MambuApiException {
		System.out.println("\nIn testUploadDocumentFromFile");

		// Our Test file to upload.
		final String filePath = "./test/data/IMG_1.JPG";

		// Encode this file
		Date d1 = new Date();
		String encodedString = DemoUtil.encodeFileIntoBase64String(filePath);
		Date d2 = new Date();
		long diff1 = d2.getTime() - d1.getTime();
		if (encodedString == null) {
			System.out.println("Failed encoding the file");
			return;
		}

		System.out.println("Time to encode file=" + diff1 + " Length=" + encodedString.length());
		logDocumentContent(encodedString);

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey("1");
		document.setDescription("Sample JPEG file");

		document.setDocumentHolderKey(demoLoanAccount.getEncodedKey());
		// OwnerType.LOAN_ACCOUNT or OwnerType.CLIENT
		document.setDocumentHolderType(OwnerType.LOAN_ACCOUNT);

		// document.setName("Loan Sample JPEG file");

		document.setName("test.jpg");
		document.setOriginalFilename("abc d.jpg");
		document.setType("jpg");

		jsonDocument.setDocument(document);

		// Set the encoded strings
		jsonDocument.setDocumentContent(encodedString);

		// Upload
		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();

		Date d3 = new Date();
		Document documentResponse = documentsService.uploadDocument(jsonDocument);
		Date d4 = new Date();
		long diff2 = d4.getTime() - d3.getTime();
		System.out.println("Time to upload document=" + diff2);
		// Save the key
		UPLOADED_DOCUMENT_KEY = documentResponse.getEncodedKey();
		System.out.println("Document uploaded OK, ID=" + documentResponse.getId() + " Name= "
				+ documentResponse.getName() + " Document Holder Key=" + documentResponse.getDocumentHolderKey());
	}

	/**
	 * Test GET image API
	 * 
	 * @throws MambuApiException
	 */
	public static void testGetImage() throws MambuApiException {
		System.out.println("\nIn testGetImage");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();

		String imageKey = demoClient.getProfilePictureKey();

		if (imageKey == null) {
			imageKey = UPLOADED_DOCUMENT_KEY;
			System.out.println("testGetImage: WARNING: Client =" + demoClient.getFullName()
					+ "  doesn't have a picture attached to test getImage()");
			return;

		}

		// LARGE, MEDIUM, SMALL_THUMB , TINY_THUMB or null for full size
		final IMAGE_SIZE_TYPE sizeType = IMAGE_SIZE_TYPE.MEDIUM; // null

		String base64EncodedImage = documentsService.getImage(imageKey, sizeType);

		System.out.println("\ntestGetImage Ok, Length==" + base64EncodedImage.length() + "\nEncoded String="
				+ base64EncodedImage);

	}

	/**
	 * Test getting documents for all supported entities,
	 * 
	 * @return a map of entity type to a list if retrieved documents for this type. To be used in subsequent GET
	 *         Document details tests
	 * @throws MambuApiException
	 */
	public static Map<MambuEntityType, List<Document>> testGetDocuments() throws MambuApiException {
		System.out.println("\nIn testGetDocuments");

		MambuEntityType[] supportedEntityTypes = DocumentsService.getSupportedEntities();

		Map<MambuEntityType, List<Document>> docsMap = new HashMap<>();

		final Integer offset = 0;
		final Integer limit = 5;
		for (MambuEntityType entityType : supportedEntityTypes) {
			DemoEntityParams entityParams = DemoEntityParams.getEntityParams(entityType);
			String entityKey = entityParams.getId();
			System.out.println("\nGetting Documents for " + entityType + "\tKey=" + entityKey);
			DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
			List<Document> documents = documentsService.getDocuments(entityType, entityKey, offset, limit);
			docsMap.put(entityType, documents);
			// Log the results
			logDocuments(documents);
		}

		return docsMap;

	}

	/**
	 * Test get document details
	 * 
	 * @param docsMap
	 *            a map of previously retrieved documents
	 * @throws MambuApiException
	 */
	public static void testGetDocument(Map<MambuEntityType, List<Document>> docsMap) throws MambuApiException {
		System.out.println("\nIn testGetDocument");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		String document;

		// Get document details for a client
		List<Document> clientDocs = docsMap.get(MambuEntityType.CLIENT);
		if (clientDocs != null && clientDocs.size() > 0) {
			String CLIENT_DOCUMENT_ID = clientDocs.get(0).getEncodedKey();
			System.out.println("\nDocument Details for a Client document with ID=" + CLIENT_DOCUMENT_ID);
			document = documentsService.getDocument(CLIENT_DOCUMENT_ID);
			logDocumentContent(document);
		} else {
			System.out.println("\nNo Documents attached to a demo client");
		}

		// Get document details for a Loan Account
		List<Document> loanDocs = docsMap.get(MambuEntityType.LOAN_ACCOUNT);
		if (loanDocs != null && loanDocs.size() > 0) {
			String LOAN_DOCUMENT_ID = loanDocs.get(0).getEncodedKey();
			System.out.println("\nDocument Details for a Loan document with ID=" + LOAN_DOCUMENT_ID);
			document = documentsService.getDocument(LOAN_DOCUMENT_ID);
			logDocumentContent(document);
		} else {
			System.out.println("\nNo Documents attached to a demo loan account");
		}

		// Get document details for a Savings Account
		List<Document> savingsDocs = docsMap.get(MambuEntityType.SAVINGS_ACCOUNT);
		if (savingsDocs != null && savingsDocs.size() > 0) {
			String SAVAINGS_DOCUMENT_ID = savingsDocs.get(0).getEncodedKey();
			System.out.println("\nDocument Details for a Savings document with ID=" + SAVAINGS_DOCUMENT_ID);
			document = documentsService.getDocument(SAVAINGS_DOCUMENT_ID);
			logDocumentContent(document);
		} else {
			System.out.println("\nNo Documents attached to a demo savings account");
		}

	}

	/**
	 * Test Delete document. Document uploaded in a test above is deleted
	 * 
	 * @throws MambuApiException
	 */
	public static void testDeleteDocument() throws MambuApiException {
		System.out.println("\nIn testDeleteDocument");

		String documentId = UPLOADED_DOCUMENT_KEY;
		if (documentId == null) {
			System.out.println("WARNING: cannot test Delete. document ID is NULL");
			return;
		}
		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		boolean deletionStatus = documentsService.deleteDocument(documentId);
		System.out.println("Deleted Document ID=" + documentId + "\tStatus=" + deletionStatus);

	}

	/**
	 * Log details for retrieved documents
	 * 
	 * @param documents
	 */
	public static void logDocuments(List<Document> documents) {
		if (documents == null) {
			System.out.println("Null Documents input");
			return;
		}

		if (documents.size() == 0) {
			System.out.println("No documents returned");
			return;
		}

		for (Document document : documents) {
			logDocument(document);
		}
	}

	/**
	 * Log details for the returned Document
	 * 
	 * @param doc
	 *            document
	 */
	public static void logDocument(Document doc) {
		if (doc == null) {
			System.out.println("\nNULL Document returned");
			return;
		}
		System.out.println("\nDocument Id=" + doc.getId() + "\tName=" + doc.getName() + "\tFile name="
				+ doc.getOriginalFilename() + "\tType=" + doc.getType());
		System.out.println("Holder Type=" + doc.getDocumentHolderType() + "\tHolder Key=" + doc.getDocumentHolderKey());

	}

	/**
	 * Helper to Log brief details for the encoded document content
	 * 
	 * @param content
	 *            document content
	 */
	private static void logDocumentContent(String content) {

		if (content == null) {
			System.out.println("\nContent is null");
			return;
		}
		final int maxLength = 20;
		System.out.println("\nContent:" + content.substring(0, Math.min(maxLength, content.length() - 1)) + "...");
	}
}
