package atos.mae.auto.hpalm;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.hpalm.infrastructure.Authentication;
import atos.mae.auto.hpalm.infrastructure.Response;
import atos.mae.auto.hpalm.infrastructure.RestConnector;
import atos.mae.auto.hpalm.model.TestEntity;
import atos.mae.auto.hpalm.model.TestFieldEntity;
import atos.mae.auto.hpalm.model.TestValueEntity;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.model.TestStepModel;

@Component
public class HpAlm {

	/**
	 * Logger.
	 */
	private static Logger Log = LoggerFactory.getLogger(HpAlm.class);


	@Value("${hpalmUrl:}")
	public String URL;

	public final String URLAuth = "authentication-point/authenticate";

	@Value("${hpalmUsername:}")
	public String USERNAME;
	@Value("${hpalmPassword:}")
	public String PASSWORD;


	@Value("${hpalmDomain:}")
	public String DOMAIN;

	@Value("${hpalmProject:}")
	public String PROJECT;

	@Value("${hpalmIdRepository:}")
	public String PARENTID;

	private RestConnector connector;

	private Map<String, String> cookiesMapSession;

	private Map<String,String> headerJsonPost;
	private Map<String,String> headerJsonGet;
	private Map<String,String> headerXml;

	private String testRunIdForStep;

	@Autowired
	private Authentication auth;

	private boolean isReportingEnable = true;

	public boolean connect(){

		if(!this.isReportingEnable)
			return false;

		// if information missing
		if(URL == null || URL.trim().isEmpty()
		|| USERNAME == null || USERNAME.trim().isEmpty()
		|| PASSWORD == null || PASSWORD.trim().isEmpty()
		|| DOMAIN == null || DOMAIN.trim().isEmpty()
		|| PROJECT == null || PROJECT.trim().isEmpty()
		|| PARENTID == null || PARENTID.trim().isEmpty())
		    return false;

		if(this.connector == null){
			this.connector = RestConnector.getInstance().init(
	                        new HashMap<String, String>(),
	                        this.URL,
	                        this.DOMAIN,
	                        this.PROJECT);
		}
		boolean isAuthenticated = false;
		try {
			String authURL = this.auth.isAuthenticated();
			//String authURL =  this.connector.buildUrl(this.URLAuth);
			if(authURL == null)
				return true;

			 isAuthenticated =  this.auth.login(authURL, this.USERNAME, this.PASSWORD);

			 if(!isAuthenticated){
				 Log.warn("login failed to HpAlm : login and/or password are incorrect");
				 return false;
			 }

			//Session Initialisation
	        String cookiesSession =  this.auth.siteSession();

	        //Recuperation des cookies en HashMAP
	        this.cookiesMapSession =  this.auth.cookieSession();

	        this.headerJsonPost = new HashMap<String, String>();
	        this.headerJsonPost.put("Content-Type", "application/json");
	        this.headerJsonPost.put("Accept", "application/json");

	        this.headerJsonGet = new HashMap<String, String>();
	        this.headerJsonGet.put("Content-Type", "application/json");
	        this.headerJsonGet.put("Accept", "application/json");

	        this.headerXml = new HashMap<String, String>();
	        this.headerXml.put("Content-Type", "application/xml");
	        this.headerXml.put("Accept", "application/xml");

		} catch (Exception e) {
			Log.error("login failed to HpAlm.",e);
			return false;
		}
		return isAuthenticated;
	}

	public void disconnect(){
		// CPD try to prevent error HPALM At the end of test when HPALM is not use
		if(this.connect()){
			try {
				this.auth.logout();
			} catch (Exception e) {
				Log.error("Error while disconnecting : ",e);
			}
		}
	}

	public TestFieldEntity createField(String name, String value){
		final TestFieldEntity tfe = new TestFieldEntity(name);
		if(value == null)
			tfe.getValues().add(new TestValueEntity(""));
		else
			tfe.getValues().add(new TestValueEntity(value));
		return tfe;
	}

	public void createTestRunHierarchy(TestStepModel tsm) {
		String testId = this.searchTest(tsm.getTestName().split(".xlsm")[0]);
		if(testId == null)
			testId = this.createTest(tsm);

		Log.info("HPALM Test ID : " + testId);

		String testSetId = this.searchTestSet(tsm.getTestName().split(".xlsm")[0]);
		if(testSetId == null)
			testSetId = this.createTestSet(tsm);

		Log.info("HPALM Test set ID : " + testSetId);

		String testInstanceId = this.searchTestInstance(testSetId);
		if(testInstanceId == null)
			testInstanceId = this.createTestInstance(testId,testSetId);

		Log.info("HPALM Test instance ID : " + testInstanceId);

		this.testRunIdForStep = this.createRun(tsm,testInstanceId,testId);

		Log.info("HPALM Test run ID : " + this.testRunIdForStep);
	}

