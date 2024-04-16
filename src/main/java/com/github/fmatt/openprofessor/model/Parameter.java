package com.github.fmatt.openprofessor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "parameters")
public class Parameter extends BaseEntity {
    
    @Size(max = 20)
    private String name;

    private String text;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
