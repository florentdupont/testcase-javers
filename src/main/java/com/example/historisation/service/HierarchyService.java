package com.example.historisation.service;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.repository.BankDetailsRepository;
import com.example.historisation.repository.EmployeeRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.example.historisation.domain.Position.*;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

@Service
public class HierarchyService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    BankDetailsRepository bankDetailsRepository;
    

    @Transactional(REQUIRES_NEW)
    public Employee initStructure(){
        System.out.println("initializing structure");

        val gandalf = new Employee("Gandalf", 10_000, CEO, "Middle-earth");
        val elrond = new Employee("Elrond", 8_000, CFO, "Rivendell");
        val aragorn = new Employee("Aragorn", 8_000, CTO, "Minas Tirith");
        val thorin = new Employee("Thorin", 5_000, TEAM_LEAD, "Lonely Mountain");
        val frodo = new Employee("Frodo", 3_000, DEVELOPER, "Shire");
        val fili = new Employee("Fili", 3_000, DEVELOPER, "Lonely Mountain");
        val kili = new Employee("Kili", 3_000, DEVELOPER, "Lonely Mountain");
        val bifur = new Employee("Bifur", 3_000, DEVELOPER, "Lonely Mountain");
        val bombur = new Employee("Bombur", 2_000, SCRUM_MASTER, "Lonely Mountain");

        gandalf.addSubordinates(elrond, aragorn);
        aragorn.addSubordinate(thorin);
        thorin.addSubordinates(frodo, fili, kili, bifur, bombur);

        employeeRepository.save(gandalf);

        return gandalf;
    }

    @Transactional(REQUIRES_NEW)
    public void giveRaise(Employee employee, int raise) {
        employee.giveRaise(raise);
        employeeRepository.save(employee);
    }

    @Transactional(REQUIRES_NEW)
    public void updateSalary(Employee employee, int salary) {
        employee.updateSalary(salary);
        employeeRepository.save(employee);
    }

    @Transactional(REQUIRES_NEW)
    public void updateCity(Employee employee, String city) {
        employee.getAddress().setCity(city);
        employeeRepository.save(employee);
    }

    @Transactional(REQUIRES_NEW)
    public void addBankDetails(Employee employee, BankDetails bankDetails) {
        bankDetailsRepository.save(bankDetails);
        employee.setBankDetails(bankDetails);
        
        employeeRepository.save(employee);
    }

    public Employee findByName(String name) {
        return employeeRepository.findByName(name);
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id).get();
    }

    @Transactional(REQUIRES_NEW)
    public void updateBankDetails(Employee frodo, String modified) {
        val bd = frodo.getBankDetails();
        bd.setIban(modified);
        bankDetailsRepository.save(bd);
       
        // je resave, mais normalement, ca ne devrait rien faire car l'objet n'a pas été modifié
        // au vu de la modélisation, c'est employee qui porte un lien vers le BankDetail.
        // si le contenu de BD change, le lien ne change pas.
        // et en effet, ca ne change rien !
        // les changements disponible pour Employee ne refleterons pas les changement de BankDetails, 
        // ce qui semble normal car les 2 entités sont indépendantes.
        employeeRepository.save(frodo);
        
        // Il faudrait pouvoir trouver un moyen de propager des modifications, ou alors d'intégrer une modification au 
        // sein d'un meme "commit". 
        
        
    }
}
