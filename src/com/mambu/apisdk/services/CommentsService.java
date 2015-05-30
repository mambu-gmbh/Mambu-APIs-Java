package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.api.server.handler.coments.model.JSONComment;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.core.shared.model.Comment;

/**
 * Service class which handles API operations for retrieving and posting comments for Mambu Entities. Posting and
 * Getting Comments for the following entities is currently supported: Client, Group. LoanAccount, SavingsAccount,
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

public class CommentsService {
	// Specify Entity managed by the class
	private final static MambuEntityType ownedEntity = MambuEntityType.COMMENT;

	// Service helper
	protected ServiceExecutor serviceExecutor;

	// Specify Mambu entities supported by the Comments API
	// Comments are supported by Client, Group. LoanAccount, SavingsAccount, LoanProduct, SavingsProduct, Branch,
	// Centre, User
	private final static MambuEntityType[] supportedEntities = new MambuEntityType[] { MambuEntityType.CLIENT,
			MambuEntityType.GROUP, MambuEntityType.LOAN_ACCOUNT, MambuEntityType.SAVINGS_ACCOUNT,
			MambuEntityType.LOAN_PRODUCT, MambuEntityType.SAVINGS_PRODUCT, MambuEntityType.BRANCH,
			MambuEntityType.CENTRE, MambuEntityType.USER };

	public static MambuEntityType[] getSupportedEntities() {
		return supportedEntities;
	}

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

	/**
	 * Create new Comment. Only "text" field is used, other comment fields are ignored for new comments
	 * 
	 * @param parentEntity
	 *            MambuEntityType for which comments are retrieved. Example: MambuEntityType.CLIENT for comments owned
	 *            by Client. Must not be null. Comments for the following entities are currently supported: Client,
	 *            Group. LoanAccount, SavingsAccount, LoanProduct, SavingsProduct, Branch, Centre, User
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity. Example, ciinetId for MambuEntityType.CLIENT. Must not
	 *            be null
	 * @param comment
	 *            comment to post. Must not be null
	 * @return created comment
	 * @throws MambuApiException
	 */
	public Comment create(MambuEntityType parentEntity, String parentEntityId, Comment comment)
			throws MambuApiException {

		// POST {"comment:":{"text":"Posting a new comment" }} /api/centres/ABC123/comments
		// Available since Mambu 3.11 See MBU-8609 for more details

		if (comment == null) {
			throw new IllegalArgumentException("Comment cannot be null");
		}
		// Set all fields except the text field to null: Mambu expects only on field in the JSON request
		Comment postComment = new Comment();
		postComment.setText(comment.getText());

		// POST comment using JSONComment wrapper class and parse the result into Comment object
		JSONComment jsonComment = new JSONComment(comment);
		return serviceExecutor.createOwnedEntity(parentEntity, parentEntityId, jsonComment, Comment.class);
	}

	/***
	 * Get all Comments for a given parent entity
	 * 
	 * @param parentEntity
	 *            MambuEntityType for which comments are retrieved. MambuEntityType.CLIENT, MambuEntity.BRANCH. Must not
	 *            be null. Comments for the following entities are currently supported: Client, Group. LoanAccount,
	 *            SavingsAccount, LoanProduct, SavingsProduct, Branch, Centre, User
	 * @param parentId
	 *            entity id or encoded key for the parent entity. Example, client id for a MambuEntityType.CLIENT. Must
	 *            not be null
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * @return a list of comments for the parent entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Comment> getComments(MambuEntityType parentEntity, String parentId, Integer offset, Integer limit)
			throws MambuApiException {
		// Example: GET /api/clients/ABC123/comments GET /api/loans/XYZ123/comments
		// Available since Mambu 3.11. See MBU-8608 for more details

		return serviceExecutor.getOwnedEntities(parentEntity, parentId, ownedEntity, offset, limit);

	}

	/**
	 * Is parent entity type supported by the Comments API
	 * 
	 * @param parentEntityType
	 *            Mambu Entity type
	 * @return
	 */
	public static boolean isSupported(MambuEntityType parentEntityType) {
		if (parentEntityType == null) {
			return false;
		}

		Set<MambuEntityType> theSet = new HashSet<MambuEntityType>(Arrays.asList(supportedEntities));
		return (theSet.contains(parentEntityType)) ? true : false;

	}
}
