package com.example.historisation.service;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.domain.Status;
import com.example.historisation.repository.BankDetailsRepository;
import com.example.historisation.repository.EmployeeRepository;
import lombok.val;
import org.javers.core.Javers;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.HashMap;

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

        val gandalf = new Employee("Gandalf", 10_000, "Middle-earth");
        val elrond = new Employee("Elrond", 8_000, "Rivendell");
        val aragorn = new Employee("Aragorn", 8_000, "Minas Tirith");
        val thorin = new Employee("Thorin", 5_000, "Lonely Mountain");
        val frodo = new Employee("Frodo", 3_000, "Shire");
        val fili = new Employee("Fili", 3_000, "Lonely Mountain");
        val kili = new Employee("Kili", 3_000, "Lonely Mountain");
        val bifur = new Employee("Bifur", 3_000, "Lonely Mountain");
        val bombur = new Employee("Bombur", 2_000, "Lonely Mountain");

        val bd = new BankDetails("IBAN123");
        frodo.addBankDetails(bd);
        bankDetailsRepository.save(bd);
        
        employeeRepository.save(gandalf);
        employeeRepository.save(frodo);
                
        // commit manuel : l'avantage de cette approche est de pouvoir gérer plus finement
        // notamment si des entités sont dans un état DRAFT par exemple.
        // dans ce cas, il n'est pas nécessaire de les commiter
        javers.commit("author", gandalf);
        
        javers.commit("author", frodo);

        return gandalf;
    }
    
    
    

    @Transactional(REQUIRES_NEW)
    public void giveRaise(Long employeeId, int raise) {
        val employee = employeeRepository.findById(employeeId).get();
                
        employee.giveRaise(raise);
        employeeRepository.save(employee);
        
        // on lui change son IBAN également
        employee.getBankDetails().setIban("IBAN987");
        
        employee.setStatus(Status.MODIFIED_BUT_NOT_REVALIDATED);
        
        // récupère le dernier SNAPSHOT : tant que le statu est MODIFIED_BUT_NOT_REVALIDATED
        // il n'est pas nécessaire de stocker toutes les versions on ne gardera que le dernier
        // pour éviter de ne stocker des modifications pour rien;
        // val lastSnapshots = javers.findSnapshots(QueryBuilder.byInstance(employee).withSnapshotTypeUpdate().limit(1).build());
        val latestSnapshot = javers.getLatestSnapshot(employeeId, Employee.class);
        
        if(latestSnapshot.isPresent()) {

            System.out.println(latestSnapshot);
            
        }
                
                
        // tant qu'il n'a pas été validé au moins une fois, 
        // alors on n'enregistre pas son historique.
        if(employee.getStatus() != Status.BEING_CREATED) {
            // dans ce cas, Employee et BankDetails seront commité dans le meme
            javers.commit("author", employee);
        }
        
        
        
    }

    @Transactional(REQUIRES_NEW)
    public void validate(Long employeeId) {

        val employee = employeeRepository.findById(employeeId).get();

        employee.setStatus(Status.VALIDATED);
        employeeRepository.save(employee);
        
        val properties = new HashMap<String, String>();
        properties.put("comment", "VALIDATION CERTIFIEE XXXX");
        
        javers.commit("author", employee, properties);
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
