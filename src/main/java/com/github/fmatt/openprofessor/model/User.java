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
@Table(schema = "auth", name = "users")
public class User extends BaseEntity {

    @Size(max = 50)
    private String name;

    @Size(max = 20)
    private String username;

    @Size(max = 256)
    private String password;

    private Boolean active = true;

    @ManyToMany
    @JoinTable(schema = "auth",
        name = "users_roles",
        joinColumns = { @JoinColumn(name = "user_id")},
        inverseJoinColumns = { @JoinColumn(name = "role_id")})
    private Set<Role> roles = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
