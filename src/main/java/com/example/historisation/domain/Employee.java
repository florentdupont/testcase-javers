package com.example.historisation.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    
    @ManyToOne
    Employee boss;
    
    @OneToMany(cascade = ALL, mappedBy = "boss")
    Set<Employee> subordinates = new HashSet();

    @Getter
    @Embedded
    Address address;
    
    @OneToOne(cascade = ALL)
    @Getter @Setter
    BankDetails bankDetails;
                
    Integer salary;

    Position position;


    public Employee(String name, Integer salary, Position position, String city) {
        this.name = name;
        this.address = new Address(city);
        this.salary = salary;
        this.position = position;
    }

    Employee getSubordinate(String name) {
        return subordinates.stream().filter(it -> it.name.equals(name)).findFirst().get();
    }
    
    public void addBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public void addSubordinate(Employee subordinate) {
        subordinate.boss = this;
        this.subordinates.add(subordinate);
    }

    public void addSubordinates(Employee... subordinates) {
        for(Employee it : subordinates) {
            addSubordinate(it);
        }
    }
    
    
    int getLevel() {
        if (boss == null) 
            return 0;
        return boss.getLevel() + 1;
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
                ", boss=" + boss +
                ", subordinates=" + subordinates +
                ", address=" + address +
                ", salary=" + salary +
                ", position=" + position +
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
