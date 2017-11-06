package core;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import java.security.Principal;
import java.util.Set;

public class User implements Principal {
    private String name;
    private Set<String> roles;
    private Subject subject;
    private String password;

    public User(String name) {
        this.name = name;
        this.roles = null;
    }

    public User(String name, String password, Subject subject) {
        this.name = name;
        this.password = password;
        this.subject = subject;
    }

    public User(Subject subject) {
        super();
        if (subject == null) throw new NullPointerException();
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name  = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getId() {
        return (int) (Math.random() * 100);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean isPermitted(String permission) throws AuthorizationException {
        return subject.isPermitted(permission);
    }

    public boolean hasRole(String roleIdentifier) throws AuthorizationException {
        return this.subject.hasRole(roleIdentifier);
    }

    public boolean isAuthenticated() {
        return this.subject.isAuthenticated();
    }
}