package com.mambu.apisdk.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.ServiceTestBase;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor;

/**
 * @author lpinkowski
 *
 */

public class ActivitiesServiceTest extends ServiceTestBase {

    private static final String ACTIVITIES_ENDPOINT = "https://demo.mambutest.com/api/activities";
	private ActivitiesService service;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    private String dateFromString = "2010-10-10";
    private String dateToString = "2011-11-11";
    private Date dateFrom;
    private Date dateTo;
    private int offset = 0;
    private int limit = 500;
    private Class<?> mambuEntityClass = MambuEntityType.CLIENT.getEntityClass();
    private String mambuEntityIdParameterName = APIData.CLIENT_ID;
    private String mambuEntityId = "AFDJSKFJSDKFJKS";

    @Override
    public void setUp() throws MambuApiException {
        super.setUp();

        service = new ActivitiesService(super.mambuApiService);
        
        try {
            dateTo = sdf.parse(dateToString);
            dateFrom = sdf.parse(dateFromString);
        } catch (ParseException e) {
            //ignore
        }
    }
    
    @Test
    public void testActivitiesDateRange() throws MambuApiException {
        // execute
        service.getActivities(dateFrom, dateTo);

        // verify
        ParamsMap params = new ParamsMap();
        params.put(APIData.FROM, dateFromString);
        params.put(APIData.TO, dateToString);

        Mockito.verify(executor).executeRequest(ACTIVITIES_ENDPOINT, params,
                RequestExecutor.Method.GET,
                RequestExecutor.ContentType.WWW_FORM);
    }

    @Test
    public void testActivitiesDateRangeAndPagination() throws MambuApiException {
        // execute
        service.getActivities(dateFrom, dateTo, offset, limit);

        // verify
        ParamsMap params = new ParamsMap();
        params.put(APIData.FROM, dateFromString);
        params.put(APIData.TO, dateToString);
        params.put(APIData.OFFSET, Integer.toString(offset));
        params.put(APIData.LIMIT, Integer.toString(limit));

        Mockito.verify(executor).executeRequest(ACTIVITIES_ENDPOINT, params,
                RequestExecutor.Method.GET,
                RequestExecutor.ContentType.WWW_FORM);
    }

    @Test
    public void testActivitiesDateRangeAndMambuEntity() throws MambuApiException {

        // execute
        service.getActivities(dateFrom, dateTo, mambuEntityClass, mambuEntityId);

        // verify
        ParamsMap params = new ParamsMap();
        params.put(APIData.FROM, dateFromString);
        params.put(APIData.TO, dateToString);
        params.put(mambuEntityIdParameterName, mambuEntityId);

        Mockito.verify(executor).executeRequest(ACTIVITIES_ENDPOINT, params,
                RequestExecutor.Method.GET,
                RequestExecutor.ContentType.WWW_FORM);
    }

    @Test
    public void testActivitiesDateRangeAndMambuEntityAndPagination() throws MambuApiException {

        // execute
        service.getActivities(dateFrom, dateTo, mambuEntityClass, mambuEntityId, offset, limit);

        // verify
        ParamsMap params = new ParamsMap();
        params.put(APIData.FROM, dateFromString);
        params.put(APIData.TO, dateToString);
        params.put(mambuEntityIdParameterName, mambuEntityId);
        params.put(APIData.OFFSET, Integer.toString(offset));
        params.put(APIData.LIMIT, Integer.toString(limit));

        Mockito.verify(executor).executeRequest(ACTIVITIES_ENDPOINT, params,
                RequestExecutor.Method.GET,
                RequestExecutor.ContentType.WWW_FORM);
    }
    
}
