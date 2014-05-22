/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.docs.shared.model.Document;

/**
 * Service class which handles API operations like uploading documents
 * 
 * @author thobach
 * 
 */
public class DocumentsService {

	private MambuAPIService mambuAPIService;

	private static String DOCUMENTS = APIData.DOCUMENTS;
	private static String IMAGES = APIData.IMAGES;
	private static String SIZE = APIData.SIZE;

	/***
	 * Create a new documents service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public DocumentsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * Get current mambuAPIService
	 * 
	 * @return mambuAPIService the service responsible for the connection to the server
	 */
	public MambuAPIService getMambuAPIService() {
		return mambuAPIService;
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

		// Convert object to json
		String jsonDocument = GsonUtils.createGson().toJson(document, JSONDocument.class);

		ParamsMap params = new ParamsMap();
		params.put(APIData.JSON_OBJECT, jsonDocument);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(DOCUMENTS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		Document documentResult = GsonUtils.createGson().fromJson(jsonResponse, Document.class);

		return documentResult;
	}

	/***
	 * Get all documents for a specific Mambu service by providing the API end point and entity ID. Note, this is a
	 * Helper method intended to be used primarily by other Mambu services. Services supporting GET Document attachments
	 * can implement their own wrapper for getDocuments() service by calling this helper method. For example,
	 * ClientService implements getDocuments() by calling DocumentsService.getDocumnts(mambuAPIService, APIData.CLIENTS,
	 * clientId). As of Mambu 3.6, the following services can request GET Documents: ClientService, LoanService and
	 * SavingsService (this list could, potentially, be extended in a future to support other services, for example,
	 * UserService, OrganizationService (for getting Branch and Centre attachments), etc.)
	 * 
	 * See {@link https://mambucom.jira.com/browse/MBU-5084} for more details
	 * 
	 * @param mambuAPIService
	 *            Mambu Api service to use for executing API request
	 * 
	 * @param serviceEndPoint
	 *            the API endpoint string for this service. E.g. APIData.CLIENT, APIData.LOANS, etc.
	 * 
	 * @param entityId
	 *            the encoded key or id of the Mambu entity for which the attached documents are to be retrieved
	 * 
	 * @return documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public static List<Document> getDocuments(MambuAPIService mambuAPIService, String serviceEndPoint, String entityId)
			throws MambuApiException {

		if (mambuAPIService == null) {
			throw new IllegalArgumentException("Mambu API Service must not be null");
		}

		if (serviceEndPoint == null || serviceEndPoint.trim().isEmpty()) {
			throw new IllegalArgumentException("Service EndPoint must not be null or empty");
		}
		if (entityId == null || entityId.trim().isEmpty()) {
			throw new IllegalArgumentException("Entity ID must not be null or empty");
		}

		String urlString = new String(mambuAPIService.createUrl(serviceEndPoint + "/" + entityId + "/" + DOCUMENTS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		// Parse the Json and get a list of returned documents
		Type collectionType = new TypeToken<List<Document>>() {
		}.getType();
		List<Document> documents = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return documents;
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
	public String getDocument(long documentId) throws MambuApiException {

		if (documentId <= 0) {
			throw new IllegalArgumentException("Document ID must a positive number representing existent document id");
		}
		final String documentidParam = Long.toString(documentId);

		String urlString = new String(mambuAPIService.createUrl(DOCUMENTS + "/" + documentidParam));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		return jsonResponse;
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

		if (imageKey == null || imageKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Image key cannot be null or empty");
		}

		// Add size type as a parameter
		ParamsMap params = new ParamsMap();
		if (sizeType != null) {
			params.put(SIZE, sizeType.name());
		}

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(IMAGES + "/" + imageKey));

		String apiResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

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
}