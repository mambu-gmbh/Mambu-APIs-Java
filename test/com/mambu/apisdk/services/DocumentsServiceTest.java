package com.mambu.apisdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;

/**
 * @author thobach
 * 
 */
public class DocumentsServiceTest extends MambuAPIServiceTest {

	private DocumentsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new DocumentsService(super.mambuApiService);
	}

	@Test
	public void uploadDocument() throws MambuApiException {

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey("8a3615ef414e97d301415007253359f7");
		document.setDescription("Sample text file");
		document.setDocumentHolderKey("8a38a2c9415022670141507a1eb4001c");
		document.setDocumentHolderType(OwnerType.CLIENT);
		document.setName("sample.txt");
		document.setOriginalFilename("sample-original.txt");
		document.setType("txt");
		jsonDocument.setDocument(document);

		String documentContent = "VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGRvY3VtZW50IGluIFVURi04IHdpdGggc3BlY2lhbCBjaGFyYWN0ZXJzIGxpa2Ugw6TDtsO8PcOpJyIu";
		jsonDocument.setDocumentContent(documentContent);

		service.uploadDocument(jsonDocument);

		ParamsMap params = new ParamsMap();
		params.addParam(
				"JSON",
				"{\"document\":"
						+ "{\"documentHolderKey\":\"8a38a2c9415022670141507a1eb4001c\","
						+ "\"documentHolderType\":\"CLIENT\","
						+ "\"name\":\"sample.txt\","
						+ "\"type\":\"txt\","
						+ "\"originalFilename\":\"sample-original.txt\","
						+ "\"description\":\"Sample text file\","
						+ "\"createdByUserKey\":\"8a3615ef414e97d301415007253359f7\"},"
						+ "\"documentContent\":\"VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGRvY3VtZW50IGluIFVURi04IHdpdGggc3BlY2lhbCBjaGFyYWN0ZXJzIGxpa2Ugw6TDtsO8PcOpJyIu\""
						+ "}");

		// verify
		Mockito.verify(executor).executeRequest(
				"https://demo.mambutest.com/api/documents/", params,
				Method.POST, ContentType.JSON);
	}

}
