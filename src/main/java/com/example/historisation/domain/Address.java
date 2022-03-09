package com.example.historisation.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;
import org.javers.repository.jql.QueryBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@TypeName("domain.Address")
public class Address {

    @Getter @Setter
    @Column(name = "address_city")
    String city;

    @DiffIgnore
    @Column(name = "address_street")
    String street;

    Address(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", street='" + street + '\'' +
                '}';
    }
}
