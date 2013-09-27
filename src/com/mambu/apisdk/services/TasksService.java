/**
 * 
 */
package com.mambu.apisdk.services;

import java.text.DateFormat;
import java.util.Date;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.tasks.shared.model.Task;

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

		String jsonTask = GsonUtils.createGson().toJson(task, Task.class);
		String jsonTaskRequest = String.format("{\"task\":%s}", jsonTask);

		System.out.println("Create json request=" + jsonTaskRequest);

		ParamsMap params = new ParamsMap();
		params.put("JSON", jsonTaskRequest);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		Task taskResult = GsonUtils.createGson().fromJson(jsonResposne, Task.class);

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

}