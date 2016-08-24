package com.mambu.apisdk.services;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.DatabaseBackup;
import com.mambu.apisdk.model.DatabaseBackupRequest;
import com.mambu.apisdk.model.DatabaseBackupResponse;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
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

	// Create API definition
	private final static ApiDefinition createDatabaseBackup = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			DatabaseBackupRequest.class, DatabaseBackupResponse.class);

	// Download DB backup API definition
	private final static ApiDefinition downloadLatestDbBackup = new ApiDefinition(ApiType.GET_ENTITY,
			DatabaseBackup.class);

	/**
	 * Create a new Database service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
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
	 *            webhooks to send the successful backup message after backup creation on S3 server. Callback URL must
	 *            be valid if provided.
	 * @return A wrapper object containing the return status and code for the database backup trigger.
	 * @throws MambuApiException
	 */
	public DatabaseBackupResponse createDatabaseBackup(DatabaseBackupRequest databaseBackupRequest)
			throws MambuApiException {
		// Available since 4.3. See MBU-11020
		// Example POST /api/database/backup {"callback":"http://somedomain.com"}

		if (databaseBackupRequest != null && databaseBackupRequest.getCallback() != null) {
			validateCallbackUrl(databaseBackupRequest.getCallback());
		}

		createDatabaseBackup.setUrlPath(createDatabaseBackup.getEndPoint() + APIData.FORWARD_SLASH + APIData.BACKUP);

		// delegate execution to service executor
		return serviceExecutor.executeJson(createDatabaseBackup, databaseBackupRequest);

	}

	/**
	 * Downloads the last DB backup if there is one.
	 * 
	 * @return A DatabaseBackup wrapper containing the DB backup as a ByteArrayOutputStream.
	 * @throws MambuApiException
	 */
	public DatabaseBackup downloadLatestDbBackup() throws MambuApiException {
		// Available since 4.3. See MBU-11022.
		// Example GET /api/database/backup/LATEST

		downloadLatestDbBackup.setUrlPath(createDatabaseBackup.getEndPoint() + APIData.FORWARD_SLASH + APIData.BACKUP
				+ APIData.FORWARD_SLASH + APIData.LATEST);

		downloadLatestDbBackup.setApiReturnFormat(ApiReturnFormat.ZIP_ARCHIVE);

		// creates a DatabaseCackup wrapper to store the backup content
		DatabaseBackup backup = new DatabaseBackup();
		backup.setContent((ByteArrayOutputStream) serviceExecutor.execute(downloadLatestDbBackup));

		return backup;
	}

	/**
	 * Validates whether the provided callbackUrl is valid or not. In case is not valid throws an
	 * IllegalArgumentException.
	 * 
	 * @param callbackUrl
	 *            the URL to be validated
	 */
	private void validateCallbackUrl(String callbackUrl) {

		try {
			new URL(callbackUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Callback URL must be valid if provided!", e);
		}

	}

}
