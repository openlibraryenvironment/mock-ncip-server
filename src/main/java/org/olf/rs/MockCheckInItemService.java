package org.olf.rs;

import java.sql.Connection;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemResponseData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemService;
import org.extensiblecatalog.ncip.v2.service.ItemDescription;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.ItemIdentifierType;
import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.ServiceContext;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
import org.olf.rs.Constants;
import org.olf.rs.MockRemoteServiceManager;

import org.json.JSONObject;

public class MockCheckInItemService extends MockNcipService implements CheckInItemService {

	private static final Logger logger = Logger.getLogger(MockCheckInItemService.class);
	

	
	public CheckInItemResponseData performService(CheckInItemInitiationData initData, ServiceContext serviceContext,
			RemoteServiceManager serviceManager) {

		CheckInItemResponseData checkInItemResponseData = new CheckInItemResponseData();
		ItemId itemId = initData.getItemId();
		logger.info("checking in " + itemId);
		Connection con = ((MockRemoteServiceManager)serviceManager).getDbConnection();

		try {
			validateItemId(itemId);
		} catch (Exception exception) {
			if (checkInItemResponseData.getProblems() == null)
				checkInItemResponseData.setProblems(new ArrayList<Problem>());
			Problem p = new Problem(new ProblemType(Constants.CHECK_IN_PROBLEM), Constants.CHECK_IN_PROBLEM,
					exception.getMessage(), Constants.CHECK_IN_PROBLEM);
			checkInItemResponseData.getProblems().add(p);
			logger.error("item validation failed");
			return checkInItemResponseData;
		}

		//ATTEMPT TO DETERMINE AGENCY ID
		// INITIATION HEADER IS NOT REQUIRED
		String requesterAgencyId = null;
		try {
			requesterAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
			if (requesterAgencyId == null || requesterAgencyId.trim().equalsIgnoreCase(""))
				throw new Exception("Agency ID could nto be determined");
		} catch (Exception e) {
			logger.error("Could not determine agency id from initiation header or request id element.  Using default");
			if (checkInItemResponseData.getProblems() == null) checkInItemResponseData.setProblems(new ArrayList<Problem>());
        	Problem p = new Problem(new ProblemType(Constants.CHECK_OUT_PROBLEM),Constants.AGENCY_ID,Constants.CHECK_OUT_PROBLEM ,e.getMessage());
        	checkInItemResponseData.getProblems().add(p);
        	return checkInItemResponseData;
		}
		

		try {
			//THE SERVICE MANAGER CALLS THE OKAPI APIs
			JSONObject checkInResponseDetails = ((MockRemoteServiceManager) serviceManager).checkIn(initData,
					requesterAgencyId.toLowerCase());
			// construct ItemId
			ItemIdentifierType idemIdentiferType = new ItemIdentifierType("Scheme", "Item Barcode");
			ItemId newItemId = new ItemId();
			newItemId.setAgencyId(new AgencyId(requesterAgencyId));
			newItemId.setItemIdentifierType(idemIdentiferType);
			newItemId.setItemIdentifierValue(itemId.getItemIdentifierValue());
			
			//CONSTRUCT "User Id" ELEMENT
			//IF THIS LOAN WAS ALREADY CHECKED IN, THE API DOES
			//NOT RETURN A "loan" AS PART OF THE RESPONSE
			try {
					
				UserIdentifierType userIdentiferType = new UserIdentifierType("Scheme", "User Barcode");
				UserId uId = new UserId();
				uId.setAgencyId(new AgencyId(requesterAgencyId));
				uId.setUserIdentifierType(userIdentiferType);
				uId.setUserIdentifierValue(checkInResponseDetails.getString("patronBarcode"));
				checkInItemResponseData.setUserId(uId);
			}
			catch(Exception e) {
				//IT'S FINE...IN CASE CHECKIN IS CALLED TWICE 
				//THERE WON'T BE PATRON DETAILS
				//MOVE ON
				
			}
			
			
			checkInItemResponseData.setItemId(newItemId);
			
			



		} catch (Exception exception) {
			if (checkInItemResponseData.getProblems() == null)
				checkInItemResponseData.setProblems(new ArrayList<Problem>());
			Problem p = new Problem(new ProblemType(Constants.CHECK_IN_PROBLEM), Constants.UNKNOWN_DATA_ELEMENT,
					Constants.CHECK_IN_PROBLEM, exception.getMessage());
			checkInItemResponseData.getProblems().add(p);
			return checkInItemResponseData;
		}
		return checkInItemResponseData;
	}

	// TODO is call number necessary? - i don't see it in the FOLIO check in response
	//WOULD INVOVLE ANOTHER API CALL
	private ItemDescription retreiveItemDescription(JSONObject checkInTrans) {
		ItemDescription itemDescription = new ItemDescription();
		// itemDescription.setCallNumber(checkInTrans.getCallNumber());
		return itemDescription;
	}

	private BibliographicDescription retrieveBiblioDescription(JSONObject checkInTrans) {
		BibliographicDescription biblioDescription = new BibliographicDescription();
		biblioDescription.setAuthor(checkInTrans.getJSONObject("item").getString("primaryContributor"));
		biblioDescription.setTitle(checkInTrans.getJSONObject("item").getString("title"));
		return biblioDescription;
	}

}
