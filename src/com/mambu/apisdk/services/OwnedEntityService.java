package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.apisdk.util.ServiceExecutor;

/**
 * Abstract Service class which handles API operations for the owned Mambu entities (getting, posting, patching and
 * deleting owned entities). Extending class must specify the type of entity they manage (their owned entity, example
 * Document) the supported ApiTypes and the set of parent entities that are supported for this owned entity.
 * 
 * For example, CommentsService can extend this class and would return MambuEntity.COMMENT as the owned entity it
 * manages, supported ApiTypes as GET_OWNED_ENTITIES and POST_OWNED_ENTITY, and also a set of MambuEntities parents
 * supported by the Comments API. The OwnedEntityService provides the implementation for the actual API calls
 * 
 * @author mdanilkis
 * 
 */

public abstract class OwnedEntityService {

	// Extended classes must specify the type of owned entity. Example: Document or Comment
	abstract protected MambuEntity getOwnedEntity();

	// Extended classes must specify supported parent classes
	abstract public MambuEntity[] getSupportedEntities();

	// Extended classes must specify ApiTypes supported by it
	abstract protected ApiType[] getSupporteApiTypes();

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
	 *            Mambu entity for which owned entities are retrieved. MambuEntity.CLIENT, MambuEntity.BRANCH
	 * @param parentId
	 *            entity id or encoded key for the parent entity. Example, client id for a MmabuEntity.CLIENT
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * @return a list of owned entities for the parent entity
	 * 
	 * @throws MambuApiException
	 */
	public <T> List<T> getOwnedEntities(MambuEntity parentEntity, String parentId, Integer offset, Integer limit)
			throws MambuApiException {
		// Example: GET /api/clients/ABC123/comments ; GET /api/savings/ABC123/documents

		// Validate that GET_OWNED_ENTITIES API is supported
		validateAPiIsSupported(ApiType.GET_OWNED_ENTITIES);

		// Validate specified parent entity is supported
		validateParentEntity(parentEntity);

		// Get a list of owned entities for a parent entity
		// Derived class provides the owned entity managed by it. Example, Comment, CustomFieldValue
		MambuEntity ownedEntity = getOwnedEntity();
		return serviceExecutor.getOwnedEntities(parentEntity, parentId, ownedEntity, offset, limit);

	}

	/**
	 * Create new owned entity. This method is used when Mambu returns the same class as the posted entity
	 * 
	 * @param parentEntity
	 *            parent MambuEntity for which owned entities are retrieved. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param ownedEntity
	 *            owned entity object to post
	 * @return resulting owned entity
	 * 
	 * @throws MambuApiException
	 */
	public <T> T createOwnedEntity(MambuEntity parentEntity, String parentEntityId, T ownedEntity)
			throws MambuApiException {
		// Example: POST LoanTransaction /api/loans/ABC123/transactions

		// Validate POST_OWNED_ENTITY is supported
		validateAPiIsSupported(ApiType.POST_OWNED_ENTITY);

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		return serviceExecutor.createOwnedEntity(parentEntity, parentEntityId, ownedEntity);
	}

	/**
	 * Create new owned entity. This method is used when the class of the result returned by Mambu differs form the
	 * posted entity class
	 * 
	 * @param parentEntity
	 *            parent MambuEntity for which owned entities are retrieved. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param ownedEntity
	 *            owned entity object to post
	 * @param resultClass
	 *            the class for the result returned by Mambu. Example: Comment.class
	 * @return resulting owned entity
	 * @throws MambuApiException
	 */
	public <R, T> R createOwnedEntity(MambuEntity parentEntity, String parentEntityId, T ownedEntity,
			Class<?> resultClass) throws MambuApiException {
		// Example: Post JSONComment. Mambu returns resulting Comment object back
		// POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments

		// Validate that POST_OWNED_ENTITY is supported
		validateAPiIsSupported(ApiType.POST_OWNED_ENTITY);

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		return serviceExecutor.createOwnedEntity(parentEntity, parentEntityId, ownedEntity, resultClass);
	}

