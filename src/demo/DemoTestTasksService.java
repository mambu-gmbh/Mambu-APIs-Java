package demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.TasksService;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.OwnerType;
import com.mambu.tasks.shared.model.Task;
import com.mambu.tasks.shared.model.TaskStatus;

/**
 * Test class to show example usage of the api calls
 * 
 * @author thobach
 * 
 */
public class DemoTestTasksService {

	private static User demoUser;
	private static Client demoClient;
	private static Task taskToUpdate;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Get demo entities needed for testing
			demoUser = DemoUtil.getDemoUser();
			demoClient = DemoUtil.getDemoClient();

			// available since Mambu 1.11
			testCreateTaskFromEncoded();

			// available since Mambu 3.3
			testCreateTaskJson();

			// available since Mambu 3.3
			testGetTasks();

			// Available since Mambu 3.6
			testupdateTask();

			testDeleteTask();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Tasks Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static Task testCreateTaskJson() throws MambuApiException {
		System.out.println("\nIn testCreateTaskJson");

		User user = demoUser;
		Client client = demoClient;

		TasksService tasksService = MambuAPIFactory.getTasksService();

		Task task = new Task();
		task.setTitle("Task #1");
		task.setAssignedUserKey(user.getEncodedKey());

		final int dueDaysFromNow = 5;
		Date today = new Date();
		Date someDaysFromNow = new Date(today.getTime() + (24 * dueDaysFromNow * 1000 * 60 * 60));
		task.setDueDate(someDaysFromNow);

		task.setCreatedByUserKey(user.getEncodedKey());
		task.setTaskLinkKey(client.getEncodedKey());
		task.setTaskLinkType(OwnerType.CLIENT);

		task = tasksService.createTask(task);

		System.out.println("Created task =" + task + "  Returned ID=" + task.getId() + " and Due Date="
				+ task.getDueDate());

		return task;

	}

	public static void testCreateTaskFromEncoded() throws MambuApiException {
		System.out.println("\nIn testCreateTaskFromEncoded");

		User user = demoUser;
		Client client = demoClient;

		TasksService tasksService = MambuAPIFactory.getTasksService();

		Task task = new Task();
		task.setTitle("Task #1");
		task.setAssignedUserKey(user.getEncodedKey());
		// don't set due date directly since date format will be invalid (should not contain time)
		task.setDaysUntilDue(0);
		task.setCreatedByUserKey(user.getEncodedKey());
		task.setTaskLinkKey(client.getEncodedKey());
		task.setTaskLinkType(OwnerType.CLIENT);

		task = tasksService.createTask(task.getTitle(), user.getUsername(), task.getDescription(), new Date(),
				task.getTaskLinkKey(), null);

		System.out.println("Created task =" + task + "  Returned=" + task.getId());

	}

	public static List<Task> testGetTasks() throws MambuApiException {
		System.out.println("\nIn testGetTasks");

		// Get Input params
		String clientId = demoClient.getId(); // or null;
		String username = demoUser.getUsername(); // or null;

		clientId = demoClient.getId();
		username = demoUser.getId();

		TaskStatus taskStatus = TaskStatus.OPEN; // TaskStatus.OPEN or TaskStatus.COMPLETED;
		// Pagination params
		String offset = "0"; // or null;
		String limit = "50"; // or null;

		TasksService tasksService = MambuAPIFactory.getTasksService();

		List<Task> tasks = tasksService.getTasks(username, clientId, taskStatus, offset, limit);

		if (tasks == null) {
			System.out.println("No tasks returned, null object");
			return null;
		}

		System.out.println("Total tasks returned=" + tasks.size() + "\tFor User=" + username + "\tFor client ID= "
				+ clientId + "\tfor Status=" + taskStatus);

		if (tasks.size() > 0) {
			taskToUpdate = tasks.get(0);
		}
		for (Task task : tasks) {
			System.out.println("Username=" + task.getAssignedUserName() + "\tClient Name=" + task.getTaskLinkName()
					+ "\tClient Key=" + task.getTaskLinkKey() + "\tID=" + task.getId() + "\tTitle=" + task.getTitle()
					+ "\tStatus=" + task.getStatus().name());
		}
		System.out.println();

		return tasks;
	}

	public static void testupdateTask() throws MambuApiException {
		System.out.println("\nIn testupdateTask");

		if (taskToUpdate == null) {
			System.out.println("No task to update");
			return;
		}

		final String changeNote = " updated by API";
		String description = taskToUpdate.getDescription();
		taskToUpdate.setDescription(description + changeNote);

		taskToUpdate.setTitle(taskToUpdate.getTitle() + changeNote);

		// Reverse completion status for testing
		TaskStatus status = taskToUpdate.getStatus();
		if (status == TaskStatus.OPEN) {
			taskToUpdate.setToCompleted(new Date());
		} else {
			taskToUpdate.setToOpen();
		}

		// Update Task
		TasksService tasksService = MambuAPIFactory.getTasksService();
		Task modifiedTask = tasksService.updateTask(taskToUpdate);

		if (!modifiedTask.getEncodedKey().equalsIgnoreCase(taskToUpdate.getEncodedKey())) {
			System.out.println("Error! The Existent Task =" + taskToUpdate.getEncodedKey() + " was not updated");
			return;
		}
		System.out
				.println("New Description=" + modifiedTask.getDescription() + " New Title=" + modifiedTask.getTitle());

	}

	public static void testDeleteTask() throws MambuApiException {
		System.out.println("\nIn testDeleteTask");

		List<Task> someDemoTasks = testGetTasks();

		if (someDemoTasks == null || someDemoTasks.isEmpty()) {
			// Add New task
			Task task = testCreateTaskJson();
			someDemoTasks = new ArrayList<Task>();
			someDemoTasks.add(task);
		}

		boolean testForSuccess = true; // or test for false

		Task theTask;
		String taskId;

		if (testForSuccess) {
			theTask = someDemoTasks.iterator().next();
			taskId = String.valueOf(theTask.getId());
		} else {
			taskId = UUID.randomUUID().toString();
			System.out.println("Testing Task deletion with random ID=" + taskId);
		}

		TasksService tasksService = MambuAPIFactory.getTasksService();
		boolean status = tasksService.deleteTasks(taskId);

		System.out.println("Deletion status=" + status);
	}
}
