package org.olf.rs.otherservices;

import java.io.InputStream;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.olf.rs.Constants;
import org.olf.rs.mockncip.models.ServiceError;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VerificationService {
	
	protected SqlSession getSession(Connection dbConnection) throws Exception {
		try {
    		SqlSession mysqlsession = null; 
    		String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
    		InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
    		SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
    		mysqlsession = mysqlsqlSessionFactory.openSession(dbConnection);
    		return mysqlsession;
		}
		catch(Exception e) {
			throw e;
		}
	}
	
	protected String constructReturnString(Object object) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		String json = objectMapper.writeValueAsString(object);
		return json;
	}
	
	protected String errorAsJsonString(String errorMessage,Exception e) throws JsonProcessingException {
		ServiceError error = new ServiceError();
		error.setErrorMessage(errorMessage + "; Exception: " + e.getLocalizedMessage());
		String errorMessageAsJson = constructReturnString(error);
		return errorMessageAsJson;
	}


}
