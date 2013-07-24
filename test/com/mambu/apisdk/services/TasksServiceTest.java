package com.mambu.apisdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.docs.shared.model.OwnerType;
import com.mambu.tasks.shared.model.Task;

/**
 * @author thobach
 * 
 */
public class TasksServiceTest extends MambuAPIServiceTest {

	private TasksService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new TasksService(super.mambuApiService);
	}

	@Test
	public void createTaskJson() throws MambuApiException {

		Task task = new Task();
		task.setTitle("Task #1");
		task.setDaysUntilDue(1);
		task.setAssignedUserKey("abc");
		task.setCreatedByUserKey("def");
		task.setTaskLinkType(OwnerType.CLIENT);
		task.setTaskLinkKey("ghi");

		service.createTask(task);

		ParamsMap params = new ParamsMap();
		params.addParam(
				"JSON",
				"{\"task\":{\"title\":\"Task #1\",\"createdByUserKey\":\"def\",\"status\":\"OPEN\",\"taskLinkKey\":\"ghi\",\"taskLinkType\":\"CLIENT\",\"daysUntilDue\":1,\"assignedUserKey\":\"abc\"}}");

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/tasks/", params, Method.POST,
				ContentType.JSON);
	}

}
