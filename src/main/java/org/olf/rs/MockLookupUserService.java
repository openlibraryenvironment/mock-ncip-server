package org.olf.rs;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.AgencyUserPrivilegeType;
import org.extensiblecatalog.ncip.v2.service.AuthenticationInput;
import org.extensiblecatalog.ncip.v2.service.ElectronicAddress;
import org.extensiblecatalog.ncip.v2.service.ElectronicAddressType;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserResponseData;
import org.extensiblecatalog.ncip.v2.service.LookupUserService;
import org.extensiblecatalog.ncip.v2.service.NameInformation;
import org.extensiblecatalog.ncip.v2.service.PersonalNameInformation;
import org.extensiblecatalog.ncip.v2.service.PhysicalAddress;
import org.extensiblecatalog.ncip.v2.service.PhysicalAddressType;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.ServiceContext;
import org.extensiblecatalog.ncip.v2.service.StructuredAddress;
import org.extensiblecatalog.ncip.v2.service.StructuredPersonalUserName;
import org.extensiblecatalog.ncip.v2.service.UserAddressInformation;
import org.extensiblecatalog.ncip.v2.service.UserAddressRoleType;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
import org.extensiblecatalog.ncip.v2.service.UserOptionalFields;
import org.extensiblecatalog.ncip.v2.service.UserPrivilege;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatus;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatusType;

