package com.example.historisation.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor
@TypeName("domain.BankDetails")
public class BankDetails {

    @Id @Getter
    @GeneratedValue(strategy = IDENTITY)
    long id;
    
    @Column
    @Getter @Setter        
    String iban;
    
    public BankDetails(String iban) {
        this.iban = iban;
    }
    
}
