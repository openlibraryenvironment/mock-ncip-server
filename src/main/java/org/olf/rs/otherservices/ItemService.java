package org.olf.rs.otherservices;

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
import org.olf.rs.mockncip.data.ItemMapper;
import org.olf.rs.mockncip.models.Item;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;


@Path("/items")
public class ItemService extends VerificationService {

	@Context
	private HttpServletRequest servletRequest;
	final Logger logger = LogManager.getLogger(ItemService.class);

	@GET
	@Path("/{itemBarcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response openRequestByItemBarcode(@PathParam("itemBarcode") String itemBarcode) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
			Item item = itemMapper.getItemByBarcode(itemBarcode);
			if (item == null)
				throw new Exception("no item found");
			String json = constructReturnString(item);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.toString());
			String errorMessage = errorAsJsonString("unable to find a loan for item barcode", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/available")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAllAvailableItem() throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
			List<Item> items = itemMapper.getAvailableItems();
			return Response.ok().entity(constructReturnString(items)).build();
		} catch (Exception e) {
			logger.fatal(e.toString());
			String errorMessage = errorAsJsonString("unable to get available items ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/loaned")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAllLoanedItem() throws Exception {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
			List<Item> items = itemMapper.getLoanItems();
			return Response.ok().entity(constructReturnString(items)).build();
		} catch (JsonProcessingException e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get loaned items ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAllItems() throws Exception {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
			List<Item> items = itemMapper.getAllItems();
			return Response.ok().entity(constructReturnString(items)).build();
		} catch (JsonProcessingException e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get items ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

}
