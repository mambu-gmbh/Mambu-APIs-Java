package com.mambu.apisdk.services;

import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;

/**
 * Abstract Service class which handles API operations for the owned Mambu entities (getting, posting, patching and
 * deleting owned entities). Extending class must specify the type of entity they manage (their owned entity, example
 * Document) the supported API methods and the set of parent entities that are supported for this owned entity.
 * 
 * For example, CommentsService can extend this class and would specify MambuEntity.COMMENT as the owned entity,
 * supported API methods as GET and POST, and also a set of MambuEntities supported by the Comments API. The
 * OwnedEntityService provides the implementation for the actual API calls
 * 
 * @author mdanilkis
 * 
 */

public abstract class OwnedEntityService {

	// Extended classes must specify the type of owned entity. Example, Document, Comment
	abstract protected MambuEntity getOwnedEntity();

	// Extended classes must specify supported parent classes
	abstract protected Set<MambuEntity> getSupportedEntities();

	// Extended classes must specify API methods supported by it
	abstract protected Set<RequestExecutor.Method> getSupporteMethods();

	// Service helper
	protected ServiceExecutor serviceExecutor;

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public OwnedEntityService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Get all owned entities for a given parent entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which owned entities are retrieved. Example, Client, Branch
	 * 
	 * @param entityId
	 *            entity id or encoded key. Example, client id for a Client.class
	 * 
	 * @return a list of owned entities for parentEntity
	 * 
	 * @throws MambuApiException
	 */
	public <T> List<T> getOwnedEntities(MambuEntity parentEntity, String entityId, Integer offset, Integer limit)
			throws MambuApiException {
		// Example: GET /api/clients/ABC123/comments ; GET /api/savings/ABC123/comments
		// See MBU-8608 for more details

		// Validate GET is supported
		validateAPiIsSupported(Method.GET);

		// Validate the API is used for the Supported entity
		validateParentEntity(parentEntity);

		// Get a list of owned entities for a parent entity
		// Derived class provides the owned entity managed by it. Example, Comment, CustomFieldValue
		MambuEntity ownedEntity = getOwnedEntity();
		return serviceExecutor.getOwnedEntities(parentEntity, entityId, ownedEntity, offset, limit);

	}

	/**
	 * Post new owned entity
	 * 
	 * @param parentEntity
	 *            class for the entity for which comments are retrieved. Example, Client.class, Branch.class
	 * @param parentEntityId
	 *            entity id or encoded key for the parent
	 * @param ownedEntity
	 *            owned entity to post
	 * @throws MambuApiException
	 */
	public <R, T> R postOwnedEntity(MambuEntity parentEntity, String parentEntityId, T ownedEntity)
			throws MambuApiException {
		// POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments
		// See MBU-8609 for more details

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		return serviceExecutor.postOwnedEntity(parentEntity, parentEntityId, ownedEntity);
	}

	/***
	 * Update owned value. This method sends a PATCH request for the provided owned entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the owned entity is updated. Example, Client, Branch
	 * @param parentEntityId
	 *            entity id or encoded key for the parent
	 * @param ownedEntityId
	 *            the encoded key or id of the owned entity to be updated
	 * @param ownedEntity
	 *            the new owned entity object
	 * @return updated owned entity
	 * @throws MambuApiException
	 */
	public <T> boolean updateOwnedEntity(MambuEntity parentEntity, String parentEntityId, String ownedEntityId,
			T ownedEntity) throws MambuApiException {

		// Execute request for PATCH API to update custom field value for a Loan Account. See MBU-6661
		// e.g. PATCH "{ "value": "10" }" /host/api/loans/accointId/custominformation/customFieldId

		// Or API request to PATCH a linked field for the custom field value (see MBU-8514)
		// PATCH '{ "linkedEntityKeyValue": "40288a13...." }'// /api/loans/abc123/custominformation/customFieldId

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		// Submit API request
		return serviceExecutor.patchOwnedEntity(parentEntity, parentEntityId, ownedEntity, ownedEntityId);

	}

	/***
	 * Delete owned entity for a Mambu parent entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which owned entity is deleted. Example, Client, Branch
	 * @param parentEntityId
	 *            entity id or encoded key for the parent
	 * @param ownedEntityId
	 *            the encoded key or id of the owned entity to be deleted
	 * @return true if successful
	 * @throws MambuApiException
	 */
	public boolean deleteOwnedEntity(MambuEntity parentEntity, String parentEntityId, String ownedEntityId)
			throws MambuApiException {
		// Execute request for DELETE API to delete custom field value for a client
		// e.g. DELETE /host/api/clients/clientId/custominformation/customFieldId

		validateParentEntity(parentEntity);

		// Derived class will provide the owned entity managed by it. Example, Comment, CustomFieldValue
		MambuEntity ownedEntity = getOwnedEntity();
		return serviceExecutor.deleteOwnedEntity(parentEntity, parentEntityId, ownedEntity, ownedEntityId);

	}

	/**
	 * Validate parent entity. This methods validates if the provided parent entity is supported by the extending class
	 * 
	 * @param parentEntity
	 *            parent entity
	 */
	protected void validateParentEntity(MambuEntity parentEntity) {
		Set<MambuEntity> supportedEntities = getSupportedEntities();

		if (supportedEntities == null || !supportedEntities.contains(parentEntity)) {
			throw new IllegalArgumentException("Parent Entity  " + parentEntity + " is not supported by "
					+ getOwnedEntity());
		}
	}

	/**
	 * Validate API supported entity. This methods validates if the provided request is supported by the extending class
	 * 
	 * @param parentEntity
	 *            parent entity
	 */
	protected void validateAPiIsSupported(Method method) {
		Set<RequestExecutor.Method> supportedEntities = getSupporteMethods();

		if (supportedEntities == null || !supportedEntities.contains(method)) {
			throw new IllegalArgumentException("API method  " + method + " is not supported for " + getOwnedEntity());
		}
	}
}
