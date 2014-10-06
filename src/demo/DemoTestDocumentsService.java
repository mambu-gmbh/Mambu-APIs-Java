package demo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

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
	private static SavingsAccount demoSavingsAccount;

	private static String UPLOADED_DOCUMENT_KEY;

	private static String CLIENT_DOCUMENT_ID;
	private static String LOAN_DOCUMENT_ID;
	private static String SAVAINGS_DOCUMENT_ID;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoClient = DemoUtil.getDemoClient();
			demoLoanAccount = DemoUtil.getDemoLoanAccount();
			demoSavingsAccount = DemoUtil.getDemoSavingsAccount();

			testUploadDocumentFromFile();

			testUploadDocument();

			testGetImage();

			// Available since 3.6
			testGetDocuments();

			testGetDocument();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Document Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

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

	public static void testUploadDocumentFromFile() throws MambuApiException {
		System.out.println("\nIn testUploadDocumentFromFile");

		// Our Test file to upload.
		final String filePath = "./test/data/IMG_1.JPG";

		// Encode this file
		Date d1 = new Date();
		String encodedString = encodeFileIntoBase64String(filePath);
		Date d2 = new Date();
		long diff1 = d2.getTime() - d1.getTime();
		if (encodedString == null) {
			System.out.println("Failed encoding the file");
			return;
		}

		System.out.println("Time to encode file=" + diff1 + " Length=" + encodedString.length() + " Encoded string:"
				+ encodedString);

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey("1");
		document.setDescription("Sample JPEG file");

		document.setDocumentHolderKey(demoLoanAccount.getEncodedKey());
		// OwnerType.LOAN_ACCOUNT or OwnerType.CLIENT
		document.setDocumentHolderType(OwnerType.LOAN_ACCOUNT);

		// document.setName("Loan Sample JPEG file");

		document.setName("test.jpg");
		document.setOriginalFilename("abc d.txt");
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

	//
	public static void testGetImage() throws MambuApiException {
		System.out.println("\nIn testGetImage");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();

		String imageKey = demoClient.getProfilePictureKey();

		if (imageKey == null) {
			imageKey = UPLOADED_DOCUMENT_KEY;
			System.out.println("testGetImage: Client =" + demoClient.getFullName()
					+ "  doesn't have a picture attached. Getting the just uploaded image, Key=" + imageKey);
		}

		// LARGE, MEDIUM, SMALL_THUMB , TINY_THUMB or null for full size
		final IMAGE_SIZE_TYPE sizeType = IMAGE_SIZE_TYPE.MEDIUM; // null

		String base64EncodedImage = documentsService.getImage(imageKey, sizeType);

		System.out.println("\ntestGetImage Ok, Length==" + base64EncodedImage.length() + "\nEncoded String="
				+ base64EncodedImage);

	}

	public static void testGetDocuments() throws MambuApiException {
		System.out.println("\nIn testGetDocuments");

		List<Document> documents;

		// Get documents for a demo Client
		ClientsService clientService = MambuAPIFactory.getClientService();
		documents = clientService.getClientDocuments(demoClient.getId());

		// Log the results
		System.out.println("\nDocuments for a Client with ID=" + demoClient.getId());
		logDocuments(documents);
		// Save the first doc ID for subsequent getDocument() tests
		if (documents != null && documents.size() > 0) {
			CLIENT_DOCUMENT_ID = documents.get(0).getEncodedKey();
		}

		// Get documents for a demo Loan Account
		LoansService loansService = MambuAPIFactory.getLoanService();
		documents = loansService.getLoanAccountDocuments(demoLoanAccount.getId());

		// Log the results
		System.out.println("\nDocuments for a Loan Account with ID=" + demoLoanAccount.getId());
		logDocuments(documents);
		// Save the first doc ID for subsequent getDocument() tests
		if (documents != null && documents.size() > 0) {
			LOAN_DOCUMENT_ID = documents.get(0).getEncodedKey();
		}

		// Get documents for a demo Savings Account
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		documents = savingsService.getSavingsAccountDocuments(demoSavingsAccount.getId());

		// Log the results
		System.out.println("\nDocuments for a Savings Account with ID=" + demoSavingsAccount.getId());
		logDocuments(documents);
		// Save the first doc ID for subsequent getDocument() tests
		if (documents != null && documents.size() > 0) {
			SAVAINGS_DOCUMENT_ID = documents.get(0).getEncodedKey();
		}

	}

	public static void testGetDocument() throws MambuApiException {
		System.out.println("\nIn testGetDocument");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		String document;

		// Get document details for a client
		if (CLIENT_DOCUMENT_ID != null) {
			System.out.println("\nDocument Details for a Client document with ID=" + CLIENT_DOCUMENT_ID);
			document = documentsService.getDocument(CLIENT_DOCUMENT_ID);
			System.out.println("\nContent:" + document);
		} else {
			System.out.println("\nNo Documents attached to a demo client");
		}

		// Get document details for a Loan Account
		if (LOAN_DOCUMENT_ID != null) {
			System.out.println("\nDocument Details for a Loan document with ID=" + LOAN_DOCUMENT_ID);
			document = documentsService.getDocument(LOAN_DOCUMENT_ID);
			System.out.println("\nContent:" + document);
		} else {
			System.out.println("\nNo Documents attached to a demo loan account");
		}

		// Get document details for a Savings Account
		if (SAVAINGS_DOCUMENT_ID != null) {
			System.out.println("\nDocument Details for a Savings document with ID=" + SAVAINGS_DOCUMENT_ID);
			document = documentsService.getDocument(SAVAINGS_DOCUMENT_ID);
			System.out.println("\nContent:" + document);
		} else {
			System.out.println("\nNo Documents attached to a demo savings account");
		}

	}

	// Private Helpers
	private static String encodeFileIntoBase64String(String absolutePath) {
		final String methodName = "encodeFileIntoBase64String";

		System.out.println("Encoding image file=" + absolutePath);

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(absolutePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Picture file not found. Path=" + absolutePath);
			return null;
		}

		// Convert file to bytes stream
		byte[] bytes;
		byte[] buffer = new byte[8192];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println(methodName + " IO Exception reading file=" + absolutePath + " Message=" + e.getMessage());
			return null;
		}
		bytes = output.toByteArray();

		// Encode the byte stream
		String encodedString = Base64.encodeBase64URLSafeString(bytes);
		boolean isBase64 = Base64.isArrayByteBase64(encodedString.getBytes());
		System.out.println("Encoded document. Is String Base64=" + isBase64);

		// Close open streams
		try {
			inputStream.close();
			output.close();
		} catch (IOException e) {

		}
		return encodedString;

	}

	// Log returned Documents
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

	// Log details for the returned Document
	public static void logDocument(Document doc) {
		if (doc == null) {
			System.out.println("\nNULL Document returned");
			return;
		}
		System.out.println("\nDocument Id=" + doc.getId() + "\tName=" + doc.getName() + "\tFile name="
				+ doc.getOriginalFilename() + "\tType=" + doc.getType());
		System.out.println("Holder Type=" + doc.getDocumentHolderType() + "\tHolder Key=" + doc.getDocumentHolderKey());

	}
}
