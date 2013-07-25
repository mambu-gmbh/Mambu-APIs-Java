package demo;

import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.TasksService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.OwnerType;
import com.mambu.tasks.shared.model.Task;

/**
 * Test class to show example usage of the api calls
 * 
 * @author thobach
 * 
 */
public class DemoTestTasksService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			// available since Mambu 1.11
			testCreateTaskFromEncoded();

			// available since Mambu 3.3
			testCreateTaskJson();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Tasks Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testCreateTaskJson() throws MambuApiException {
		System.out.println("\nIn testCreateTaskJson");

		UsersService usersService = MambuAPIFactory.getUsersService();
		User thomas = usersService.getUserByUsername("demo");

		ClientsService clientsService = MambuAPIFactory.getClientService();
		List<Client> clients = clientsService.getClientByFullName("Doe", "John");
		Client client;
		if (clients.isEmpty()) {
			client = clientsService.createClient("John", "Doe");
		} else {
			client = clients.iterator().next();
		}

		TasksService tasksService = MambuAPIFactory.getTasksService();

		Task task = new Task();
		task.setTitle("Task #1");
		task.setAssignedUserKey(thomas.getEncodedKey());
		// don't set due date directly since date format will be invalid (should not contain time)
		task.setDaysUntilDue(0);
		task.setCreatedByUserKey(thomas.getEncodedKey());
		task.setTaskLinkKey(client.getEncodedKey());
		task.setTaskLinkType(OwnerType.CLIENT);

		task = tasksService.createTask(task);

		System.out.println("Created task =" + task + "  Returned=" + task.getId());

	}

	public static void testCreateTaskFromEncoded() throws MambuApiException {
		System.out.println("\nIn testCreateTaskFromEncoded");

		UsersService usersService = MambuAPIFactory.getUsersService();
		User user = usersService.getUserByUsername("demo");

		ClientsService clientsService = MambuAPIFactory.getClientService();
		List<Client> clients = clientsService.getClientByFullName("Doe", "John");
		Client client;
		if (clients.isEmpty()) {
			client = clientsService.createClient("John", "Doe");
		} else {
			client = clients.iterator().next();
		}

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

}
