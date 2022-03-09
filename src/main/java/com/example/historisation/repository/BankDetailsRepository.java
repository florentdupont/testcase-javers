package com.example.historisation.repository;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

@JaversSpringDataAuditable
public interface BankDetailsRepository extends CrudRepository<BankDetails, Long> {


}
