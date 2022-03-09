package com.example.historisation.service;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.repository.BankDetailsRepository;
import com.example.historisation.repository.EmployeeRepository;
import lombok.val;
import org.javers.core.Javers;
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

    @Autowired
    Javers javers;

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
        
        // commit manuel : l'avantage de cette approche est de pouvoir gérer plus finement
        // notamment si des entités sont dans un état DRAFT par exemple.
        // dans ce cas, il n'est pas nécessaire de les commiter
        javers.commit("author", gandalf);

        return gandalf;
    }

    @Transactional(REQUIRES_NEW)
    public void giveRaise(Long employeeId, int raise) {
        val employee = employeeRepository.findById(employeeId).get();
                
        employee.giveRaise(raise);
        employeeRepository.save(employee);
        
        javers.commit("author", employee);
    }

    @Transactional(REQUIRES_NEW)
    public void updateSalary(Employee employee, int salary) {
        employee.updateSalary(salary);
        employeeRepository.save(employee);

        javers.commit("author", employee);
    }

    @Transactional(REQUIRES_NEW)
    public void updateCity(Employee employee, String city) {
        employee.getAddress().setCity(city);
        employeeRepository.save(employee);

        javers.commit("author", employee);
    }

    @Transactional(REQUIRES_NEW)
    public void addBankDetails(Long employeeId, BankDetails bankDetails) {
        
        val employee = employeeRepository.findById(employeeId).get();
        
        bankDetailsRepository.save(bankDetails);
        employee.setBankDetails(bankDetails);
        
        employeeRepository.save(employee);

        // est-ce qu'on peux mettre 2 truc dans ce cas ?
        // ici pas besoin de commiter le BandDetails, comme il est dans le graphe du Employee
        // alors il est pris en compte
        javers.commit("author", employee);
    }

    public Employee findByName(String name) {
        return employeeRepository.findByName(name);
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id).get();
    }

    @Transactional(REQUIRES_NEW)
    public void updateBankDetails(Long employeeId, String modified) {
        
        val employee = employeeRepository.findById(employeeId).get();
        
        val bd = employee.getBankDetails();
        bd.setIban(modified);
        bankDetailsRepository.save(bd);
       
        // je resave, mais normalement, ca ne devrait rien faire car l'objet n'a pas été modifié
        // au vu de la modélisation, c'est employee qui porte un lien vers le BankDetail.
        // si le contenu de BD change, le lien ne change pas.
        // et en effet, ca ne change rien !
        // les changements disponible pour Employee ne refleterons pas les changement de BankDetails, 
        // ce qui semble normal car les 2 entités sont indépendantes.
        // employeeRepository.save(employee);
        
        // en faisant un commit explicite juste sur le employee, le bankDetails est pris en compte car
        // il fait parti du graphe modifié de employee.
        // Mais attention, il n'apparaitra pas pour autant dans les Changes du Employee
        javers.commit("author", employee);
        
        // lors de la récupération des Changes, il faudra bien récupérer les changements de Employee
        // puis de BankDetails dans un second temps
        
    }
}
