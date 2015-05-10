package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.api.server.handler.coments.model.JSONComment;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.core.shared.model.Comment;

/**
 * Service class which handles API operations for retrieving and posting comments for Mambu Entities. Posting and
 * Getting Comments for the following entities are currently supported: Client, Group. LoanAccount, SavingsAccount,
 * LoanProduct, SavingsProduct, Branch, Centre, User
 * 
 * Comments API currently supports:
 * 
 * GET Comments (see MBU-8608 - As a Developer, I'd like to GET comments via APIs) and
 * 
 * POST Comment (see MBU-8609 - As a Developer, I need to POST comments via APIs)
 * 
 * @author mdanilkis
 * 
 */

public class CommentsService extends OwnedEntityService {
	// Specify Entity managed by the class
	private final static MambuEntity ownedEntity = MambuEntity.COMMENT;

	@Override
	protected MambuEntity getOwnedEntity() {
		return ownedEntity;
	}

	// Specify Mambu entities supported by the Comments API
	// Comments are supported by Client, Group. LoanAccount, SavingsAccount, LoanProduct, SavingsProduct, Branch,
	// Centre, User
	final static MambuEntity[] supportedEntities = new MambuEntity[] { MambuEntity.CLIENT, MambuEntity.GROUP,
			MambuEntity.LOAN_ACCOUNT, MambuEntity.SAVINGS_ACCOUNT, MambuEntity.LOAN_PRODUCT,
			MambuEntity.SAVINGS_PRODUCT, MambuEntity.BRANCH, MambuEntity.CENTRE, MambuEntity.USER };

	@Override
	public MambuEntity[] getSupportedEntities() {
		return supportedEntities;
	}

	// Specify Mambu API Methods supported by the Comments API
	// Comments API currently support GET comments and POST Comment. See MBU-8608 and MBU-8609
	// Example: GET /api/clients/ABC123/comments
	// Example: POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments
	/**
	 * Usage: {@link CommentsService#getList(MambuEntity, String, Integer, Integer)}
	 * 
	 * Usage: {@link CommentsService#create(MambuEntity, String, Comment)}
	 * 
	 */
	final static ApiType[] supportedApiTypes = new ApiType[] { ApiType.GET_OWNED_ENTITIES, ApiType.POST_OWNED_ENTITY };

	@Override
	protected ApiType[] getSupporteApiTypes() {
		return supportedApiTypes;
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
	 * Create new Comment. Only "text" field is used, other comment fields are ignored for new comments
	 * 
	 * @param parentEntity
	 *            MambuEntity for which comments are retrieved. Example: MambuEntity.CLIENT for comments owned by
	 *            Client. Must not be null;
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity. Example, ciinetId for MambuEntity.CLIENT. Must not be
	 *            null
	 * @param comment
	 *            comment to post. Must not be null
	 * @return created comment
	 * @throws MambuApiException
	 */
	public Comment create(MambuEntity parentEntity, String parentEntityId, Comment comment)
			throws MambuApiException {

		// POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments
		// See MBU-8609 for more details

		if (comment == null) {
			throw new IllegalArgumentException("Comment cannot be null");
		}
		// Set all fields except the text field to null: Mambu expects only on field in the JSON request
		Comment postComment = new Comment();
		postComment.setText(comment.getText());

		// POST comment using JSONComment wrapper class and parse the result into Comment object
		JSONComment jsonComment = new JSONComment(comment);
		return create(parentEntity, parentEntityId, jsonComment, Comment.class);
	}

	/**
	 * Get comments for a parent entity. Example: GET /api/clients/ABC123/comments
	 * 
	 * Users of the Comments service should call {@link #getOwnedEntities(MambuEntity, String, Integer, Integer)} See
	 * MBU-8608 for details.
	 * 
	 * Example:
	 * 
	 * List<Comment> comments =commentsService.getOwnedEntities(MambuEntity, String, Integer, Integer)}
	 */

}
