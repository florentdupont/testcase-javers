package com.example.historisation.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.*;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor
// il est préférable de respécifier le nom du type pour en réduire la longueur
@TypeName("domain.Employee")
public class Employee {

    @Id @Getter
    @GeneratedValue(strategy = IDENTITY)
    long id;

    @Getter
    String name;
    
    @Getter
    @Embedded
    Address address;
    
    @OneToOne(cascade = ALL)
    @Getter @Setter
    BankDetails bankDetails;
                
    Integer salary;

    @Getter @Setter
    Status status;
    
    


    public Employee(String name, Integer salary, String city) {
        this.name = name;
        this.address = new Address(city);
        this.salary = salary;
        this.status = Status.BEING_CREATED;
    }

    
    public void addBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }
    

    public void giveRaise(int raise) {
        salary += raise;
    }

    public void updateSalary(int salary) {
        this.salary = salary;
    }
    
    
    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", address=" + address +
                ", salary=" + salary +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