	public String getTests(int startIndex){
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;
		Response response = null;
		try {
			response = this.connector.httpGet(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/tests?start-index=%3$s", this.DOMAIN, this.PROJECT,String.valueOf(startIndex))),null, this.headerJsonGet);

		} catch (Exception e) {
			Log.error("Error while getting tests : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return response.toString();
	}

	public String createTest(TestStepModel tsm){
		this.isReportingEnable = true;
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		TestEntity te = new TestEntity();
		te.getFields().add(this.createField("name", tsm.getTestName().split(".xlsm")[0]));
		te.getFields().add(this.createField("description", tsm.getTestDescription()));
		te.getFields().add(this.createField("owner", this.USERNAME));
		te.getFields().add(this.createField("subtype-id", "MANUAL"));
		te.getFields().add(this.createField("parent-id", this.PARENTID));

		String test = new Gson().toJson(te);

		Response response = null;
		try {
			response = this.connector.httpPost(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/tests", this.DOMAIN, this.PROJECT)), test.getBytes(), this.headerJsonPost);
		} catch (Exception e) {
			Log.error("Error while Creating a test : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		String id = this.getIdFromEntity(response.toString());

		return id;

	}

	public String searchTest(String name){
		if(!this.isReportingEnable)
			return null;
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		String id = null;
		int index = 1;
		String idOrResult = null;
		do{
			idOrResult = this.getIdFromCollection(this.getTests(index), "name", name);
			if(Integer.parseInt(idOrResult) > 0)
				id = idOrResult;
			index += 100;
		}while(index < (Integer.parseInt(idOrResult) * -1));
		return id;
	}



	/*
	 *
	 * Test set
	 *
	 */

	public String getTestSet(int startIndex){
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;
		Response response = null;
		try {
			response = this.connector.httpGet(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/test-sets?start-index=%3$s", this.DOMAIN, this.PROJECT,startIndex)), null, headerJsonGet);
		} catch (Exception e) {
			Log.error("Error while getting tests set : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return response.toString();
	}

	public String createTestSet(TestStepModel tsm){
		if(!this.isReportingEnable)
			return null;
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		TestEntity te = new TestEntity();

		te.getFields().add(this.createField("name", tsm.getTestName().split(".xlsm")[0]));
		te.getFields().add(this.createField("parent-id", "8"));
		te.getFields().add(this.createField("subtype-id", "hp.qc.test-set.default"));
		te.getFields().add(this.createField("status", "Open"));

		String jsonEntity = new Gson().toJson(te);

		//String checkoutcomment = "<CheckOutParameters><Comment>Test check-out with REST.</Comment></CheckOutParameters>";
		Response response = null;
		try {
			response = this.connector.httpPost(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/test-sets", this.DOMAIN, this.PROJECT)), jsonEntity.getBytes(), this.headerJsonPost);
		} catch (Exception e) {
			Log.error("Error while Creating a test set : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return this.getIdFromEntity(response.toString());
	}

	public String searchTestSet(String name){
		if(!this.isReportingEnable)
			return null;
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		String id = null;
		int index = 1;
		String idOrResult = null;
		do{
			idOrResult = this.getIdFromCollection(this.getTestSet(index), "name", name);
			if(Integer.parseInt(idOrResult) > 0)
				id = idOrResult;
			index += 100;
		}while(index < (Integer.parseInt(idOrResult) * -1));
		return id;
	}

	/*
	 *
	 * Test set instance
	 *
	 */

	public String getTestInstances(int startIndex){
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;
		Response response = null;
		try {
			response = this.connector.httpGet(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/test-instances?start-index=%3$s", this.DOMAIN, this.PROJECT,startIndex)), null, headerJsonGet);

		} catch (Exception e) {
			Log.error("Error while getting tests set : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return response.toString();
	}

	public String createTestInstance(String testId, String testSetId){
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		TestEntity te = new TestEntity();

		te.getFields().add(this.createField("owner", this.USERNAME));
		te.getFields().add(this.createField("cycle-id", testSetId));
		te.getFields().add(this.createField("test-id", testId));
		te.getFields().add(this.createField("test-order", "1"));
		te.getFields().add(this.createField("subtype-id", "hp.qc.test-instance.MANUAL"));

		String jsonEntity = new Gson().toJson(te);
		Response response = null;
		try {
			response = this.connector.httpPost(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/test-instances", this.DOMAIN, this.PROJECT)), jsonEntity.getBytes(), headerJsonPost);

		} catch (Exception e) {
			Log.error("Error while Creating a test set : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return this.getIdFromEntity(response.toString());
	}

	public String searchTestInstance(String testSetId){
		if(!this.isReportingEnable)
			return null;
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		String id = null;
		int index = 1;
		String idOrResult = null;
		do{
			idOrResult = this.getIdFromCollection(this.getTestInstances(index), "cycle-id", testSetId);
			if(Integer.parseInt(idOrResult) > 0)
				id = idOrResult;
			index += 100;
		}while(index < (Integer.parseInt(idOrResult) * -1));
		return id;
	}
	/*
	 *
	 * Test run
	 *
	 */

	public String createRun(TestStepModel tsm, String testInstanceId, String testId){
		boolean isConnected = this.connect();
		if(!isConnected)
			return null;

		TestEntity te = new TestEntity();
		te.getFields().add(this.createField("name", tsm.getTestName().split(".xlsm")[0]));
		te.getFields().add(this.createField("testcycl-id", testInstanceId));
		te.getFields().add(this.createField("owner", this.USERNAME));
		te.getFields().add(this.createField("test-id", testId));
		te.getFields().add(this.createField("subtype-id", "hp.qc.run.MANUAL"));
		te.getFields().add(this.createField("status", "No Run"));

		String jsonEntity = new Gson().toJson(te);
		Response response = null;
		try {
			response = this.connector.httpPost(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/runs", this.DOMAIN, this.PROJECT)), jsonEntity.getBytes(), headerJsonPost);

		} catch (Exception e) {
			Log.error("Error while Creating a run : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
		return this.getIdFromEntity(response.toString());
	}

	/*
	 *
	 * Test run step
	 *
	 */

	public void addRunStep(StepModel sm){
		if(!this.isReportingEnable)
			return;
		boolean isConnected = this.connect();
		if(!isConnected)
			return;

		TestEntity te = new TestEntity();
		te.getFields().add(this.createField("name", sm.getName()));
		te.getFields().add(this.createField("description", sm.getStepDescription()));

		switch(sm.getStepReturn().getStepReturn()){
		case PASS:
			te.getFields().add(this.createField("status", "Passed"));
			break;
		case WARN:
			te.getFields().add(this.createField("status", "Failed"));
			break;
		case FAIL:
			if(sm.isAbortOnFail())
				te.getFields().add(this.createField("status", "Blocked"));
			else
				te.getFields().add(this.createField("status", "Failed"));
			break;
		case ERROR:
			if(sm.isAbortOnFail())
				te.getFields().add(this.createField("status", "Blocked"));
			else
				te.getFields().add(this.createField("status", "Failed"));
			break;
		default:
			break;
		}

		te.getFields().add(this.createField("expected",sm.getStepReturn().getExpected()));
		te.getFields().add(this.createField("actual", String.format("%1$s\r\n%2$s", sm.getStepReturn().getActual(),sm.getStepReturn().getInformation())));

		String jsonEntity = new Gson().toJson(te);
		Response response = null;
		try {
			response = this.connector.httpPost(this.connector.buildUrl(String.format("rest/domains/%1$s/projects/%2$s/runs/%3$s/run-steps", this.DOMAIN, this.PROJECT,this.testRunIdForStep)), jsonEntity.getBytes(), this.headerJsonPost);
		} catch (Exception e) {
			Log.error("Error while adding a run step : " + e);
			//Error, stop hpalm reporting
			this.isReportingEnable = false;
		}
	}



	/*
	 *
	 * Utils
	 *
	 */

	public String getIdFromCollection(String jsonCollection, String keyToCheck, String valueExpected) throws JSONException{
		try{
			JSONObject jsonObject =new JSONObject(jsonCollection);
			JSONArray  jsonEntities = jsonObject.getJSONArray("entities");
			String jsonEntity = null;
			for (Object entity : jsonEntities) {
		        JSONObject jsonItemsEntity = (JSONObject) entity;
		        JSONArray  jsonFields = jsonItemsEntity.getJSONArray("Fields");
		        for (Object fields : jsonFields) {
		        	JSONObject jsonItemsFields = (JSONObject) fields;
			        String key = jsonItemsFields.getString("Name");

			        if(key.compareTo(keyToCheck) == 0){
			        	JSONArray values = jsonItemsFields.getJSONArray("values");
			        	JSONObject value = (JSONObject)values.get(0);
			        	if(value.getString("value").compareTo(valueExpected) == 0)
			        		jsonEntity = jsonItemsEntity.toString();

			        }
		        }
		    }
			if(jsonEntity == null){
				Long total = jsonObject.getLong("TotalResults");
				return String.format("-%1$s", String.valueOf(total));
			}
			return this.getIdFromEntity(jsonEntity);
		}catch(JSONException e){
			Log.error("Error while reading hp alm api result : " + e);
			return null;
		}
	}

	public String getIdFromEntity(String jsonString) throws JSONException{
		try{
			JSONObject jsonObject =new JSONObject(jsonString);
			JSONArray  jsonFields = jsonObject.optJSONArray("Fields");
			if(jsonFields == null)
				return null;
			String id = null;
			for (Object o : jsonFields) {
		        JSONObject jsonLineItem = (JSONObject) o;
		        String key = jsonLineItem.getString("Name");

		        if(key.compareTo("id") == 0){
		        	JSONArray values = jsonLineItem.getJSONArray("values");
		        	JSONObject value = (JSONObject)values.get(0);
		        	id = value.getString("value");
		        }
		    }
			return id;
		}catch(JSONException e){
			Log.error("Error while reading hp alm api result : " + e);
			return null;
		}
	}
}
