/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
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
	 * Create a new task using a Task object and as json request
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

}