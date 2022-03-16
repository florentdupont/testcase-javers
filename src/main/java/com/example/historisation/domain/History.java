package com.example.historisation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Classe générique sortant les données d'historisation
public class History {
    
    @Getter
    List<Line> lines = new ArrayList<>();
    
    public void addLine(Line newLine) {
        lines.add(newLine);
    }
        
    @Data
    public static class Line {
        LocalDateTime date;
        @Getter @Setter
        String comment;
        List<Event> events = new ArrayList<>();
        
        public Line(LocalDateTime d) {
            this.date = d;
        }
        
        public Line(LocalDateTime d, Event e) {
            this(d);
            events.add(e);
        }
        
        public void addEvent(Event e) {
            events.add(e);
        }
    } 
    
    @Data
    @AllArgsConstructor
    public static class Event {
        
        String description;
    }
}
