package org.olf.rs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.AuthenticationInput;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemResponseData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemService;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.ItemIdentifierType;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.ServiceContext;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
import org.json.JSONObject;

public class MockCheckOutItemService extends MockNcipService implements CheckOutItemService {

	private static final Logger logger = Logger.getLogger(MockCheckOutItemService.class);
	
	
	 public CheckOutItemResponseData performService(CheckOutItemInitiationData initData,
			 ServiceContext serviceContext,
             RemoteServiceManager serviceManager) {
		
		CheckOutItemResponseData checkOutItemResponseData = new CheckOutItemResponseData();
		
		ItemId itemId = initData.getItemId();
		UserId userId = retrieveUserId(initData);
    	Calendar cal = new GregorianCalendar();
        try {
        	validateUserId(userId);
        	validateItemId(itemId);
        }
        catch(Exception exception) {
        	if (checkOutItemResponseData.getProblems() == null) checkOutItemResponseData.setProblems(new ArrayList<Problem>());
        	Problem p = new Problem(new ProblemType(Constants.CHECK_OUT_PROBLEM),Constants.CHECK_OUT_PROBLEM,exception.getMessage(),exception.getMessage());
        	checkOutItemResponseData.getProblems().add(p);
        	return checkOutItemResponseData;
        } 

        //ATTEMPT TO DETERMINE AGENCY ID
        //INITIATION HEADER IS NOT REQUIRED
        String requesterAgencyId = null;
        try {
        	requesterAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
    		if (requesterAgencyId == null || requesterAgencyId.trim().equalsIgnoreCase(""))
    			throw new Exception("From Agency ID Missing");
        }
        catch(Exception e) {
        	//cannot get requester agency id from init header - try request id element
        	try {
        		requesterAgencyId = initData.getRequestId().getAgencyId().getValue();
        		if (requesterAgencyId == null || requesterAgencyId.trim().equalsIgnoreCase(""))
        			throw new Exception("From Agency ID Missing");
        	}
        	catch(Exception except) {
        		logger.error("Could not determine agency id from initiation header or request id element.  Using default");
        		if (checkOutItemResponseData.getProblems() == null) checkOutItemResponseData.setProblems(new ArrayList<Problem>());
            	Problem p = new Problem(new ProblemType(Constants.CHECK_OUT_PROBLEM),Constants.AGENCY_ID,Constants.CHECK_OUT_PROBLEM ,e.getMessage());
            	checkOutItemResponseData.getProblems().add(p);
            	return checkOutItemResponseData;
        	}
        	
        }
        
        GregorianCalendar calendar = new GregorianCalendar();		
		
        try {
        	//THE SERVICE MANAGER CALLS THE OKAPI APIs
        	JSONObject checkOutItemResponseDetails = ((MockRemoteServiceManager)serviceManager).checkOut(initData,requesterAgencyId.toLowerCase());
        	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        	String dueDate = checkOutItemResponseDetails.getString("dueDate");
        	Date date = dateFormat.parse(dueDate);
        	cal.setTime(date);
        	
         }
        catch(Exception  e) {
        	if (checkOutItemResponseData.getProblems() == null) checkOutItemResponseData.setProblems(new ArrayList<Problem>());
        	Problem p = new Problem(new ProblemType(Constants.CHECK_OUT_PROBLEM),Constants.UNKNOWN_DATA_ELEMENT,Constants.CHECK_OUT_PROBLEM ,e.getMessage());
        	checkOutItemResponseData.getProblems().add(p);
        	return checkOutItemResponseData;
        }
        
		//construct ItemId for Response
		ItemIdentifierType idemIdentiferType = new ItemIdentifierType(Constants.SCHEME,Constants.ITEM_BARCODE);
		ItemId iId = new ItemId();
		iId.setAgencyId(new AgencyId(requesterAgencyId));
		iId.setItemIdentifierType(idemIdentiferType);
		iId.setItemIdentifierValue(itemId.getItemIdentifierValue());
		
		//construct UserId for Response
		UserIdentifierType userIdentiferType = new UserIdentifierType(Constants.SCHEME, Constants.USER_BARCODE);
		UserId uId = new UserId();
		uId.setAgencyId(new AgencyId(requesterAgencyId));
		uId.setUserIdentifierType(userIdentiferType);
		uId.setUserIdentifierValue(userId.getUserIdentifierValue());
		
		CheckOutItemResponseData responseData = new CheckOutItemResponseData();
		responseData.setDateDue((GregorianCalendar) cal);
		responseData.setItemId(iId);
		responseData.setUserId(uId);
		
		return responseData;
	 }
	
	 private String retrieveAuthenticationInputTypeOf(String type,CheckOutItemInitiationData initData) {
	    	if (initData.getAuthenticationInputs() == null) return null;
	    	String authenticationID = null;
	    	for (AuthenticationInput authenticationInput : initData.getAuthenticationInputs()) {
	    		if (authenticationInput.getAuthenticationInputType().getValue().equalsIgnoreCase(type)) {
	    			authenticationID = authenticationInput.getAuthenticationInputData();
	    			break;
	    		}
	    	}
	    	if (authenticationID != null && authenticationID.equalsIgnoreCase("")) authenticationID = null;
	    	return authenticationID;
	 }
	 
	private UserId retrieveUserId(CheckOutItemInitiationData initData) {
	    	UserId uid = null;
	    	String uidString = null;
	    	if (initData.getUserId() != null) {
	    		uid = initData.getUserId();
	    	}
	    	else {
	    		//TRY Barcode Id
	    		uidString = this.retrieveAuthenticationInputTypeOf(Constants.AUTH_BARCODE,initData);
	    		//TRY User Id
	    		if (uidString == null) {
	    			uidString = this.retrieveAuthenticationInputTypeOf(Constants.AUTH_UID, initData);
	    		}
	    	}
	    	if (uidString != null) {
	    		uid = new UserId();
	    		uid.setUserIdentifierValue(uidString);
	    	}
	    	return uid;
	}

}
