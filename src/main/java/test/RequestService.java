package test;

import java.io.IOException;
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
import org.olf.rs.mockncip.data.RequestMapper;
import org.olf.rs.mockncip.models.Request;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;


@Path("/requests")
public class RequestService extends VerificationService {

	@Context
	private HttpServletRequest servletRequest;
	final Logger logger = LogManager.getLogger(RequestService.class);
	
	@GET
	@Path("/patron/{patronBarcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRequestsForPatron(@PathParam("patronBarcode") String patronBarcode) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
			List<Request> requests = requestMapper.getOpenRequestsForPatron(patronBarcode);
			if (requests == null)
				throw new Exception("no requests found");
			String json = constructReturnString(requests);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get open requests for patron:", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/item/{itemBarcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response openRequestByItemBarcode(@PathParam("itemBarcode") String itemBarcode) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
			Request request = requestMapper.getOpenRequestForItem(itemBarcode);
			if (request == null)
				throw new Exception("no request found");
			String json = constructReturnString(request);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.toString());
			String errorMessage = errorAsJsonString("unable to find a request for  item", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/{requestId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestById(@PathParam("requestId") String requestId) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
			Request request = requestMapper.getRequestById(requestId);
			if (request == null) {
				throw new Exception("no request found");
			}
			String json = constructReturnString(request);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.toString());
			String errorMessage = errorAsJsonString("unable to get request for id: ", e);
			return Response.serverError().entity(errorMessage).build();
		}

	}

	@GET
	@Path("/open")
	@Produces(MediaType.TEXT_PLAIN)
	public Response openRequests() throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
			List<Request> requests = requestMapper.getAllOpenRequests();
			return Response.ok().entity(constructReturnString(requests)).build();
		} catch (Exception e) {
			logger.fatal(e.toString());
			String errorMessage = errorAsJsonString("unable to get open requests ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response allRequests() throws Exception {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
			List<Request> requests = requestMapper.getAllRequests();
			return Response.ok().entity(constructReturnString(requests)).build();
		} catch (JsonProcessingException e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get requests ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

}
