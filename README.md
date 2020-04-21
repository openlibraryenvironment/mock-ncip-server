# mock-ncip-server
Receives and responds to NCIP requests


## Build and run:
gradle build
<br>
gradle appRun
### build Docker image
Build the application
```
./gradlew build
```
Build the container:
```
docker build -f docker/Dockerfile -t mock-ncip-server .
```
Run the container (example)
```
docker run -d -p 8080:8080 mock-ncip-server
```

## Endpoints

#### All NCIP requests should use the endpoint (POST): http://localhost:8080/ncip  (you can adjust the port in the build file)

#### The following GET endpoints can be used for pulling data out/ testing
##### Patrons
http://localhost:8080/api/patrons/
<br>
http://localhost:8080/api/patrons/{patronid}

#### Items
http://localhost:8080/api/items/{itemBarcode}
<br>
http://localhost:8080/api/items/available
<br>
http://localhost:8080/api/items/loaned
<br>
http://localhost:8080/api/items/

#### Requests
http://localhost:8080/api/requests/
<br>
http://localhost:8080/api/requests/open
<br>
http://localhost:8080/api/requests/{requestId}
<br>
http://localhost:8080/api/requests/item/{itemBarcode}
<br>
http://localhost:8080/api/requests/patron/{patronBarcode}

#### Loans
http://localhost:8080/api/loans/
<br>
http://localhost:8080/api/loans/patron/{patronBarcode}
<br>
http://localhost:8080/api/loans/item/{itemBarcode}
<br>
http://localhost:8080/api/loans/open

When appRun is called a jetty server is started and a sqlite database is created and test data is inserted:
```sql
INSERT INTO PATRON values("2","Jane","Doe","5551000","def123","BLOCKED","STAFF","LINDERMAN","def123@mockemail.edu","201-555-1212");
INSERT INTO PATRON values("3","John","Smith","5551001","jos555","ACTIVE","GRAD","FAIRCHILD","jos555@mockemail.edu","215-555-1212");
INSERT INTO PATRON values("4","Cindy","Jones","5551002","coj555","ACTIVE","UGRAD","LINDERMAN","coj555@mockemail.edu","610-555-1212");
INSERT INTO ITEM values("b2a10690-6951-11ea-bc55-0242ac130003","One Fish Two Fish","39151000209805","Richard Hesse","AVAILABLE","500.1");
INSERT INTO ITEM values("f08421d3-72b4-47c4-a55d-670f58e08100","Grapes of Wrath","39151000095337","James Ortega","LOANED","500.2");
INSERT INTO ITEM values("9d3b79f9-9c18-4c48-b50f-37e090006f11","Fine Balance","39151004322067","A. Smith","AVAILABLE","500.3");
INSERT INTO LOAN values("9d3b79f9-9c18-4c48-b50f-37e090006f11","2020-06-24T04:00:00Z","39151000095337","5551002","OPEN");
```
When the application is closed...any data added to the database is not saved...it's just temporary while the mock server is in use


## Sample XML

### LookupUser
```xml
<?xml version="1.0" encoding="UTF-8"?>
<NCIPMessage version="http://www.niso.org/schemas/ncip/v2_0/ncip_v2_0.xsd"
   xmlns="http://www.niso.org/2008/ncip">
  <LookupUser>
<InitiationHeader>
      <FromAgencyId>
        <AgencyId>ReShare</AgencyId>
      </FromAgencyId>
      <ToAgencyId>
        <AgencyId>ReShare</AgencyId>
      </ToAgencyId>
      <ApplicationProfileType>EZBORROW</ApplicationProfileType>
  </InitiationHeader> 
    <UserId>
      <AgencyId>ReShare</AgencyId>
      <UserIdentifierValue>5551000</UserIdentifierValue>
    </UserId>
    <UserElementType>Name Information</UserElementType>
    <UserElementType>User Address Information</UserElementType>
    <UserElementType>User Privilege</UserElementType>
    <UserElementType>User Id</UserElementType>
  </LookupUser>
</NCIPMessage>
```

### Accept Item
```xml
<?xml version="1.0" encoding="UTF-8"?>
<NCIPMessage version="http://www.niso.org/schemas/ncip/v2_0/ncip_v2_0.xsd"
   xmlns="http://www.niso.org/2008/ncip">
	<AcceptItem>
		<InitiationHeader>
    		 <FromAgencyId>
        		<AgencyId>ReShare</AgencyId>
    		</FromAgencyId>
    		<ToAgencyId>
        		<AgencyId>ReShare</AgencyId>
    		</ToAgencyId>
      <ApplicationProfileType>EZBORROW</ApplicationProfileType>
    	</InitiationHeader>
		<RequestedActionType>Hold For Pickup</RequestedActionType>
		<UserId>
			<AgencyId>ReShare</AgencyId>
			<UserIdentifierValue>5551000</UserIdentifierValue>
		</UserId>
		<ItemId>
			<AgencyId>ReShare</AgencyId>
			<ItemIdentifierValue>LEH-20200310556</ItemIdentifierValue>
		</ItemId>
		<ItemOptionalFields>
			<BibliographicDescription>
			<Author>Author</Author>
			<Title>Delete test</Title>
		</BibliographicDescription>
		<ItemDescription>
			<CallNumber>Call Number</CallNumber>
		</ItemDescription>
		</ItemOptionalFields>
				<RequestId>
			<AgencyId>ReShare</AgencyId>
			<RequestIdentifierValue>LEH-20200310556</RequestIdentifierValue>
		</RequestId>
		<PickupLocation>FAIRCHILD</PickupLocation>
	</AcceptItem>
</NCIPMessage>
```

### Checkout Item
```xml
<?xml version="1.0" encoding="UTF-8"?>
<NCIPMessage v:version="5.6" xmlns:v="http://www.niso.org/2008/ncip" xmlns="http://www.niso.org/2008/ncip" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.niso.org/2008/ncip http://www.niso.org/schemas/ncip/v2_0/ncip_v2_0.xsd ">
  <CheckOutItem>
    <InitiationHeader>
      <FromAgencyId>
        <AgencyId>ReShare</AgencyId>
      </FromAgencyId>
      <ToAgencyId>
        <AgencyId>ReShare</AgencyId>
      </ToAgencyId>
      <ApplicationProfileType>EZBORROW</ApplicationProfileType>
    </InitiationHeader> 
    <UserId>
      <AgencyId>ReShare</AgencyId>
      <UserIdentifierValue>5551000</UserIdentifierValue>
    </UserId>
    <ItemId>
      <AgencyId>ReShare</AgencyId> 
      <ItemIdentifierValue>LEH-20200310556</ItemIdentifierValue>
    </ItemId>
  </CheckOutItem>
</NCIPMessage>
```

### Checkin Item
```xml
<?xml version="1.0" encoding="UTF-8"?>
<NCIPMessage version="http://www.niso.org/schemas/ncip/v2_0/ncip_v2_0.xsd"
   xmlns="http://www.niso.org/2008/ncip">
 <CheckInItem>
 <InitiationHeader>
		<FromAgencyId>
			<AgencyId>ReShare</AgencyId>
		</FromAgencyId>
		<ToAgencyId>
			<AgencyId>ReShare</AgencyId>
		</ToAgencyId>
		<ApplicationProfileType>EZBORROW</ApplicationProfileType>
	</InitiationHeader> 
   <ItemId>
      <AgencyId>ReShare</AgencyId>
	  <ItemIdentifierValue>LEH-20200310556</ItemIdentifierValue>
   </ItemId>
   <ItemElementType>Bibliographic Description</ItemElementType>
 </CheckInItem>
</NCIPMessage>
```




