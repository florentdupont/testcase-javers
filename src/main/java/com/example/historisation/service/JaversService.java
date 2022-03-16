package com.example.historisation.service;

import com.example.historisation.domain.Employee;
import lombok.val;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.InstanceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JaversService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    Javers javers;
    
    
    public void removeLastCommit(long id, Class clazz) {
        
        // todo prévoir des protections  pour ne pas supprimer trop loin (ne pas supprimer le INITIAL)
        
        val lastSnapshot = javers.getLatestSnapshot(id, clazz).get();
        removeCommitContainingSnapshot(lastSnapshot);
    }

    private void removeCommitContainingSnapshot(CdoSnapshot snapshot) {
      
        val globalIdPk = findGlobalIdPk(snapshot);
        val commitPk=  findCommitPk(globalIdPk, snapshot.getVersion());
        
        removeCommit(commitPk);
    }

    private Integer findGlobalIdPk(CdoSnapshot snapshot) {
        // TODO faire un test si l'instance est autre chose qu'un InstanceId;
        val instanceId = (InstanceId) snapshot.getGlobalId();
        val typeName = instanceId.getTypeName();
        val cdoId = instanceId.getCdoId().toString();
        
        String query = "Select global_id_pk from JV_global_id WHERE type_name = ? AND  local_id = ?";
        val globalIdPk = jdbcTemplate.queryForObject(query, Integer.class, new Object[] { typeName, cdoId });
        
        return globalIdPk;
    }
    
    private Integer findCommitPk(long globalIdPk, long version) {
        val query = "Select commit_fk from JV_snapshot WHERE version = ? AND  global_id_fk = ?";
        val commitPk = jdbcTemplate.queryForObject(query, Integer.class, new Object[] { version, globalIdPk });
        return commitPk;
    }
    
    private void removeCommit(long commitPk) {
        // plusieurs lignes de SNAPSHOT peuvent correspondre à un seul commit
        jdbcTemplate.update("delete from jv_snapshot where commit_fk = ?", commitPk);
        jdbcTemplate.update("DELETE FROM jv_commit where commit_pk = ?", commitPk);
        // TODO éventuellement supprimer aussi de la table jv_commit_property

    }
    
    
}
