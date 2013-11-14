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
	 * Create a new task using a Task object and as json request
	 * 
	 * @param task
	 *            the new task object contain all mandatory fields
	 * 
	 * @return the new task parsed as an object returned from the API call
	 * 
	 * @throws MambuApiException
	 */
	public Task createTask(Task task) throws MambuApiException {

		// Convert object to json
		JSONTask inputJsonTask = new JSONTask(task);
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		String jsonTaskRequest = GsonUtils.createGson(dateTimeFormat).toJson(inputJsonTask, JSONTask.class);

		// System.out.println("Create json request=" + jsonTaskRequest);

		ParamsMap params = new ParamsMap();
		params.put("JSON", jsonTaskRequest);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		JSONTask jsonTask = GsonUtils.createGson().fromJson(jsonResposne, JSONTask.class);
		// Get Tsk from JsonTask
		Task taskResult = jsonTask.getTask();

		return taskResult;
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
	 * @return a list of tasks matching specified criteria
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<Task> getTasks(String username, String clientId, TaskStatus taskStatus, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(APIData.USERNAME, username);
		paramsMap.addParam(APIData.CLIENT_ID, clientId);
		paramsMap.addParam(APIData.STATUS, taskStatus.name());
		paramsMap.put(APIData.OFFSET, offset);
		paramsMap.put(APIData.LIMIT, limit);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<Task>>() {}.getType();

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
		// An exception can be thrown: E.g, ({"returnCode":980,"returnStatus":"INVALID_TASK_ID"})

		// Parse the response. (Though, as no exception was thrown here, must be a "SUCCESS" response)
		boolean deletionStatus = false;
		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0)
			deletionStatus = true;

		return deletionStatus;
	}

}