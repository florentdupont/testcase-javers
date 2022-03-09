package com.example.historisation.repository;

import com.example.historisation.domain.Employee;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

@JaversSpringDataAuditable
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    
    Employee findByName(String name);
    
}