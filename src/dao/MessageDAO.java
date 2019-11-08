package dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.*;
import com.cloudant.client.api.query.Expression;

@Stateless
@LocalBean
public class MessageDAO {
	private CloudantClient client = null;
	private Database db = null;
	private static final String CLASS_NAME = MessageDAO.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	@PostConstruct
	public void init() {
		String url = null;
		String iam = null;
		try {
			InputStream inputStream = getClass().getResourceAsStream("/META-INF/customercare.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			url = properties.getProperty("db_URL");
			iam = properties.getProperty("db_apikey");
		} catch (IOException e2) {
			LOGGER.severe("Can't load .properties file");
		}
		//try {
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			LOGGER.fine(VCAP_SERVICES);
			/*
			JSONObject vcap = (JSONObject) JSONObject.parse(VCAP_SERVICES);
			JSONArray cloudant = (JSONArray) vcap.get("cloudantNoSQLDB");
			JSONObject cloudantInstance = (JSONObject) cloudant.get(0);
			JSONObject cloudantCredentials = (JSONObject) cloudantInstance.get("credentials");
			url = (String) cloudantCredentials.get("url");
			iam = (String) cloudantCredentials.get("apikey");
		} catch (IOException e) {// 
			LOGGER.severe("Can't load VCAP_SERVICES");
		}*/
		try {
			client = ClientBuilder.url(new URL(url)).iamApiKey(iam).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = client.database("messagedb", false);

		LOGGER.logp(Level.FINEST, CLASS_NAME, "init():", "Persisted");
	}

	

	public String getMessage(String id, String lang) {
		QueryResult<Message> msg = db.query(new QueryBuilder( Operation.and(Expression.eq("id", id),Expression.eq("lang", lang))).fields("text").build(),
				Message.class);
		List<Message> docs = msg.getDocs();
		if (docs.isEmpty()) return "";
		return docs.get(0).text;
	}
	class Message{
		String text;
	}
	
}
