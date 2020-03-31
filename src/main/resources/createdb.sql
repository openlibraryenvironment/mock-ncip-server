


CREATE TABLE request (
	id TEXT(255),
	patronBarcode TEXT(500),
	requestDate TEXT(500),
	itemBarcode TEXT(500),
	pickupLocation TEXT(500),
	requestStatus TEXT(500)
);

CREATE TABLE loan (
	id TEXT(500),
	dueDate TEXT(500),
	itemBarcode TEXT(500),
	patronBarcode TEXT(500),
	loanStatus TEXT(500)
);

CREATE TABLE Item (
	id TEXT(500),
	title TEXT(500),
	itemBarcode TEXT(500),
	author TEXT(500),
	status TEXT(500),
	callNumber TEXT(500)
);

CREATE TABLE patron (
	id TEXT(255),
	firstName TEXT(500),
	lastName TEXT(500),
	barcode TEXT(500),
	userid TEXT(500),
	status TEXT(500),
	patrongroup TEXT(500),
	library TEXT(500),
	email TEXT(500),
	phone TEXT(500)
);

INSERT INTO PATRON values("2","jane","Doe","5551000","def123","BLOCKED","STAFF","LINDERMAN","def123@lehigh.edu","201-555-1212");
INSERT INTO PATRON values("2","john","Smith","5551001","jos555","ACTIVE","GRAD","FAIRCHILD","jos555@lehigh.edu","215-555-1212");
INSERT INTO PATRON values("2","Cindy","Jones","5551002","coj555","ACTIVE","UGRAD","LINDERMAN","coj555@lehigh.edu","610-555-1212");
INSERT INTO ITEM values("b2a10690-6951-11ea-bc55-0242ac130003","One Fish Two Fish","39151000209805","Richard Hesse","AVAILABLE","500.1");
INSERT INTO ITEM values("f08421d3-72b4-47c4-a55d-670f58e08100","Grapes of Wrath","39151000095337","James Ortega","LOANED","500.2");
INSERT INTO ITEM values("9d3b79f9-9c18-4c48-b50f-37e090006f11","Fine Balance","39151004322067","A. Smith","AVAILABLE","500.3");
INSERT INTO LOAN values("9d3b79f9-9c18-4c48-b50f-37e090006f11","2020-06-24T04:00:00Z","39151000095337","5551002","OPEN");


