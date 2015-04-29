package com.mambu.apisdk.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.JSONComment;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.core.shared.model.Comment;

/**
 * Service class which handles API operations for retrieving and posting comments for Mambu Entities. Posting and
 * Getting Comments for the following entities are currently supported: Client, Group. LoanAccount, SavingsAccount,
 * LoanProduct, SavingsProduct, Branch, Centre, User
 * 
 * See more details in MBU-8608 - As a Developer, I'd like to GET comments via APIs and MBU-8609 - As a Developer, I
 * need to POST comments via APIs
 * 
 * @author mdanilkis
 * 
 */

public class CommentsService {
	// Entity managed by the class
	private final static MambuEntity ownedEntity = MambuEntity.COMMENT;

	// Specify Mambu entities supported by the Comments API
	final public static Set<MambuEntity> supportedEntities;
	static {
		supportedEntities = new HashSet<MambuEntity>();
		// See MBU-8609 (Mambu 3.11) for a list of supported entities
		supportedEntities.add(MambuEntity.CLIENT);
		supportedEntities.add(MambuEntity.GROUP);
		supportedEntities.add(MambuEntity.LOAN_ACCOUNT);
		supportedEntities.add(MambuEntity.SAVINGS_ACCOUNT);
		supportedEntities.add(MambuEntity.LOAN_PRODUCT);
		supportedEntities.add(MambuEntity.SAVINGS_PRODUCT);
		supportedEntities.add(MambuEntity.BRANCH);
		supportedEntities.add(MambuEntity.CENTRE);
		supportedEntities.add(MambuEntity.USER);

	}

	// Service helper
	private ServiceExecutor serviceExecutor;

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public CommentsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Get all comments for a given entity type.
	 * 
	 * @param parentEntity
	 *            Mambu entity for which comments are retrieved. Example, Client, Branch
	 * 
	 * @param entityId
	 *            entity id or encoded key. Example, client id for a Client.class
	 * @return comments for the forEntity entity type with id = entityId
	 * 
	 * @throws MambuApiException
	 */
	public List<Comment> getComments(MambuEntity parentEntity, String entityId, Integer offset, Integer limit)
			throws MambuApiException {
		// Example: GET /api/clients/ABC123/comments ; GET /api/savings/ABC123/comments
		// See MBU-8608 for more details

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		// Get list of comments for a parent entity
		return serviceExecutor.getOwnedEntities(parentEntity, entityId, ownedEntity, offset, limit);
	}

	/**
	 * Post new Comment
	 * 
	 * @param parentEntity
	 *            class for the entity for which comments are retrieved. Example, Client.class, Branch.class
	 * 
	 * @param parentEntityId
	 *            entity id or encoded key for the parent
	 * @param comment
	 *            comment to post
	 * @throws MambuApiException
	 */
	public Comment postComment(MambuEntity parentEntity, String parentEntityId, Comment comment)
			throws MambuApiException {
		// POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments
		// See MBU-8609 for more details

		// Validate API is used for Supported entity
		validateParentEntity(parentEntity);

		// POST comment using JSONComment wrapper class
		JSONComment jsonComment = new JSONComment(comment);
		return serviceExecutor.postOwnedEntity(parentEntity, parentEntityId, jsonComment);
	}

	/**
	 * Validate parent entity
	 * 
	 * @param parentEntity
	 *            parent entity
	 */
	private void validateParentEntity(MambuEntity parentEntity) {

		if (!supportedEntities.contains(parentEntity)) {
			throw new IllegalArgumentException("Comments API for Entity  " + parentEntity + " are not supported");
		}
	}
}
