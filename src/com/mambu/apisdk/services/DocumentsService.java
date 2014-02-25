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