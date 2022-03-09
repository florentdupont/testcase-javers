package com.example.historisation.domain;

import lombok.Getter;

import java.time.LocalDateTime;

public class HistoryEvent {
    
    @Getter
    String event;
    
    @Getter
    LocalDateTime date;
    
    public HistoryEvent(String event, LocalDateTime date) {
        this.event = event;
        this.date = date;
    }
    
}
