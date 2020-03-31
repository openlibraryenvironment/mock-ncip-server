package org.olf.rs.mockncip.listeners;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.olf.rs.Constants;


public class ConfigurationListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		System.out.println("init the db here");
		try {
			Class.forName("org.sqlite.JDBC");
			//Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			//connection.setAutoCommit(false);
			
			SqlSession mysqlsession = null; 
			String mysqlresource = Constants.MY_BATIS_CONFIG_FILE;
			InputStream mysqlinputStream = Resources.getResourceAsStream(mysqlresource);
			SqlSessionFactory mysqlsqlSessionFactory = new SqlSessionFactoryBuilder().build(mysqlinputStream);
			mysqlsession = mysqlsqlSessionFactory.openSession();
			Connection connection = mysqlsession.getConnection();

			DatabaseMetaData meta = connection.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("A new database has been created.");
			Statement stmt = connection.createStatement(); 
			stmt.executeUpdate("drop table if exists item");
			stmt.executeUpdate("drop table if exists patron");
			stmt.executeUpdate("drop table if exists request");
			stmt.executeUpdate("drop table if exists loan");
			
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("createdb.sql");
			String s            = new String();
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while((s = br.readLine()) != null)
            {
                sb.append(s);
            }
            br.close();
            String[] inst = sb.toString().split(";");

 
            for(int i = 0; i<inst.length; i++)
            {
                // we ensure that there is no spaces before or after the request string
                // in order to not execute empty statements
                if(!inst[i].trim().equals(""))
                {
                	stmt.executeUpdate(inst[i]);
                    System.out.println(">>"+inst[i]);
                }
            }
            


            context.setAttribute("db", connection);
            
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

}
