package com.mambu.apisdk.services;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.JSONComment;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.apisdk.util.RequestExecutor.Method;
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

public class CommentsService extends OwnedEntityService {
	// Entity managed by the class
	private final static MambuEntity ownedEntity = MambuEntity.COMMENT;

	protected MambuEntity getOwnedEntity() {
		return ownedEntity;
	}

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

	protected Set<MambuEntity> getSupportedEntities() {
		return supportedEntities;
	}

	// Specify Mambu API Methods supported by the Comments API
	final public static Set<Method> supportedMethods;
	static {
		supportedMethods = new HashSet<Method>();
		// See MBU-8609 (Mambu 3.11) for a list of supported APIs
		supportedMethods.add(Method.GET);
		supportedMethods.add(Method.POST);
	}

	protected Set<Method> getSupporteMethods() {
		return supportedMethods;
	}

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public CommentsService(MambuAPIService mambuAPIService) {
		super(mambuAPIService);
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

		// POST comment using JSONComment wrapper class
		JSONComment jsonComment = new JSONComment(comment);
		return postOwnedEntity(parentEntity, parentEntityId, jsonComment);
	}
}
