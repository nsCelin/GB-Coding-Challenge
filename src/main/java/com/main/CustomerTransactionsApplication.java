package com.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


/*Spring Boot is embedded with Hibernate looking in for database configuration.
 * As this application doesnot need datasource, it is excluded with Spring Boot Config*/

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = { "com.configuration", "com.dto"})
public class CustomerTransactionsApplication {

	public static void main(String[] args) throws Exception {	
		
		final Logger LOGGER  = LoggerFactory.getLogger(CustomerTransactionsApplication.class);
		
		LOGGER.info("Entering the CustomerTransactionsApplication Application.");
		SpringApplication.run(CustomerTransactionsApplication.class, args);		
		
	}

}
