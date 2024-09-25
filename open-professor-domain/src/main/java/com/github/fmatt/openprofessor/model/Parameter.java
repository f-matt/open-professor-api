package com.github.fmatt.openprofessor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "parameters")
@NamedQuery(name = "Parameter.findAll",
        query = "SELECT p "
                + "FROM Parameter p "
                + "ORDER BY p.name")
@NamedQuery(name = "Parameter.findByName",
        query = "SELECT p "
                + "FROM Parameter p "
                + "WHERE UPPER(p.name) = :name")
public class Parameter extends BaseEntity {

    public static final String PROPERTIES_FILE_PATH = "PROPERTIES_FILE_PATH";

    public static final String MOODLE_MASK = "MOODLE_MASK";

    public static final String LATEX_MASK = "LATEX_MASK";

    @Size(max = 20)
    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
