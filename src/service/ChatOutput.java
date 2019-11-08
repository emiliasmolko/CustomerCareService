package service;

import com.ibm.watson.assistant.v1.model.MessageResponse;

public class ChatOutput {
	/**
	 * 
	 */
	public Context context;
	public Output output;

	
	public ChatOutput(MessageResponse mr) {		
		context = new Context(mr.getContext().getConversationId());
		output = new Output(mr.getOutput());
	}


	public String getConversationId() {
		if(context!=null) {
			return context.conversation_id;
		}
		return "0";
	}

}
