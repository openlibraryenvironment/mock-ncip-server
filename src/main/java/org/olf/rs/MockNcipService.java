package org.olf.rs;

import java.util.regex.Pattern;

import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.olf.rs.Constants;
import org.olf.rs.MockNcipException;

public class MockNcipService {

	protected void validateUserId(UserId userId) throws MockNcipException {

		this.validateUserIdIsPresent(userId);
		this.validateUserIdIsValid(userId);

	}

	protected void validateItemId(ItemId itemId) throws Exception {

		this.validateItemIdIsPresent(itemId);
		this.validateItemIdIsValid(itemId);
	}

	protected void validateUserIdIsPresent(UserId userId) throws MockNcipException {

		if (userId == null || userId.getUserIdentifierValue() == null) {
			MockNcipException exception = new MockNcipException(Constants.USER_ID_MISSING);
			throw exception;
		}

	}

	protected void validateUserIdIsValid(UserId userId) throws MockNcipException {

		final Pattern idPattern = Pattern.compile("^[a-zA-Z0-9\\s\\.\\-_]+$");
		if (!idPattern.matcher(userId.getUserIdentifierValue()).matches()
				|| userId.getUserIdentifierValue().length() > 100) {
			MockNcipException exception = new MockNcipException("User id is invalid");
			throw exception;
		}
	}

	protected void validateItemIdIsPresent(ItemId itemId) throws MockNcipException {

		if (itemId == null || itemId.getItemIdentifierValue() == null) {
			MockNcipException exception = new MockNcipException("Item id missing");
			throw exception;
		}

	}

	protected void validateItemIdIsValid(ItemId itemId) throws MockNcipException {

		final Pattern idPattern = Pattern.compile("^[a-zA-Z0-9\\s\\.\\-_]+$");
		if (!idPattern.matcher(itemId.getItemIdentifierValue()).matches()
				|| itemId.getItemIdentifierValue().length() > 100) {
			MockNcipException exception = new MockNcipException("Item id is invalid");
			throw exception;
		}
	}
}
