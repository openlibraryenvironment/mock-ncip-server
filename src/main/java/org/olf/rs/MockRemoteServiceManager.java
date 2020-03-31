package org.olf.rs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONObject;
import org.olf.rs.mockncip.data.ItemMapper;
import org.olf.rs.mockncip.data.LoanMapper;
import org.olf.rs.mockncip.data.PatronMapper;
import org.olf.rs.mockncip.data.RequestMapper;
import org.olf.rs.mockncip.models.Item;
import org.olf.rs.mockncip.models.Loan;
import org.olf.rs.mockncip.models.Patron;
import org.olf.rs.mockncip.models.Request;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.ItemId;

import java.time.LocalDateTime;

public class MockRemoteServiceManager implements RemoteServiceManager {

	private static final Logger logger = Logger.getLogger(MockRemoteServiceManager.class);

	private Connection dbConnection;

	public Connection getDbConnection() {
		return dbConnection;
	}

	public void setDbConnection(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public MockRemoteServiceManager() throws Exception {

	}

	public MockRemoteServiceManager(Properties properties) {

	}

	public JSONObject acceptItem(AcceptItemInitiationData initData, String agencyId) throws Exception {

		SqlSession mysqlsession = null;
		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
		mysqlsession = mysqlsqlSessionFactory.openSession(dbConnection);

		// DOES THE PATRON EXIST?
		String patronBarcode = initData.getUserId().getUserIdentifierValue();
		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
		Patron existingPatron = patronMapper.getPatronByBarcode(patronBarcode);
		if (existingPatron == null) {
			throw new Exception("Patron with that barcode does not exist");
		}

		// MAKE SURE THE ITEM BARCODE DOES NOT ALREADY EXIST
		ItemId itemId = initData.getItemId();
		String itemBarcode = itemId.getItemIdentifierValue();
		ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
		Item existingItem = itemMapper.getItemByBarcode(itemBarcode);
		if (existingItem != null) {
			throw new Exception("Item with that barcode (" + itemBarcode + ") already exists.");
		}

		// GENERATE UUIDS FOR OBJECTS
		UUID requestUuid = UUID.randomUUID();
		UUID itemUuid = UUID.randomUUID();

		// INSERT ITEM
		String itemTitle = retreiveItemTitle(initData);
		String itemAuthor = retreiveItemTitle(initData);

		String requestId = initData.getRequestId().getRequestIdentifierValue();
		String pickupLocation = initData.getPickupLocation().getValue();
		Item item = new Item();
		item.setTitle(itemTitle);
		item.setAuthor(itemAuthor);
		item.setId(itemUuid.toString());
		item.setItemBarcode(itemBarcode);
		item.setStatus("AVAILABLE");
		item.setCallNumber(itemBarcode);
		// INSERT REQUEST
		Request request = new Request();
		request.setId(requestUuid.toString());
		request.setItemBarcode(itemBarcode);
		request.setPatronBarcode(patronBarcode);
		request.setPickupLocation(pickupLocation);
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String dateString = dateFormat.format(date);
		request.setRequestDate(dateString);

		RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
		try {
			requestMapper.insertNewRequest(request);
			itemMapper.insertNewItem(item);
		} catch (Exception e) {
			throw e;
		}

		JSONObject returnValues = new JSONObject();
		returnValues.put("id", requestUuid.toString());
		return returnValues;
	}

	public JSONObject checkIn(CheckInItemInitiationData initData, String agencyId) throws Exception {

		// FIND THE LOAN
		SqlSession mysqlsession = null;
		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
		mysqlsession = mysqlsqlSessionFactory.openSession(dbConnection);

		String itemBarcode = initData.getItemId().getItemIdentifierValue();
		LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);
		Loan loan = loanMapper.getOpenLoanByItemBarcode(itemBarcode);

		List<Loan> currentLoans = loanMapper.getAllLoans();

		// OPEN LOAN DOESN'T EXIST - JUST RETURN
		if (loan == null)
			return new JSONObject();

		// CLOSE THE OPEN LOAN
		loanMapper.closeLoan(loan);

		// MAKE THE ITEM AVAILABLE
		ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
		itemMapper.setItemToAvailableByBarcode(itemBarcode);

		ObjectMapper objectMapper = new ObjectMapper();
		String loanAstString = objectMapper.writeValueAsString(loan);
		JSONObject returnValue = new JSONObject(loanAstString);

		return returnValue;
	}

