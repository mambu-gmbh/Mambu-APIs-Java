package com.mambu.apisdk.services;

import java.text.SimpleDateFormat;
import java.util.Date;

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

	@Test
	public void createTaskFormEncoded() throws MambuApiException {

		Task task = new Task();
		task.setTitle("Task #1");
		task.setDescription("foo bar");
		task.setCreatedByUserKey("def");
		task.setTaskLinkType(OwnerType.CLIENT);
		task.setDueDate(new Date());
		task.setTaskLinkKey("ghi");

		service.createTask(task.getTitle(), "abc", task.getDescription(), task.getDueDate(), task.getTaskLinkKey(),
				null);

		ParamsMap params = new ParamsMap();
		params.addParam("title", task.getTitle());
		params.addParam("username", "abc");
		params.addParam("description", task.getDescription());
		params.addParam("duedate", new SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()));
		params.addParam("clientid", task.getTaskLinkKey());
		params.addParam("groupid", null);

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/tasks/", params, Method.POST,
				ContentType.WWW_FORM);
	}

}
