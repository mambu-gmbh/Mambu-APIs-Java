/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.core.shared.model.Image;
import com.mambu.docs.shared.model.Document;

/**
 * Service class which handles API operations like uploading documents
 * 
 * @author thobach
 * 
 */
public class DocumentsService {

	private static String SIZE = APIData.SIZE;

	// Our service helper
	private ServiceHelper serviceHelper;
	// Get Document
	private final static ApiDefinition getDocument = new ApiDefinition(ApiType.GET_ENTITY, Document.class);
	// Create Document. The input entity is a JSONDocument and Mambu returns a Document class
	private final static ApiDefinition createDocument = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONDocument.class, Document.class);
	// Get Image
	private final static ApiDefinition getImage = new ApiDefinition(ApiType.GET_ENTITY, Image.class);

	/***
	 * Create a new documents service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public DocumentsService(MambuAPIService mambuAPIService) {
		this.serviceHelper = new ServiceHelper(mambuAPIService);
	}

	/***
	 * Upload new Document using a JSONDocument object and as json request
	 * 
	 * @param document
	 *            the new document object to be uploaded containing all mandatory fields
	 * 
	 * @return the new document parsed as an object returned from the API call
	 * 
	 * @throws MambuApiException
	 */
	public Document uploadDocument(JSONDocument document) throws MambuApiException {

		if (document == null) {
			throw new IllegalArgumentException("Document cannot be null");
		}
		// Use custom parsing for the potentially very large document object. JSONDocument object
		// contains a Document object and also the encoded documentContent part, which can be a very large string. For
		// memory management efficiency reasons it is better no to be asking Gson to parse the whole JSONDocument
		// object (with this potentially very large string included) but to parse only the Document object with
		// the blank content part and then insert the actual document content value into the resulting JSON ourselves
		// (there were reports of out of memory errors during Gson parsing JSONDocument objects with a large document
		// content)

		// Make the JSON string
		String documentJson = makeDocumentJson(document);

		// Add generated JSON string to the ParamsMap
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, documentJson);

		return serviceHelper.execute(createDocument, paramsMap);
	}

	/***
	 * Get base64 encoded document data by document id. A typical scenario would be getting a list of attachments for a
	 * client/group/account via getDocuments() API and then retrieving a specific document (attachment) by its id with
	 * this API call.
	 * 
	 * @param documentId
	 *            the encoded key or id of the document
	 * 
	 * @return base64 encoded document content
	 * 
	 * @throws MambuApiException
	 */
	public String getDocument(String documentId) throws MambuApiException {
		// The getDocument API must just return the response as is
		getDocument.setApiReturnFormat(ApiReturnFormat.RESPONSE_STRING);
		return serviceHelper.execute(getDocument, documentId);
	}

	/***
	 * Get an Image file using file's encoded key and the preferred image size
	 * 
	 * @param imageKy
	 *            a key to access image file (e.g. client's profile picture key: client.getProfilePictureKey())
	 * @param sizeType
	 *            a desired size to be returned. E.g LARGE, MEDIUM, SMALL_THUMB, TINY_THUMB. Can be null to get full
	 *            size
	 * 
	 * @return a base64 string with encoded image file
	 * 
	 * @throws MambuApiException
	 */
	public String getImage(String imageKey, IMAGE_SIZE_TYPE sizeType) throws MambuApiException {

		// Add size type as a parameter
		ParamsMap params = null;
		if (sizeType != null) {
			params = new ParamsMap();
			params.put(SIZE, sizeType.name());
		}

		// For this API we just need the response string as is to extract the encoded image
		getImage.setApiReturnFormat(ApiReturnFormat.RESPONSE_STRING);
		String apiResponse = serviceHelper.execute(getImage, imageKey, params);

		// Get only the encoded part. Mambu returns the following format: "data:image/jpg;base64,/9j...."
		// The encoded string is base64 encoded with CRLFs, E.g. 9j/4AAQSkZJR...\r\nnHBwgJC4nICIsIxwcKDcpL...
		// We need to get the encoded data which starts right after "data:image/jpg;base64,"

		// Find ";base64,";
		final String encodingStartsAfter = APIData.BASE64_ENCODING_INDICATOR;

		// Check if the response format is as expected and return null otherwise
		String base64EncodedString = null;
		if (apiResponse != null && apiResponse.contains(encodingStartsAfter)) {

			final int dataStart = apiResponse.indexOf(encodingStartsAfter) + encodingStartsAfter.length();
			// Get the actual encoded string part. From dataStart till the end (without the enclosing double quote char)
			base64EncodedString = apiResponse.substring(dataStart, apiResponse.length() - 1);
		}

		return base64EncodedString;
	}

	/***
	 * Create JSON string for JSONDocument object
	 * 
	 * @param document
	 *            JSONDocument document containing Document object and documentContent string
	 * 
	 * @return JSON string
	 */
	private String makeDocumentJson(JSONDocument document) {

		// Create JSON string in two steps: a) parse document object with the blank document content value and b) insert
		// document content value into the JSON string. This approach requires less memory than just using
		// Gson.toJson(document) parser

		// Save document content
		String documentContent = document.getDocumentContent();

		// Set the content in the JSONDocument to blank for parsing
		document.setDocumentContent("");

		// Parse modified JSONDocument with the blank content value
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(document, JSONDocument.class);

		// Now insert back document content value into the generated JSON string
		StringBuffer finalJson = new StringBuffer(jsonData.length() + documentContent.length());
		finalJson.append(jsonData);

		// Find the position to insert document content (into the "" part of the "documentContent":"")
		final String contentPair = "\"documentContent\":\"\"";
		int insertPosition = finalJson.indexOf(contentPair) + contentPair.length() - 1;

		// Insert document content
		finalJson.insert(insertPosition, documentContent);

		return finalJson.toString();
	}

}