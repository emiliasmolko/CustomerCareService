package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v1.Assistant;
import com.ibm.watson.assistant.v1.model.Context;
import com.ibm.watson.assistant.v1.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v1.model.MessageInput;
import com.ibm.watson.assistant.v1.model.MessageOptions;
import com.ibm.watson.assistant.v1.model.MessageRequest;
import com.ibm.watson.assistant.v1.model.MessageResponse;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.IdentifiedLanguage;
import com.ibm.watson.language_translator.v3.model.IdentifiedLanguages;
import com.ibm.watson.language_translator.v3.model.IdentifyOptions;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.Translation;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

import dao.MessageDAO;



@ApplicationScoped
@Path("/")
@PermitAll
public class ChatService {

	private static final String CLASS_NAME = ChatService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	private String workspaceId = "";
	private Assistant service = null;

	private static HashMap<String,MessageResponse> mem = new HashMap<String, MessageResponse>();
	
	private LanguageTranslator translate;

	@Inject
	private MessageDAO db;


	@PostConstruct
	public void init() {
		Properties properties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream("/META-INF/customercare.properties");
		try {
			properties.load(inputStream);
			LOGGER.info("customercare_Conversation_apiKey="+properties.getProperty("customercare_Conversation_apiKey"));
			LOGGER.info("workspaceId="+properties.getProperty("customercare_Conversation_WorkspaceId"));
		} catch (IOException e) {
			LOGGER.severe("Can't load customercare.properties file");
		}		
		Authenticator authenticator = new IamAuthenticator(properties.getProperty("customercare_Conversation_apiKey"));
		service = new Assistant("2019-02-28", authenticator);
		service.setServiceUrl(properties.getProperty("customercare_URL"));
		workspaceId = properties.getProperty("customercare_Conversation_WorkspaceId");	
		
		authenticator = new IamAuthenticator(properties.getProperty("Translate_Key"));
		translate = new LanguageTranslator("2018-05-01", authenticator);	
		translate.setServiceUrl(properties.getProperty("Translate_URL"));
		HttpConfigOptions configOptions = new HttpConfigOptions.Builder()
				  .disableSslVerification(true)
				  .build();
		translate.configureClient(configOptions);		
		
	}

	@POST
	@Path("/message")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ChatOutput runMessage( ChatInput payload) {
		MessageRequest mr = null;
		Context context = null;
		
		MessageResponse oldMessageResponse = getMessageResponse(payload.getConversationId());
		if(oldMessageResponse != null){
			context = oldMessageResponse.getContext();
			mr = new MessageRequest.Builder().context(context).build();
			
		}
		else {
			context = new Context();
			context.put("timezone", "Europe/Warsaw");
		}
		MessageInput input = new MessageInput();
		if(payload!=null && payload.getText()!= null) {
			input.setText(translate(payload.getText()));
		}
		LOGGER.info("BEFORE" + mr);
		MessageOptions options = new MessageOptions.Builder(workspaceId).context(context).input(input).build();		
		MessageResponse response = service.message(options).execute().getResult();
		updateMessageResponse(response);
		
		LOGGER.info("AFTER" + response);
		/*ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
		String result="";
		try {
			result = mapper.writeValueAsString(new ChatOutput(response));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		*/
		return new ChatOutput(response);
	}
	public String translate(String text) {
		
		IdentifyOptions identifyOptions = new IdentifyOptions.Builder()
				  .text(text).build();
		IdentifiedLanguages languages = translate.identify(identifyOptions)
				  .execute().getResult();
		String lang = languages.getLanguages().get(0).getLanguage();
		LOGGER.info("language:"+lang);
		List <String> input = new ArrayList<String>();
		input.add(text);
		TranslateOptions translateOptions = new TranslateOptions.Builder()
		        .addText(text)
		        .modelId(lang+"-en")
		        .build();
		TranslationResult result = translate.translate(translateOptions).execute().getResult();

		List<Translation> lt = result.getTranslations();
		String t = "";
		for (Iterator<Translation> i = lt.iterator();i.hasNext();) {
			t += i.next().getTranslation();
		}
		LOGGER.info("translated:"+t);
		return t;
	}
	
	public MessageResponse getMessageResponse(String conversationId) {
		return mem.get(conversationId);		
	}
	public void updateMessageResponse(MessageResponse mr) {
		mem.put(mr.getContext().getConversationId().toString(),mr);		
	}
	
}
