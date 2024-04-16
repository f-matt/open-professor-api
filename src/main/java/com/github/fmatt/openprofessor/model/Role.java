package com.github.fmatt.openprofessor.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(schema = "auth", name = "roles")
public class Role extends BaseEntity {

    @Size(max = 20)
    private String description;

    private Boolean active = true;

    @ManyToMany
    @JoinTable(schema = "auth", 
        name = "roles_permissions", 
        joinColumns = { @JoinColumn(name = "role_id") },
        inverseJoinColumns = { @JoinColumn(name = "permission_id") })
    private Set<Permission> permissions = new HashSet<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}