	/***
	 * Update owned entity. This method sends a PATCH request for the provided owned entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the owned entity is updated. Example: MambuEntity.CLIENT, MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param ownedEntityId
	 *            the encoded key or id of the owned entity to be updated
	 * @param ownedEntity
	 *            owned entity object
	 * @return updated owned entity
	 * @throws MambuApiException
	 */
	public <T> boolean updateOwnedEntity(MambuEntity parentEntity, String parentEntityId, String ownedEntityId,
			T ownedEntity) throws MambuApiException {
		// Example: Execute request for PATCH API to update custom field value for a Loan Account
		// e.g. PATCH "{ "value": "10" }" /host/api/loans/accointId/custominformation/customFieldId

		// Validate that PATCH_OWNED_ENTITY is supported
		validateAPiIsSupported(ApiType.PATCH_OWNED_ENTITY);

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		// Submit API request
		return serviceExecutor.updateOwnedEntity(parentEntity, parentEntityId, ownedEntity, ownedEntityId);

	}

	/***
	 * Delete owned entity for a Mambu parent entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which owned entity is deleted. Example: MambuEntity.CLIENT, MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param ownedEntityId
	 *            the encoded key or id of the owned entity to be deleted
	 * @return true if successful
	 * @throws MambuApiException
	 */
	public boolean deleteOwnedEntity(MambuEntity parentEntity, String parentEntityId, String ownedEntityId)
			throws MambuApiException {
		// Example: Execute request for DELETE API to delete custom field for a client
		// e.g. DELETE /host/api/clients/clientId/custominformation/customFieldId

		// Validate that DELETE_OWNED_ENTITY is supported
		validateAPiIsSupported(ApiType.DELETE_OWNED_ENTITY);

		validateParentEntity(parentEntity);

		// Derived class will provide the owned entity managed by it. Example, Comment, CustomFieldValue
		MambuEntity ownedEntity = getOwnedEntity();
		return serviceExecutor.deleteOwnedEntity(parentEntity, parentEntityId, ownedEntity, ownedEntityId);

	}

	// Private methods
	/**
	 * Helper to convert an array of supported entities into a set. This method uses abstract
	 * {@link #getSupportedEntities()} to get an array of supported entities
	 * 
	 * @return set of supported entities
	 */
	private Set<MambuEntity> getSupportedEntitiesSet() {
		MambuEntity[] supportedEntities = getSupportedEntities();
		return new HashSet<MambuEntity>(Arrays.asList(supportedEntities));
	}

	/**
	 * Validate that specified parent entity is supported by the extending class
	 * 
	 * @param parentEntity
	 *            parent entity
	 */
	private void validateParentEntity(MambuEntity parentEntity) {
		// Get MambuEntities supported by the extending class
		Set<MambuEntity> supportedEntities = getSupportedEntitiesSet();

		// Check if the the specified parent entity type is supported
		if (supportedEntities == null || !supportedEntities.contains(parentEntity)) {
			throw new IllegalArgumentException("Parent Entity  " + parentEntity + " is not supported by "
					+ getOwnedEntity());
		}
	}

	/**
	 * Validate if the requested method (its Api Type) is supported by the extending class.
	 * 
	 * @param apiType
	 *            Api type used by a method
	 */
	private void validateAPiIsSupported(ApiType apiType) {
		// Get ApiTypes supported by the extending class
		Set<ApiType> supportedEntities = new HashSet<ApiType>(Arrays.asList(getSupporteApiTypes()));

		// Check if the the method's apiType is supported
		if (supportedEntities == null || !supportedEntities.contains(apiType)) {
			throw new IllegalArgumentException("API method  " + apiType + " is not supported for " + getOwnedEntity());
		}
	}
}
