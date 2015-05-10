package demo;

import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.CommentsService;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.core.shared.model.Comment;

/**
 * Test class to show example usage for Comments API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestCommentsService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testCreateAndGetComments(); // Available since 3.11

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Comments Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	// Test creating a comment and getting comments for all supported Mambu entities
	// As of Mambu 3.11 the following entity types support Comments: Client. Group, LoanAccount, SavingsAccount,
	// LoanProduct, SavingsProduct, Branch, Centre, User
	public static void testCreateAndGetComments() throws MambuApiException {
		System.out.println("\nIn testCreateAndGetComments");

		// Getting Comments for all supported entities
		CommentsService commentsService = MambuAPIFactory.getCommentsService();

		// Iterate through supported entity types and Create a comment first and then Get all comments back
		MambuEntity[] supportedEntities = commentsService.getSupportedEntities();
		for (MambuEntity parentEntity : supportedEntities) {
			// Get key for a parent entity. Use demo entity
			DemoEntityParams entityParams = DemoEntityParams.getEntityParams(parentEntity);
			String parentyKey = entityParams.getEncodedKey();
			String parentName = entityParams.getName();

			System.out.println("Testing Comments for  " + parentEntity + "\tName=" + parentName + " with Key="
					+ parentyKey);

			// Test Posting comments first to have at least one comment available for the entity to test GET API
			// Make test comment
			Comment aComment = new Comment();
			aComment.setText("Test Comment For " + parentEntity + " " + parentName + " on " + new Date().toString());

			// Post this Comment
			Comment result = commentsService.create(parentEntity, parentyKey, aComment);
			System.out.println("POSTED Comment OK. Text=" + result.getText());

			// Now Test getting Comments back
			// Test GET all Comments for this parent entity
			Integer offset = 0;
			Integer limit = 20;
			List<Comment> comments = commentsService.getList(parentEntity, parentyKey, offset, limit);
			// Log returned Comments
			System.out.println("\nTotal comments returned=" + comments.size());
			for (Comment comment : comments) {
				System.out.println("Comment=" + comment.getText() + " Parent=" + comment.getParentKey() + " User="
						+ comment.getUserKey());
			}

		}

	}
}
