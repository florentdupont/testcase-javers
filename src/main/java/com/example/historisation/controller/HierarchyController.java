package com.example.historisation.controller;

import com.example.historisation.domain.BankDetails;
import com.example.historisation.domain.Employee;
import com.example.historisation.domain.History;
import com.example.historisation.service.HierarchyService;
import com.example.historisation.service.JaversService;
import lombok.val;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.SnapshotType;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HierarchyController {

    @Autowired
    HierarchyService service;

    @Autowired
    JaversService javersService;

    @Autowired
    Javers javers;
    
    @GetMapping("/init")
    public void init() {
        service.initStructure();
    }
    
    
    @GetMapping("/raise")
    public void update() {

        val frodo = service.findByName("Frodo");

        service.giveRaise(frodo.getId(), 100);
    }

    @GetMapping("/validate")
    public void validate() {

        val frodo = service.findByName("Frodo");

        service.validate(frodo.getId());
    }
    
    
    @GetMapping("/bank")
    public void addBankDetails() {

        val frodo = service.findByName("Frodo");
      
        service.addBankDetails(frodo.getId(), new BankDetails("IBAN0123"));

    }

    @GetMapping("/changebank")
    public void changeBankDetails() {

        val frodo = service.findByName("Frodo");

        service.updateBankDetails(frodo.getId(), "987654");

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

        // supprime le commit correspondant au dernier SNAPSHOT
        val frodo = service.findByName("Frodo");
        javersService.removeLastCommit(frodo.getId(), Employee.class);
      
    }
    
    
    @GetMapping("/changes")
    public History changesForFrodo() {
        val history = new History();

        val frodo = service.findByName("Frodo");
        
        // il n'existe pas de possibilité de faire un findCommit qui incluerait toutes les modifs ?
        // il faut d'abord recherche sur un type d'entité, récupérer les commit liés à ces entités.
        // puis réintérroger les différentes entités qui correspondent à ces commits
        val initialCommit = findInitialCommitFor(frodo);
        
        
        History.Line line = new History.Line(initialCommit.getCommitDate());
        line.addEvent(new History.Event("Creating Employee " + frodo.getName()));
        history.addLine(line);
                
        val commitIds = findUpdateCommitIdsFor(frodo);

        commitIds.forEach(commitId -> {
            
            val commitChanges = findChangesWithCommitId(commitId, Employee.class, BankDetails.class);
           
            // récupère tous les changements liés sur les autres entités
            commitChanges.groupByCommit().forEach( commit -> {
                System.out.println("props : " + commit.getCommit().getProperties());
                val commitComment = commit.getCommit().getProperties().get("comment");
                System.out.println("commitComment : " + commitComment);
                History.Line updateLine = new History.Line(commit.getCommit().getCommitDate());
                updateLine.setComment(commitComment);
                commit.get().forEach( change -> {
                    // TODO si le commit inclue un changement de STATUS, alors il n'est pas nécessaire
                    //      d'indiquer tous les autres changements. on n'indique que le changement de status et c'est tout
                    updateLine.addEvent(convertChangeToHistoryEvent(change));
                });
                history.addLine(updateLine);
            });
        });
        
        // tri
        history.getLines().sort(Comparator.comparing(History.Line::getDate));
        return history;
    }

    
       
    
    private History.Event convertChangeToHistoryEvent(Change change) {
        
        if(change instanceof ValueChange) {
            val valueChange = (ValueChange) change;
            String msg = "" + valueChange.getPropertyName() + " changed from " + valueChange.getLeft() + " to " + valueChange.getRight();
            return new History.Event(msg);
        }
        if(change instanceof NewObject) {
            return new History.Event("Creating new Employee");
        }
        if(change instanceof ReferenceChange) {
            val refChange = (ReferenceChange) change;
            return new History.Event("" +  refChange.getPropertyName() + "has been affected to a new object");
        }

        System.out.println("EVENT NOT CONVERTED : " + change.getClass().getCanonicalName());
        return new History.Event("UNDEFINED : " + change );
        
    }
    
    private List<CommitId> findAllCommitIds() {
        val changes = javers.findChanges(QueryBuilder.anyDomainObject().build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit().getId()).collect(Collectors.toList());
    }

    private List<CommitId> findAllCommitIdsFor(Class clazz) {
        val changes = javers.findChanges(QueryBuilder.byClass(clazz).build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit().getId()).collect(Collectors.toList());
    }
    
    private List<CommitId> findAllCommitIdsFor(Class clazz, Long id) {
        val changes = javers.findChanges(QueryBuilder.byInstanceId(id, clazz).build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit().getId()).collect(Collectors.toList());
    }

    private List<CommitId> findAllCommitIdsFor(Object object) {
        val changes = javers.findChanges(QueryBuilder.byInstance(object).build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit().getId()).collect(Collectors.toList());
    }

    private CommitMetadata findInitialCommitFor(Object object) {
        val changes = javers.findChanges(QueryBuilder.byInstance(object).withSnapshotType(SnapshotType.INITIAL).build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit()).findFirst().get();
    }

    private List<CommitId> findUpdateCommitIdsFor(Object object) {
        val changes = javers.findChanges(QueryBuilder.byInstance(object).withSnapshotTypeUpdate().build());
        return changes.groupByCommit().stream().map(byCommit -> byCommit.getCommit().getId()).collect(Collectors.toList());
    }
    
    private Changes findChangesWithCommitId(CommitId commitId, Class ... classes) {
        return javers.findChanges(QueryBuilder.byClass(classes).withCommitId(commitId).build());
    }
    
    
}
