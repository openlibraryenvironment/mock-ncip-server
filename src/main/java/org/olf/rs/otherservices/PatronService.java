package org.olf.rs.otherservices;

import java.sql.Connection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.ibatis.session.SqlSession;
import org.olf.rs.mockncip.data.LoanMapper;
import org.olf.rs.mockncip.data.PatronMapper;
import org.olf.rs.mockncip.data.RequestMapper;
import org.olf.rs.mockncip.models.Patron;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;

    

    @Path("/patrons")
    public class PatronService extends VerificationService {
    	
    	@Context
    	private HttpServletRequest servletRequest;
    	final Logger logger = LogManager.getLogger(PatronService.class);
    	
    	@GET
    	@Path("/{patronBarcode}")
        @Produces(MediaType.TEXT_PLAIN)
        public Response patronByBarcode(@PathParam("patronBarcode") String patronBarcode) throws Exception {
    		
    		try {
    			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
    			SqlSession mysqlsession = getSession(dbConnection);
	    		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
	    		LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
	    		RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
	    		Patron patron = patronMapper.getPatronByBarcode(patronBarcode);
				if (patron == null) {
					logger.info("patron not found");
					throw new Exception("patron not found: "  + patronBarcode);
				}
	    		patron.setLoans(loanMapper.getOpenLoansByPatron(patronBarcode));
	    	    patron.setRequests(requestMapper.getOpenRequestsForPatron(patronBarcode));
	    		return Response.ok().entity(constructReturnString(patron)).build();
    		} catch (Exception e) {
    			logger.fatal(e.getLocalizedMessage());
    			String errorMessage = errorAsJsonString("unable to find patron with barcode: " + patronBarcode, e);
    			logger.info(errorMessage);
    			return Response.serverError().entity(errorMessage).build();
    		}
    		
    	}
    	
    	
        @GET
        @Path("/")
        @Produces(MediaType.TEXT_PLAIN)
        public Response allPatrons() throws Exception {
        	try {
        		Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
	        	SqlSession mysqlsession = getSession(dbConnection);
	    		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
	    		List<Patron> patrons = patronMapper.getAllPatrons();
	    		return Response.ok().entity(constructReturnString(patrons)).build();
    		} catch (JsonProcessingException e) {
    			logger.fatal(e.getLocalizedMessage());
    			String errorMessage = errorAsJsonString("unable to get all patrons", e);
    			return Response.serverError().entity(errorMessage).build();
    		}
        }



    }
