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
import org.olf.rs.mockncip.data.LoanMapper;
import org.olf.rs.mockncip.models.Loan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

@Path("/loans")
public class LoanService  extends VerificationService {

	@Context
	private HttpServletRequest servletRequest;
	final Logger logger = LogManager.getLogger(LoanService.class);

	@GET
	@Path("/patron/{patronBarcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response openLoanByPatronBarcode(@PathParam("patronBarcode") String patronBarcode) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
			List<Loan> loans = loanMapper.getOpenLoansByPatron(patronBarcode);
			if (loans == null) throw new Exception("no loans found");
			String json = constructReturnString(loans);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get all loans: ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/item/{itemBarcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response openLoanByItemBarcode(@PathParam("itemBarcode") String itemBarcode) throws IOException {

		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
			Loan loan = loanMapper.getOpenLoanByItemBarcode(itemBarcode);
			if (loan == null) {
				throw new Exception("no loans found for barcode: "  + itemBarcode);
			}
			String json = constructReturnString(loan);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get item / barcode not found: ", e);
			return Response.serverError().entity(errorMessage).build();
		}

	}


	@GET
	@Path("/open")
	@Produces(MediaType.TEXT_PLAIN)
	public Response test() throws IOException {
		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
			List<Loan> loans = loanMapper.getOpenLoans();
			String json = constructReturnString(loans);
			return Response.ok().entity(json).build();
		} catch (Exception e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get open loans: ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response allLoans() throws Exception {
		try {
			Connection dbConnection = (Connection) servletRequest.getServletContext().getAttribute("db");
			SqlSession mysqlsession = getSession(dbConnection);
			LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
			List<Loan> loans = loanMapper.getAllLoans();
			String json = constructReturnString(loans);
			return Response.ok().entity(json).build();
		} catch (JsonProcessingException e) {
			logger.fatal(e.getLocalizedMessage());
			String errorMessage = errorAsJsonString("unable to get all loans: ", e);
			return Response.serverError().entity(errorMessage).build();
		}
	}

}
