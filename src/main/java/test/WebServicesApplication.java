package test;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

public class WebServicesApplication extends ResourceConfig {
	
	public WebServicesApplication() {
		packages("test");
		register(PatronService.class);
		register(LoanService.class);
	}

}