	public JSONObject checkOut(CheckOutItemInitiationData initData, String agencyId) throws Exception {

		SqlSession mysqlsession = null;
		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
		mysqlsession = mysqlsqlSessionFactory.openSession(dbConnection);

		String itemBarcode = initData.getItemId().getItemIdentifierValue();
		String userBarcode = initData.getUserId().getUserIdentifierValue();
		UUID id = UUID.randomUUID();

		// DOES THE PATRON EXIST?
		String patronBarcode = initData.getUserId().getUserIdentifierValue();
		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
		Patron existingPatron = patronMapper.getPatronByBarcode(patronBarcode);
		if (existingPatron == null) {
			throw new Exception("Patron with that barcode does not exist");
		}

		// MAKE SURE THE ITEM BARCODE EXISTS SO IT CAN BE LOANED
		ItemMapper itemMapper = mysqlsession.getMapper(ItemMapper.class);
		Item existingItem = itemMapper.getItemByBarcode(itemBarcode);
		if (existingItem == null) {
			throw new Exception("Item with barcode (" + itemBarcode + ") does not exists.");
		}
		// MAKE SURE IT IS NOT LOANED ALREADY
		if (existingItem.getStatus().equalsIgnoreCase("LOANED")) {
			throw new Exception("Item with barcode (" + itemBarcode + ") is already loaned.");
		}
		// MAKE SURE NO OTHER PATRON HAS THIS ITEM ON HOLD
		RequestMapper requestMapper = mysqlsession.getMapper(RequestMapper.class);
		Request otherRequest = requestMapper.getOpenRequestByItemAndOtherPatron(itemBarcode, patronBarcode);
		if (otherRequest != null) {
			throw new Exception("Item with barcode (" + itemBarcode + ") is on hold for another patron.");
		}

		Date currentDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		LocalDate date = LocalDate.now();
		date = date.plusDays(90);
		Date dueDate = java.sql.Date.valueOf(date);

		LoanMapper loanMapper = mysqlsession.getMapper(LoanMapper.class);

		Loan loan = new Loan();
		loan.setDueDate(dateFormat.format(dueDate));
		loan.setId(id.toString());
		loan.setItemBarcode(itemBarcode);
		loan.setPatronBarcode(userBarcode);
		loanMapper.insertNewLoan(loan);

		// SET ITEM TO LOANED
		itemMapper.setItemToLoanedByBarcode(itemBarcode);
		// SET REQUEST TO CLOSED
		requestMapper.setRequestToClosed(itemBarcode, userBarcode);

		ObjectMapper objectMapper = new ObjectMapper();
		String patronAsString = objectMapper.writeValueAsString(loan);
		JSONObject returnValue = new JSONObject(patronAsString);
		return returnValue;
	}

	public JSONObject lookupUser(UserId userid) throws Exception {

		SqlSession mysqlsession = null;
		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
		mysqlsession = mysqlsqlSessionFactory.openSession(dbConnection);
		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
		Patron patron = patronMapper.getPatronByBarcode(userid.getUserIdentifierValue());
		ObjectMapper objectMapper = new ObjectMapper();
		String patronAsString = objectMapper.writeValueAsString(patron);
		return new JSONObject(patronAsString);
	}

	public JSONObject lookupPatronRecordBy(String type, String value) throws Exception {
		return new JSONObject();
	}

	public JSONObject lookupPatronRecord(UserId userid) throws Exception {
		String barcode = userid.getUserIdentifierValue();
		SqlSession mysqlsession = null;
		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
		mysqlsession = mysqlsqlSessionFactory.openSession();
		PatronMapper patronMapper = mysqlsession.getMapper(PatronMapper.class);
		Patron patron = patronMapper.getPatronByBarcode(barcode);
		ObjectMapper objectMapper = new ObjectMapper();
		String patronAsString = objectMapper.writeValueAsString(patron);
		return new JSONObject(patronAsString);
	}

	private String retreiveItemTitle(AcceptItemInitiationData initData) {
		String title = "";
		try {
			title = initData.getItemOptionalFields().getBibliographicDescription().getTitle();
		} catch (Exception e) {
			// do nothing since title is optional.
			// method will return an empty string
		}
		if (title == null)
			title = "";
		return title;
	}


}