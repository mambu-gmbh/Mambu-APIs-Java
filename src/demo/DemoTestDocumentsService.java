package demo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.clients.shared.model.Client;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;

/**
 * Test class to show example usage of the documents api calls
 * 
 * @author thobach
 * 
 */
public class DemoTestDocumentsService {

	private static Client demoClient;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoClient = DemoUtil.getDemoClient();

			testGetImage();

			testUploadDocumentFromFile();

			testUploadDocument();

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

		Document documentResponse = documentsService.uploadDocument(jsonDocument);

		System.out.println("Document uploaded OK, ID=" + documentResponse.getId() + " Name= "
				+ documentResponse.getName() + " Document Holder Key=" + documentResponse.getDocumentHolderKey());
	}

	public static void testUploadDocumentFromFile() throws MambuApiException {
		System.out.println("\nIn testUploadDocumentFromFile");

		// Our Test file to upload
		// final String filePath = "./test/data/test_photo_3m_1.JPG";
		final String filePath = "./test/data/Ira.JPG";

		// Encode this file
		String encodedString = encodeFileIntoBase64String(filePath);

		if (encodedString == null) {
			System.out.println("Failed encoding the file");
			return;
		}
		System.out.println("Encoded string:" + encodedString);

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey("1");
		document.setDescription("Sample JPEG file");

		// / Loan: 8a24a0b141a804030141aacf6ac42d1f // Client: 8ad3e12340719f2b0140878460884524
		document.setDocumentHolderKey("8a24a0b141a804030141aacf6ac42d1f");
		// OwnerType.LOAN_ACCOUNT or OwnerType.CLIENT
		document.setDocumentHolderType(OwnerType.LOAN_ACCOUNT);
		document.setName("Loan Sample JPEG file");
		document.setOriginalFilename("sample-original.jpg");
		document.setType("jpg");
		jsonDocument.setDocument(document);

		// Set the encoded strings
		String documentContent = encodedString;

		// Set the Json document
		jsonDocument.setDocumentContent(documentContent);

		// Upload
		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		Document documentResponse = documentsService.uploadDocument(jsonDocument);

		System.out.println("Document uploaded OK, ID=" + documentResponse.getId() + " Name= "
				+ documentResponse.getName() + " Document Holder Key=" + documentResponse.getDocumentHolderKey());
	}

	//
	public static void testGetImage() throws MambuApiException {
		System.out.println("\nIn testGetImage");

		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();

		final String imageKey = demoClient.getProfilePictureKey();

		if (imageKey == null) {
			System.out.println("testGetImage: Client =" + demoClient.getFullName()
					+ "  doesn't have a picture attached. Choose another");
			return;
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
