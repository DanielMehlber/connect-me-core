package org.connectme.core;

import org.connectme.core.authentication.filter.UserAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ConnectMeCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectMeCoreApplication.class, args);
	}
}
