package demo;

import java.util.List;

import com.mambu.core.shared.model.User;

/**
 * @author cezarrom
 */
class UsersUtil {

	private UsersUtil() {
	}

	// Log some details for a list of users. Prefix with an optional message
	static void logUsers(List<User> users, String message) {
		if (users == null) {
			return;
		}
		message = message == null ? "" : message;
		System.out.println(message + "\tTotal Users=" + users.size());
		for (User user : users) {
			logIndividualUserDetails(user);
		}
		System.out.println();
	}

	static void logIndividualUserDetails(User user) {
		System.out.println("User details:");
		System.out.println("\tUsername = " + user.getUsername());
		System.out.println("\tName = " + user.getFullName());
		System.out.println("\tId = " + user.getId());
		System.out.println("\tBranch = " + user.getAssignedBranchKey());
		System.out.println("\tUsername = " + user.getUsername());
		System.out.println("\tNotes = " + user.getNotes());
	}

}
