/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
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
	 * @return
	 * @throws MambuApiException
	 */
	public Task createTask(Task task) throws MambuApiException {

		// Convert object to json

		String jsonTask = GsonUtils.createResponse().toJson(task, Task.class);
		String jsonTaskRequest = String.format("{\"task\":%s}", jsonTask);

		System.out.println("Create json request=" + jsonTaskRequest);

		ParamsMap params = new ParamsMap();
		params.put("JSON", jsonTaskRequest);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(TASKS + "/"));

		String jsonResposne = mambuAPIService.executeRequest(urlString, params,
				Method.POST_JSON);

		Task taskResult = GsonUtils.createResponse().fromJson(jsonResposne,
				Task.class);

		return taskResult;
	}

}