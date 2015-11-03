/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.IMAGE_SIZE_TYPE;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
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

	// Our serviceExecutor
	private ServiceExecutor serviceExecutor;
	// Get Document
	private final static ApiDefinition getDocument = new ApiDefinition(ApiType.GET_ENTITY, Document.class);
	// Create Document. The input entity is a JSONDocument and Mambu returns a Document class
	private final static ApiDefinition createDocument = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONDocument.class, Document.class);
	// Delete Document
	private final static ApiDefinition deleteDocument = new ApiDefinition(ApiType.DELETE_ENTITY, Document.class);
	// Get Image
	private final static ApiDefinition getImage = new ApiDefinition(ApiType.GET_ENTITY, Image.class);

	// Specify Mambu entities supported by the GET Documents API: Client, Group. LoanAccount, SavingsAccount,
	// LoanProduct, SavingsProduct, Branch, Centre, User
	private final static MambuEntityType[] supportedEntities = new MambuEntityType[] { MambuEntityType.CLIENT,
			MambuEntityType.GROUP, MambuEntityType.LOAN_ACCOUNT, MambuEntityType.SAVINGS_ACCOUNT,
			MambuEntityType.LOAN_PRODUCT, MambuEntityType.SAVINGS_PRODUCT, MambuEntityType.BRANCH,
			MambuEntityType.CENTRE, MambuEntityType.USER };

	/***
	 * Create a new documents service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public DocumentsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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
		// Upload new document. Example POST JSON api/documents
		// See MBU-3526
		if (document == null) {
			throw new IllegalArgumentException("Document cannot be null");
		}

		// Make the JSON string
		ParamsMap paramsMap = ServiceHelper.makeParamsForDocumentJson(document);

		return serviceExecutor.execute(createDocument, paramsMap);
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
		// Get document. Example: GET /api/documents/documentId
		// See MBU-5084
		// The getDocument API must just return the response as is
		getDocument.setApiReturnFormat(ApiReturnFormat.RESPONSE_STRING);
		return serviceExecutor.execute(getDocument, documentId);
	}

	/***
	 * Delete document by its Id
	 * 
	 * @param documentId
	 *            document id or encoded key
	 * 
	 * @return status
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteDocument(String documentId) throws MambuApiException {
		// Example DELETE api/documents/documentId
		// See MBU-3526
		return serviceExecutor.execute(deleteDocument, documentId);
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
		String apiResponse = serviceExecutor.execute(getImage, imageKey, params);

		// Get only the encoded part. Mambu returns data in the following format: "data:image/jpg;base64,/9j...."
		// The encoded string is base64 encoded with CRLFs, E.g. 9j/4AAQSkZJR...\r\nnHBwgJC4nICIsIxwcKDcpL...
		// We need to get the encoded data which starts right after "data:image/jpg;base64,"

		String base64EncodedString = ServiceHelper.getContentForBase64EncodedMessage(apiResponse);

		return base64EncodedString;
	}

	/***
	 * Get all Documents for a given parent entity
	 * 
	 * @param parentEntity
	 *            MambuEntityType for which documents are retrieved. Must not be null. Documents for the following
	 *            entities are currently supported: Client, Group. LoanAccount, SavingsAccount, LoanProduct,
	 *            SavingsProduct, Branch, Centre, User
	 * @param parentId
	 *            entity id or encoded key for the parent entity. Example, client id for a MambuEntityType.CLIENT. Must
	 *            not be null
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * @return a list of documents for the parent entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getDocuments(MambuEntityType parentEntity, String parentId, Integer offset, Integer limit)
			throws MambuApiException {
		// Example: GET /api/loans/3/documents
		// See MBU-5084
		if (!isSupported(parentEntity)) {
			throw new IllegalArgumentException("GET Documents API is not supported for " + parentEntity);
		}
		return serviceExecutor.getOwnedEntities(parentEntity, parentId, MambuEntityType.DOCUMENT, offset, limit);

	}

	/**
	 * Get supported entity types
	 * 
	 * @return all supported entities
	 */
	public static MambuEntityType[] getSupportedEntities() {
		return supportedEntities;
	}

	/**
	 * Is parent entity type supported by the API
	 * 
	 * @param parentEntityType
	 *            Mambu Entity type
	 * @return true if supported
	 */
	public static boolean isSupported(MambuEntityType parentEntityType) {
		if (parentEntityType == null) {
			return false;
		}

		Set<MambuEntityType> set = new HashSet<MambuEntityType>(Arrays.asList(supportedEntities));
		return (set.contains(parentEntityType)) ? true : false;

	}

}