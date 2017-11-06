package auth;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CustomAuthorizationInfo implements AuthorizationInfo {

    protected String role;

    /**
     * Collection of all string-based permissions associated with the account.
     */
    protected Set<String> stringPermissions;

    /**
     * Collection of all object-based permissions associaed with the account.
     */
    protected Set<Permission> objectPermissions;

    /**
     * Default no-argument constru ctor.
     */
    public CustomAuthorizationInfo() {
    }

    /**
     * Creates a new instance with the specified roles and no permissions.
     * @param roles the roles assigned to the realm account.
     */
    public CustomAuthorizationInfo(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    /**
     * Sets the roles assigned to the account.
     * @param roles the roles assigned to the account.
     */
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public Collection<String> getRoles() {
        return Collections.singleton(role);
    }

    public Set<String> getStringPermissions() {
        return stringPermissions;
    }

    /**
     * Sets the string-based permissions assigned directly to the account.  The permissions set here, in addition to any
     * {@link #getObjectPermissions() object permissions} constitute the total permissions assigned directly to the
     * account.
     *
     * @param stringPermissions the string-based permissions assigned directly to the account.
     */
    public void setStringPermissions(Set<String> stringPermissions) {
        this.stringPermissions = stringPermissions;
    }

    /**
     * Adds (assigns) a permission to those directly associated with the account.  If the account doesn't yet have any
     * direct permissions, a new permission collection (a Set&lt;String&gt;) will be created automatically.
     * @param permission the permission to add to those directly assigned to the account.
     */
    public void addStringPermission(String permission) {
        if (this.stringPermissions == null) {
            this.stringPermissions = new HashSet<String>();
        }
        this.stringPermissions.add(permission);
    }

    /**
     * Adds (assigns) multiple permissions to those associated directly with the account.  If the account doesn't yet
     * have any string-based permissions, a  new permissions collection (a Set&lt;String&gt;) will be created automatically.
     * @param permissions the permissions to add to those associated directly with the account.
     */
    public void addStringPermissions(Collection<String> permissions) {
        if (this.stringPermissions == null) {
            this.stringPermissions = new HashSet<String>();
        }
        this.stringPermissions.addAll(permissions);
    }

    public Set<Permission> getObjectPermissions() {
        return objectPermissions;
    }

    /**
     * Sets the object-based permissions assigned directly to the account.  The permissions set here, in addition to any
     * {@link #getStringPermissions() string permissions} constitute the total permissions assigned directly to the
     * account.
     *
     * @param objectPermissions the object-based permissions assigned directly to the account.
     */
    public void setObjectPermissions(Set<Permission> objectPermissions) {
        this.objectPermissions = objectPermissions;
    }

    /**
     * Adds (assigns) a permission to those directly associated with the account.  If the account doesn't yet have any
     * direct permissions, a new permission collection (a Set&lt;{@link Permission Permission}&gt;) will be created automatically.
     * @param permission the permission to add to those directly assigned to the account.
     */
    public void addObjectPermission(Permission permission) {
        if (this.objectPermissions == null) {
            this.objectPermissions = new HashSet<Permission>();
        }
        this.objectPermissions.add(permission);
    }

    /**
     * Adds (assigns) multiple permissions to those associated directly with the account.  If the account doesn't yet
     * have any object-based permissions, a  new permissions collection (a Set&lt;{@link Permission Permission}&gt;)
     * will be created automatically.
     * @param permissions the permissions to add to those associated directly with the account.
     */
    public void addObjectPermissions(Collection<Permission> permissions) {
        if (this.objectPermissions == null) {
            this.objectPermissions = new HashSet<Permission>();
        }
        this.objectPermissions.addAll(permissions);
    }
}
