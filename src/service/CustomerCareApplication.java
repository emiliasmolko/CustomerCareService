package service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class CustomerCareApplication extends Application {
	
	public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        
        // Resources
        classes.add( ChatService.class );
        //Loaders
        return classes;
    }
	
}
