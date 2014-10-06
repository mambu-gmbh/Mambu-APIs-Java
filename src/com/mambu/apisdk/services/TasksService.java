/**
 * 
 */
package com.mambu.apisdk.services;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.api.server.handler.tasks.model.JSONTask;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.tasks.shared.model.Task;
import com.mambu.tasks.shared.model.TaskStatus;

/**
 * Service class which handles API operations like getting and creating tasks
 * 
 * @author thobach
 * 
 */
public class TasksService {

	// Task create fields
	private static String TITLE = APIData.TITLE;
	private static String USERNAME = APIData.USERNAME;
	private static String DESCRIPTION = APIData.DESCRIPTION;
	private static String DUE_DATE = APIData.DUE_DATE;
	private static String CLIENT_ID = APIData.CLIENT_ID;
	private static String GROUP_ID = APIData.GROUP_ID;

	// Service Executor
	private ServiceExecutor serviceExecutor;
	// API definitions
	private final static ApiDefinition getTasks = new ApiDefinition(ApiType.GET_LIST, Task.class);
	// Create and Update Task API expects JSONTask
	private final static ApiDefinition createTask = new ApiDefinition(ApiType.CREATE_JSON_ENTITY, JSONTask.class);
	private final static ApiDefinition updateTask = new ApiDefinition(ApiType.UPDATE_JSON, JSONTask.class);
	private final static ApiDefinition createFormTask = new ApiDefinition(ApiType.CREATE_FORM_ENTITY, Task.class);
	private final static ApiDefinition deleteTask = new ApiDefinition(ApiType.DELETE_ENTITY, Task.class);

	/***
	 * Create a new task service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public TasksService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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

		// Tasks API expects JSONTask as input
		JSONTask jsonTask = new JSONTask(task);
		JSONTask jsonResult = serviceExecutor.executeJson(createTask, jsonTask);
		return (jsonResult == null) ? null : jsonResult.getTask();

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
		// Tasks API expects JSONTask as input
		JSONTask jsonTask = new JSONTask(task);
		JSONTask jsonResult = serviceExecutor.executeJson(updateTask, jsonTask, encodedKey);
		return (jsonResult == null) ? null : jsonResult.getTask();
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

		DateFormat df = APIData.URLDATE_FORMATTER;

		ParamsMap params = new ParamsMap();
		params.put(TITLE, title);
		params.put(USERNAME, username);
		params.put(DESCRIPTION, description);
		params.put(DUE_DATE, df.format(dueDate));
		params.put(CLIENT_ID, clientId);
		params.put(GROUP_ID, groupId);

		return serviceExecutor.execute(createFormTask, params);
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

		return serviceExecutor.execute(getTasks, paramsMap);
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
		return serviceExecutor.execute(deleteTask, taskId);
	}

}
