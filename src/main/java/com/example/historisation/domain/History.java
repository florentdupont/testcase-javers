package com.example.historisation.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Classe générique sortant les données d'historisation
public class History {
    
    // a améliorer, ptet qu'on aurait une ligne d'historique et que chaque ligne aie plusieurs
    // events
    // un event aurait aussi une date
    @Getter
    List<HistoryEvent> events = new ArrayList<>();
    
    public void addEvent(String event, LocalDateTime date) {
        events.add(new HistoryEvent(event, date ));
    }
}
