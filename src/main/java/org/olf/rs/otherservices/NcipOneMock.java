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

@Path("/ncipone")
public class NcipOneMock {
	
	@Context
	private HttpServletRequest servletRequest;
	final Logger logger = LogManager.getLogger(ItemService.class);
	

	@POST
	@Path("/lookupUser")
	@Produces(MediaType.APPLICATION_XML)
	public Response lookupUser() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("lookupUserNcipOneResponse.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/lookupUserError")
	@Produces(MediaType.APPLICATION_XML)
	public Response lookupUserError() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("lookupUserNcipOneResponseError.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	
	@POST
	@Path("/acceptitem")
	@Produces(MediaType.APPLICATION_XML)
	public Response acceptItem() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("acceptItemNcipOneResponse.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/acceptitemError")
	@Produces(MediaType.APPLICATION_XML)
	public Response acceptItemError() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("acceptItemNcipOneResponseError.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/checkinItem")
	@Produces(MediaType.APPLICATION_XML)
	public Response checkInItem() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("checkinItemNcipOneResponse.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/checkinItemError")
	@Produces(MediaType.APPLICATION_XML)
	public Response checkInItemError() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("checkinItemNcipOneResponseError.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/checkoutItem")
	@Produces(MediaType.APPLICATION_XML)
	public Response checkoutItem() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("checkoutNcipOneResponse.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	
	@POST
	@Path("/checkoutItemError")
	@Produces(MediaType.APPLICATION_XML)
	public Response checkoutItemError() throws IOException {
		
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("checkoutNcipOneResponseError.xml");
		String s            = new String();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while((s = br.readLine()) != null)
        {
            sb.append(s);
        }
        br.close();
		
		return Response.ok().entity(sb.toString()).header("Content-Type", "application/xml").build();
	}
	
	@POST
	@Path("/twoMinuteResponseWait")
	@Produces(MediaType.APPLICATION_XML)
	public Response mockTimeout() throws IOException {
		
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
