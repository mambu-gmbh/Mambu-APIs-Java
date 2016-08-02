package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.DatabaseBackupRequest;
import com.mambu.apisdk.model.DatabaseBackupResponse;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ServiceExecutor;

/**
 * Service class which handles the API operations available for the database management (e.g. backups)
 * 
 * @author acostros
 *
 */
public class DatabaseService {

	// Our ServiceExecutor
	private ServiceExecutor serviceExecutor;

	// Create AI definition
	private final static ApiDefinition createDatabaseBackup = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			DatabaseBackupRequest.class, DatabaseBackupResponse.class);

	@Inject
	public DatabaseService(MambuAPIService mambuAPIService) {

		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Triggers the process of creating a DB backup. Be aware that creating a DB backup is a long running process and
	 * the response after calling this method is just a confirmation that the process started successfully to create the
	 * backup.
	 * 
	 * @param databaseBackupRequest
	 *            The request object for the database backup API containing the callback URL that will be used by
	 *            webhooks to send the successful backup message after backup creation on S3 server. Wrapper, or
	 *            callback URL cannot be null.
	 * @return A wrapper object containing the return status and code for the database backup trigger.
	 * @throws MambuApiException
	 */
	public DatabaseBackupResponse createDatabaseBackup(DatabaseBackupRequest databaseBackupRequest)
			throws MambuApiException {
		// Available since 4.3. See MBU-11020
		// Example POST {"callback":"http://somedomain.com"}'

		if (databaseBackupRequest == null || databaseBackupRequest.getCallback() == null) {
			throw new IllegalArgumentException("Request object and callback field must not be null!!");
		}

		createDatabaseBackup.setUrlPath(createDatabaseBackup.getEndPoint() + "/" + APIData.BACKUP);

		// delegate execution to service executor
		return serviceExecutor.executeJson(createDatabaseBackup, databaseBackupRequest);

	}

}
