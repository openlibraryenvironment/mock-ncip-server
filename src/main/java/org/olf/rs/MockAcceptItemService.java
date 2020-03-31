package org.olf.rs;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.AcceptItemResponseData;
import org.extensiblecatalog.ncip.v2.service.AcceptItemService;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.ItemIdentifierType;
import org.extensiblecatalog.ncip.v2.service.PickupLocation;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.extensiblecatalog.ncip.v2.service.RequestIdentifierType;
import org.extensiblecatalog.ncip.v2.service.ServiceContext;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONObject;

public class MockAcceptItemService extends MockNcipService implements AcceptItemService {

	AcceptItemResponseData responseData = new AcceptItemResponseData();

	private static final Logger logger = Logger.getLogger(MockAcceptItemService.class);

	public AcceptItemResponseData performService(AcceptItemInitiationData initData, ServiceContext serviceContext,
			RemoteServiceManager serviceManager) {

		AcceptItemResponseData responseData = new AcceptItemResponseData();

		ItemId itemId = initData.getItemId();
		UserId userId = initData.getUserId();
		PickupLocation pickupLocation = initData.getPickupLocation();
		logger.info("accept item: " + itemId);

		try {
			validateUserId(userId);
			validateItemId(itemId);
			validatePickupLocation(pickupLocation);
		} 
		catch (Exception exception) {
			logger.error("Failed validating userid, itemid or pickup location." + exception.getLocalizedMessage());
			if (responseData.getProblems() == null)
				responseData.setProblems(new ArrayList<Problem>());
			Problem p = new Problem(new ProblemType(Constants.ACCEPT_ITEM_PROBLEM), Constants.ACCEPT_ITEM_INPUT_PROBLEM,
					exception.getMessage(), exception.getMessage());
			responseData.getProblems().add(p);
			return responseData;
		}

		// ATTEMPT TO DETERMINE AGENCY ID
		// INITIATION HEADER IS NOT REQUIRED
		String requesterAgencyId = null;
		try {
			requesterAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
			if (requesterAgencyId == null || requesterAgencyId.equalsIgnoreCase(""))
				throw new Exception("Requester agency ID is missing");
		} 
		catch (Exception e) {
			// cannot get requester agency id from init header - try request id element
			try {
				requesterAgencyId = initData.getRequestId().getAgencyId().getValue();
				if (requesterAgencyId == null || requesterAgencyId.trim().equalsIgnoreCase(""))
					throw new Exception("From Agency ID Missing");
			} catch (Exception except) {
				logger.error(
						"Could not determine agency id from initiation header or request id element.");
				if (responseData.getProblems() == null)
					responseData.setProblems(new ArrayList<Problem>());
				Problem p = new Problem(new ProblemType(Constants.CHECK_OUT_PROBLEM), Constants.UNKNOWN_DATA_ELEMENT,
						Constants.CHECK_OUT_PROBLEM, e.getMessage());
				responseData.getProblems().add(p);
				return responseData;
			}

		}

		ItemIdentifierType itemIdentifierType = new ItemIdentifierType(Constants.SCHEME, Constants.ITEM_BARCODE);
		RequestIdentifierType requestIdentifierType = new RequestIdentifierType(Constants.SCHEME, Constants.REQUEST_ID);

		itemId.setAgencyId(new AgencyId(requesterAgencyId));
		itemId.setItemIdentifierType(itemIdentifierType);
		RequestId ncipRequestId = new RequestId();
		ncipRequestId.setAgencyId(new AgencyId(requesterAgencyId));
		ncipRequestId.setRequestIdentifierType(requestIdentifierType);

		try {
			// THE SERVICE MANAGER CALLS THE OKAPI APIs
			JSONObject acceptItemResponseDetails = ((MockRemoteServiceManager) serviceManager).acceptItem(initData,
					requesterAgencyId.toLowerCase());
			String assignedRequestId = acceptItemResponseDetails.getString("id");
			responseData.setItemId(itemId);
			ncipRequestId.setRequestIdentifierValue(assignedRequestId);
			responseData.setRequestId(ncipRequestId);
		} 
		catch (Exception e) {
			if (responseData.getProblems() == null)
				responseData.setProblems(new ArrayList<Problem>());
			Problem p = new Problem(new ProblemType(Constants.ACCEPT_ITEM_PROBLEM), Constants.UNKNOWN_DATA_ELEMENT,
					Constants.ACCEPT_ITEM_PROBLEM, e.getMessage());
			responseData.getProblems().add(p);
			return responseData;
		}
		return responseData;
	}

	private void validatePickupLocation(PickupLocation pickupLocation) throws MockNcipException {
		if (pickupLocation == null || pickupLocation.getValue() == null
				|| pickupLocation.getValue().trim().equalsIgnoreCase("")) {
			MockNcipException exception = new MockNcipException(Constants.PICKUP_LOCATION_MISSING);
			throw exception;
		}
	}
}