import java.util.Properties;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.olf.rs.Constants;
import org.olf.rs.MockRemoteServiceManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class MockLookupUserService extends MockNcipService implements LookupUserService {

	private static final Logger logger = Logger.getLogger(MockLookupUserService.class);

	public LookupUserResponseData performService(LookupUserInitiationData initData, ServiceContext serviceContext,
			RemoteServiceManager serviceManager) {
		logger.info("data passed into performService  : ");
		logger.info(initData.toString());
		LookupUserResponseData responseData = new LookupUserResponseData();
		Connection con = ((MockRemoteServiceManager) serviceManager).getDbConnection();

		UserId userId = retrieveUserId(initData, serviceManager);

		try {
			validateUserId(userId);
		} 
		catch (Exception exception) {
			if (responseData.getProblems() == null)
				responseData.setProblems(new ArrayList<Problem>());
			Problem p = new Problem(new ProblemType(Constants.LOOKUP_USER_VALIDATION_PROBLEM),
					Constants.LOOKUP_USER_VALIDATION_PROBLEM, exception.getMessage(), exception.getMessage());
			responseData.getProblems().add(p);
			return responseData;
		}

		try {
			// THE SERVICE MANAGER CALLS THE OKAPI APIs
			JSONObject patronDetailsAsJson = ((MockRemoteServiceManager) serviceManager).lookupUser(userId);
			if (patronDetailsAsJson == null) {
				ProblemType problemType = new ProblemType("");
				Problem p = new Problem(problemType, Constants.USERID, Constants.USER_NOT_FOUND);
				responseData.setProblems(new ArrayList<Problem>());
				responseData.getProblems().add(p);
				return responseData;
			}
			responseData = constructResponse(initData, patronDetailsAsJson);
			logger.info("API LOOKUP RESULTS...");
			logger.info(patronDetailsAsJson.toString());

		} 
		catch (Exception e) {
			logger.error("error during performService:");
			logger.error(e.toString());
			ProblemType problemType = new ProblemType("");
			Problem p = new Problem(problemType, Constants.GENERAL_ERROR, Constants.LOOKUP_USER_FAILED + e.toString());
			responseData.setProblems(new ArrayList<Problem>());
			responseData.getProblems().add(p);
			return responseData;
		}
		return responseData;

	}

	private LookupUserResponseData constructResponse(LookupUserInitiationData initData, JSONObject userDetails)
			throws Exception {

		LookupUserResponseData responseData = new LookupUserResponseData();
		try {

			String agencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
			if (responseData.getUserOptionalFields() == null)
				responseData.setUserOptionalFields(new UserOptionalFields());

			if (initData.getNameInformationDesired()) {
				responseData.getUserOptionalFields().setNameInformation(this.retrieveName(userDetails));
			}

			if (initData.getUserIdDesired())
				responseData.setUserId(this.retrieveBarcode(userDetails, agencyId));

			if (initData.getUserAddressInformationDesired())
				responseData.getUserOptionalFields().setUserAddressInformations(this.retrieveAddress(userDetails));

			if (initData.getUserPrivilegeDesired()) {
				responseData.getUserOptionalFields().setUserPrivileges(this.retrievePrivileges(userDetails, agencyId));
				responseData.getUserOptionalFields().getUserPrivileges()
						.add(this.retrieveBorrowingPrvilege(userDetails, agencyId));
			}
		} catch (Exception e) {
			logger.error("error during constructing lookup user construct response:");
			logger.error(e.toString());
			throw e;
		}
		return responseData;
	}

	private UserId retrieveBarcode(JSONObject jsonObject, String agencyId) throws Exception {
		UserId userId = new UserId();
		String barcode = jsonObject.getString("barcode");
		userId.setUserIdentifierValue(barcode);
		userId.setAgencyId(new AgencyId(agencyId));
		userId.setUserIdentifierType(new UserIdentifierType("barcode"));
		return userId;
	}

	private UserPrivilege retrieveBorrowingPrvilege(JSONObject jsonObject, String agencyId) throws Exception {
		UserPrivilege up = new UserPrivilege();
		up.setUserPrivilegeDescription("User Status");
		up.setAgencyId(new AgencyId(agencyId));
		up.setAgencyUserPrivilegeType(new AgencyUserPrivilegeType("", "STATUS"));
		UserPrivilegeStatus ups = new UserPrivilegeStatus();
		String patronBorrowingStatus = jsonObject.getString("status");
		ups.setUserPrivilegeStatusType(new UserPrivilegeStatusType("", patronBorrowingStatus));
		up.setUserPrivilegeStatus(ups);
		return up;
	}

	private ArrayList<UserPrivilege> retrievePrivileges(JSONObject jsonObject, String agencyId) {
		String patronType = jsonObject.getString("patrongroup");
		String patronHomeLibrary = jsonObject.getString("library");

		ArrayList<UserPrivilege> list = new ArrayList<UserPrivilege>();
		;
		list.add(this.retrievePrivilegeFor(patronType, "User Profile", "PROFILE", agencyId));
		list.add(this.retrievePrivilegeFor(patronHomeLibrary, "User Library", "LIBRARY", agencyId));
		return list;
	}

	private UserPrivilege retrievePrivilegeFor(String userPrivilegeStatusTypeString, String descriptionString,
			String agencyUserPrivilegeTypeString, String agencyId) {

		UserPrivilege up = new UserPrivilege();
		up.setUserPrivilegeDescription(descriptionString);
		up.setAgencyId(new AgencyId(agencyId));
		new AgencyUserPrivilegeType(agencyId, agencyId);
		up.setAgencyUserPrivilegeType(new AgencyUserPrivilegeType("", agencyUserPrivilegeTypeString));
		UserPrivilegeStatus ups = new UserPrivilegeStatus();
		ups.setUserPrivilegeStatusType(new UserPrivilegeStatusType("", userPrivilegeStatusTypeString));
		up.setUserPrivilegeStatus(ups);
		return up;
	}

	private NameInformation retrieveName(JSONObject jsonObject) {
		String firstName = jsonObject.getString("lastName");
		String lastName = jsonObject.getString("firstName");
		NameInformation nameInformation = new NameInformation();
		PersonalNameInformation personalNameInformation = new PersonalNameInformation();
		StructuredPersonalUserName structuredPersonalUserName = new StructuredPersonalUserName();
		structuredPersonalUserName.setGivenName(firstName);
		structuredPersonalUserName.setSurname(lastName);
		personalNameInformation.setUnstructuredPersonalUserName(firstName + " " + lastName);
		personalNameInformation.setStructuredPersonalUserName(structuredPersonalUserName);
		nameInformation.setPersonalNameInformation(personalNameInformation);
		return nameInformation;
	}

	private ArrayList<UserAddressInformation> retrieveAddress(JSONObject jsonObject) {
		ArrayList<UserAddressInformation> list = new ArrayList<UserAddressInformation>();
		list.add(retrieveEmail(jsonObject));
		list.add(retrieveTelephoneNumber(jsonObject));
		return list;
	}

	private UserAddressInformation retrieveTelephoneNumber(JSONObject jsonObject) {

		String phoneNumber = jsonObject.getString("phone");
		if (phoneNumber != null) {
			ElectronicAddress phone = new ElectronicAddress();
			phone.setElectronicAddressData(phoneNumber);
			phone.setElectronicAddressType(new ElectronicAddressType("TEL")); // TODO constants
			UserAddressInformation uai = new UserAddressInformation();
			uai.setUserAddressRoleType(new UserAddressRoleType(Constants.CAMPUS)); // TODO: constants, what should this
																					// be?
			uai.setElectronicAddress(phone);
			return uai;
		} else
			return null;
	}

	private UserAddressInformation retrieveEmail(JSONObject jsonObject) {

		String emailAddress = jsonObject.getString("email"); // TODO constants
		ElectronicAddress email = new ElectronicAddress();
		email.setElectronicAddressData(emailAddress);
		email.setElectronicAddressType(new ElectronicAddressType("electronic mail address")); // TODO CONSTANT
		UserAddressInformation uai = new UserAddressInformation();
		uai.setUserAddressRoleType(new UserAddressRoleType("OTH")); // TODO CONSTANT
		uai.setElectronicAddress(email);
		return uai;
	}

	private UserAddressInformation retrievePhysicalAddress(JSONObject jsonObject) {

		// TODO constants
		String streetAddresss = jsonObject.getString("addressLine1");
		String city = jsonObject.getString("city");
		UserAddressInformation uai = new UserAddressInformation();
		PhysicalAddress pa = new PhysicalAddress();
		StructuredAddress sa = new StructuredAddress();
		sa.setLine1(streetAddresss);
		sa.setLocality(city);
		pa.setStructuredAddress(sa);
		uai.setUserAddressRoleType(new UserAddressRoleType(Constants.CAMPUS));
		pa.setPhysicalAddressType(new PhysicalAddressType(null, "Postal Address")); // TODO
		uai.setPhysicalAddress(pa);
		return uai;
	}

	private String retrieveAuthenticationInputTypeOf(LookupUserInitiationData initData,
			RemoteServiceManager serviceManager) {
		if (initData.getAuthenticationInputs() == null)
			return null;
		String barcode = null;
		for (AuthenticationInput authenticationInput : initData.getAuthenticationInputs()) {

			String authType = authenticationInput.getAuthenticationInputType().getValue();
			String authValue = authenticationInput.getAuthenticationInputData();

			try {
				JSONObject patronDetailsAsJson = ((MockRemoteServiceManager) serviceManager)
						.lookupPatronRecordBy(authType, authValue);
				barcode = patronDetailsAsJson.getString("barcode");
				if (barcode != null && !barcode.equalsIgnoreCase(""))
					return barcode;
			} catch (Exception e) {
				logger.error("unable to get barcode value from input");
			}

		}
		if (barcode != null && barcode.equalsIgnoreCase(""))
			barcode = null;
		return barcode;
	}

	private UserId retrieveUserId(LookupUserInitiationData initData, RemoteServiceManager serviceManager) {
		UserId uid = null;
		String uidString = null;
		if (initData.getUserId() != null) {
			uid = initData.getUserId();
		} else {
			// try AuthenticationInput:
			uidString = this.retrieveAuthenticationInputTypeOf(initData, serviceManager);
		}
		if (uidString != null) {
			uid = new UserId();
			uid.setUserIdentifierValue(uidString);
		}
		return uid;
	}

}
