package com.example.historisation.domain;

public enum Status {
    
    BEING_CREATED,                 // en cours de création
    VALIDATED,                     // validé
    MODIFIED_BUT_NOT_REVALIDATED  // a été modifié, mais pas encore revalidé
}