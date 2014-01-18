package demo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
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

	private static String UPLOADED_DOCUMENT_KEY = "";

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoClient = DemoUtil.getDemoClient();
			demoLoanAccount = DemoUtil.getDemoLoanAccount();

			testUploadDocumentFromFile();

			testUploadDocument();

			testGetImage();

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
		document.setName("sample.txt");
		document.setOriginalFilename("sample-original.txt");
		document.setType("txt");
		jsonDocument.setDocument(document);

		String documentContent = "VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGRvY3VtZW50IGluIFVURi04IHdpdGggc3BlY2lhbCBjaGFyYWN0ZXJzIGxpa2Ugw6TDtsO8PcOpJyIu";
		jsonDocument.setDocumentContent(documentContent);

		Document documentResponse = documentsService.uploadDocument(jsonDocument);
		// Save the key
		UPLOADED_DOCUMENT_KEY = documentResponse.getEncodedKey();

		System.out.println("Document uploaded OK, ID=" + documentResponse.getId() + " Name= "
				+ documentResponse.getName() + " Document Holder Key=" + documentResponse.getDocumentHolderKey());
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

		// / Loan: 8a24a0b141a804030141aacf6ac42d1f // Client: 8ad3e12340719f2b0140878460884524
		document.setDocumentHolderKey(demoLoanAccount.getEncodedKey());
		// OwnerType.LOAN_ACCOUNT or OwnerType.CLIENT
		document.setDocumentHolderType(OwnerType.LOAN_ACCOUNT);
		document.setName("Loan Sample JPEG file");
		document.setOriginalFilename("sample-original.jpg");
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

}
