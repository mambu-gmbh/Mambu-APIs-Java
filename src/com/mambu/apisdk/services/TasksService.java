/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.api.server.handler.tasks.model.JSONTask;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.exception.MambuApiResponseMessage;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.tasks.shared.model.Task;
import com.mambu.tasks.shared.model.TaskStatus;

/**
 * Service class which handles API operations like getting and creating tasks
 * 
 * @author thobach
 * 
 */
public class TasksService {

	private MambuAPIService mambuAPIService;

	private static String TASKS = APIData.TASKS;

	// Task create fields
	private static String TITLE = APIData.TITLE;
	private static String USERNAME = APIData.USERNAME;
	private static String DESCRIPTION = APIData.DESCRIPTION;
	private static String DUE_DATE = APIData.DUE_DATE;
	private static String CLIENT_ID = APIData.CLIENT_ID;
	private static String GROUP_ID = APIData.GROUP_ID;

	/***
	 * Create a new task service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public TasksService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * Create a new task using a Task object in a json request
	 * 
	 * @param task
	 *            the new task object containing all mandatory fields. The encoded key must be null for new tasks
	 * 
	 * @return the new task parsed as an object returned from the API call
	 * 
	 * @throws MambuApiException
	 */
	public Task createTask(Task task) throws MambuApiException {

		// Get encodedKey and ensure it's NULL for the new client request
		String encodedKey = task.getEncodedKey();

		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create Task, the encoded key must be null");
		}

		// create task api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		return submitTaskJsonRequest(urlString, task);

	}

	/***
	 * Update an existent task using a Task object in a json request
	 * 
	 * @param task
	 *            the existent task object containing all mandatory fields. The encoded key must not be null for
	 *            updating tasks
	 * 
	 * @return the updated task parsed as an object returned from the API call
	 * 
	 * @throws MambuApiException
	 */
	public Task updateTask(Task task) throws MambuApiException {

		// Get encodedKey and ensure it's NOT NULL for task update requests
		String encodedKey = task.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Task, the encoded key must be NOT null");
		}

		// Update task api call. Since Mambu 3.6 the task id or encoded key should be used in update API calls
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/" + encodedKey));

		return submitTaskJsonRequest(urlString, task);

	}

	/**
	 * Creates a new task using an html form encoded request
	 * 
	 * @param title
	 * @param username
	 * @param description
	 * @param dueDate
	 * @param clientId
	 * @param groupId
	 * 
	 * @return the new task parsed as an object returned from the API call
	 * 
	 * @throws MambuApiException
	 */
	public Task createTask(String title, String username, String description, Date dueDate, String clientId,
			String groupId) throws MambuApiException {

		if (dueDate == null) {
			throw new IllegalArgumentException("Due date cannot be null");
		}

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		DateFormat df = APIData.URLDATE_FORMATTER;

		ParamsMap params = new ParamsMap();
		params.put(TITLE, title);
		params.put(USERNAME, username);
		params.put(DESCRIPTION, description);
		params.put(DUE_DATE, df.format(dueDate));
		params.put(CLIENT_ID, clientId);
		params.put(GROUP_ID, groupId);

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.WWW_FORM);
		Task taskResult = GsonUtils.createGson().fromJson(jsonResposne, Task.class);

		return taskResult;
	}

	/***
	 * Get tasks based on the specified criteria, which can include clientId, username, and/or task's state
	 * 
	 * @param username
	 * @param clientId
	 * @param taskStatus
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * 
	 * @return a list of tasks matching specified criteria. If tasksStatus is null then Open tasks are returned. To get
	 *         tasks for the user a username must not be null. To get tasks for the client the clientId must not be null
	 *         and the username must be null. If both username and clientId are not null then the clientId is ignored
	 *         and all tasks for the user are returned.
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<Task> getTasks(String username, String clientId, TaskStatus taskStatus, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(APIData.USERNAME, username);
		paramsMap.addParam(APIData.CLIENT_ID, clientId);
		if (taskStatus != null) {
			paramsMap.addParam(APIData.STATUS, taskStatus.name());
		}
		paramsMap.put(APIData.OFFSET, offset);
		paramsMap.put(APIData.LIMIT, limit);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<Task>>() {
		}.getType();

		List<Task> tasks = (List<Task>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return tasks;
	}

	/***
	 * Delete task by its Id
	 * 
	 * @param taskId
	 * 
	 * @return status
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteTasks(String taskId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/" + taskId));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.DELETE);

		// On success the response is: {"returnCode":0,"returnStatus":"SUCCESS"}
		// An exception can be thrown: E.g. ({"returnCode":980,"returnStatus":"INVALID_TASK_ID"})

		// Parse the response. (Though, as no exception was thrown here, must be a "SUCCESS" response)
		boolean deletionStatus = false;
		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0) {
			deletionStatus = true;
		}

		return deletionStatus;
	}

	/***
	 * Helper method to submit the Create or Update API request with Task's Json request
	 * 
	 * @param taskUrlString
	 *            the url to either create or to update task, The update url must include task's encoded Key or Id. The
	 *            create request must not.
	 * 
	 * @param task
	 *            task to be created or updated
	 * @return task
	 * 
	 * @throws MambuApiException
	 */
	private Task submitTaskJsonRequest(String taskUrlString, Task task) throws MambuApiException {

		// Convert object to json
		JSONTask inputJsonTask = new JSONTask(task);
		String jsonTaskRequest = GsonUtils.createGson().toJson(inputJsonTask, JSONTask.class);

		ParamsMap params = new ParamsMap();
		params.put(APIData.JSON_OBJECT, jsonTaskRequest);

		String jsonResposne = mambuAPIService.executeRequest(taskUrlString, params, Method.POST, ContentType.JSON);

		JSONTask jsonTask = GsonUtils.createGson().fromJson(jsonResposne, JSONTask.class);
		// Get Task from JsonTask
		Task taskResult = jsonTask.getTask();

		return taskResult;

	}
}
