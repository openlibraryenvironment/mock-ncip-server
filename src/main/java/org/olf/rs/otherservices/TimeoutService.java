package org.olf.rs.otherservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Path("/timeout")
public class TimeoutService {
	
	@Context
	private HttpServletRequest servletRequest;
	final Logger logger = LogManager.getLogger(ItemService.class);
	
	
	@POST
	@Path("/twomins")
	@Produces(MediaType.APPLICATION_XML)
	public Response mockTwoMinutes() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("checkoutNcipOneResponseError.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
        
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	
	}

}
