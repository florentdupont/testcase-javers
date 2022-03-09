package com.example.historisation;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.service.HierarchyService;
import lombok.val;
import org.javers.core.ChangesByObject;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class HistorisationApplication implements CommandLineRunner {


	
	public static void main(String[] args) {
		SpringApplication.run(HistorisationApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	
		
	}
}
