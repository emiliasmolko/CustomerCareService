package service;

public class ChatInput {
	/**
	 * 
	 */
	public Context context;
	public Input input;

	
	public ChatInput() {		
		context = new Context("0");
	}


	public String getConversationId() {
		if(context!=null) {
			return context.conversation_id;
		}
		return "0";
	}


	public String getText() {
		if(input !=null) {
			return input.text;
		}
		return null;
	}

}
