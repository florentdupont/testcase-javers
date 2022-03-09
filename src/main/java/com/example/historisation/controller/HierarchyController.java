package com.example.historisation.controller;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.domain.History;
import com.example.historisation.domain.HistoryEvent;
import com.example.historisation.service.HierarchyService;
import jdk.swing.interop.SwingInterOpUtils;
import lombok.val;
import org.javers.core.ChangesByCommit;
import org.javers.core.ChangesByObject;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.SnapshotType;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

@RestController
public class HierarchyController {

    @Autowired
    HierarchyService service;

    @Autowired
    Javers javers;
    
    @GetMapping("/init")
    public void init() {
        service.initStructure();
    }
    
    
    @GetMapping("/raise")
    public void update() {

        val frodo = service.findByName("Frodo");

        service.giveRaise(frodo, 100);
    }
    
    
    @GetMapping("/bank")
    public void addBankDetails() {

        val frodo = service.findByName("Frodo");
      
        service.addBankDetails(frodo, new BankDetails("IBAN0123"));

    }

    @GetMapping("/changebank")
    public void changeBankDetails() {

        val frodo = service.findByName("Frodo");

        service.updateBankDetails(frodo, "987654");

    }


    @GetMapping("/shadows")
    public void shadowsForFrodo() {
        val frodo = service.findByName("Frodo");

        List<Shadow<Employee>> shadows = javers.findShadows(
                QueryBuilder.byInstanceId(frodo.getId(), Employee.class)
                        .withSnapshotTypeUpdate()
                        .build());

        if(shadows.isEmpty()) {
            System.out.println("No UPDATE found for Frodo");
        }

        for(Shadow<Employee> employeeShadow : shadows) {
            System.out.println("employee updated : " + employeeShadow.get());
            System.out.println("employee properties : " + employeeShadow.getCommitMetadata().getProperties());
        }
    }
    
    @GetMapping("/purge")
    public void purge() {
        javers.
    }
    
    @GetMapping("/changes") 
    public History changesForFrodo() {
        val history = new History();
        
        val frodo = service.findByName("Frodo");

        // récupère tous les changements, que ce soit des création ou modif
        val changes = javers.findChanges(
                QueryBuilder.byInstanceId(frodo.getId(), Employee.class)
                        //.withSnapshotType(SnapshotType.INITIAL)
                        .build());

        System.out.println("Employee Frodo changes : ");
        changes.groupByCommit().forEach(byCommit -> {
            System.out.println("commit " + byCommit.getCommit().getId());
            
            // sur un commit initial, on retourne un historique basique
            System.out.println("'" + byCommit.getCommit().getId().value() + "'");
            if("1.00".equals(byCommit.getCommit().getId().value())) {
                history.addEvent("Creating Employee " + frodo.getName(),
                        byCommit.getCommit().getCommitDate());
                return; // skip to the next iteration
            }
            
            byCommit.groupByObject().forEach(byObject -> {
                // System.out.println("* changes on " + byObject.getGlobalId().value() + " : ");
                byObject.get().forEach(change -> {
                    if(change instanceof ValueChange) {
                        val valueChange = (ValueChange) change;
                        history.addEvent("" + valueChange.getPropertyName() + " has been changed",
                                change.getCommitMetadata().get().getCommitDate());
                    }  else if(change instanceof ReferenceChange) {
                        val referenceChange = (ReferenceChange) change;
                        System.out.println("right :" + referenceChange.getRightObject().get());
                        history.addEvent("A new BankDetails has been added",
                                change.getCommitMetadata().get().getCommitDate());
                        
                    } else {
                        history.addEvent("Another change has been done",
                                change.getCommitMetadata().get().getCommitDate());
                    }
                    
                    System.out.println("  - " + change);
                });
                
            });
        });

        if(frodo.getBankDetails() == null)
            return history;
        
        // je vois pas d'autres solutions que de vérifier également dans les objets liés
        System.out.println("Employee Frodo Bank Details changes ");
        val bdChanges = javers.findChanges(
                QueryBuilder.byInstanceId(frodo.getBankDetails().getId(), BankDetails.class).
                        withSnapshotTypeUpdate().build());

        bdChanges.groupByCommit().forEach(byCommit -> {
            System.out.println("commit " + byCommit.getCommit().getId());
            byCommit.groupByObject().forEach(byObject -> {
                System.out.println("* changes on " + byObject.getGlobalId().value() + " : ");
                byObject.get().forEach(change -> {
                    if(change instanceof ValueChange) {
                        val valueChange = (ValueChange) change;
                        history.addEvent("BankDetails value " + valueChange.getPropertyName() + " has been changed", 
                                change.getCommitMetadata().get().getCommitDate());
                    }
                    System.out.println("  - " + change);
                });
            });
        });
         
        // tri
        history.getEvents().sort(comparing(HistoryEvent::getDate));
        return history;
    }
    
    
}
