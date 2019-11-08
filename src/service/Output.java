package service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.watson.assistant.v1.model.OutputData;
import com.ibm.watson.assistant.v1.model.RuntimeResponseGeneric;

public class Output {
	
	public Output(OutputData output) {
		generic = new ArrayList<Generic>();
		//generic = output.getGeneric();
		RuntimeResponseGeneric x = null;
		for(Iterator<RuntimeResponseGeneric> i = output.getGeneric().iterator();i.hasNext();) {
			x = i.next();
			generic.add(new Generic(x.text(), x.responseType()));
		}
		text = output.getText();		
	}

	public List<Generic> generic;
	public List<String> text;

}
