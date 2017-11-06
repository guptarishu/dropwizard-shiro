package realm.jdbc;

import auth.CustomAuthorizationInfo;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class PhoenixRealm extends AuthorizingRealm {

    protected static final String DEFAULT_CONNECTION_URL = "jdbc:phoenix:10.10.30.51";

    /**
     * The default query used to retrieve the roles that apply to a user.
     */
    protected static final String DEFAULT_USER_ROLES_QUERY = "select USER_ROLE from APP_USERS_TEST where user_name = ?";

    /**
     * The default query used to retrieve permissions that apply to a particular role.
     */
    protected static final String DEFAULT_PERMISSIONS_QUERY = "select PERMISSIONS from ROLE_PERMISSIONS where ROLE = ?";

    private static final Logger log = LoggerFactory.getLogger(JdbcRealm.class);

    /*--------------------------------------------
    |    I N S T A N C E   V A R I A B L E S    |
    ============================================*/

    protected String userRolesQuery = DEFAULT_USER_ROLES_QUERY;
    protected String permissionsQuery = DEFAULT_PERMISSIONS_QUERY;
    protected boolean permissionsLookupEnabled = false;
    protected String connectionURL = DEFAULT_CONNECTION_URL;

    /*--------------------------------------------
    |         C O N S T R U C T O R S           |
    ============================================*/

    /*--------------------------------------------
    |  A C C E S S O R S / M O D I F I E R S    |
    ============================================*/

    /**
     * Overrides the default query used to retrieve a user's roles during authorization.  When using the default
     * implementation, this query must take the user's username as a single parameter and return a row
     * per role with a single column containing the role name.  If you require a solution that does not match this query
     * structure, you can override {@link #doGetAuthorizationInfo(PrincipalCollection)} or just
     * {@link #getRoleNameForUser(java.sql.Connection,String)}
     *
     * @param userRolesQuery the query to use for retrieving a user's roles.
     * @see #DEFAULT_USER_ROLES_QUERY
     */
    public void setUserRolesQuery(String userRolesQuery) {
        this.userRolesQuery = userRolesQuery;
    }

    /**
     * Overrides the default query used to retrieve a user's permissions during authorization.  When using the default
     * implementation, this query must take a role name as the single parameter and return a row
     * per permission with three columns containing the fully qualified name of the permission class, the permission
     * name, and the permission actions (in that order).  If you require a solution that does not match this query
     * structure, you can override {@link #doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)} or just
     * </p>
     * <p/>
     * <b>Permissions are only retrieved if you set {@link #permissionsLookupEnabled} to true.  Otherwise,
     * this query is ignored.</b>
     *
     * @param permissionsQuery the query to use for retrieving permissions for a role.
     * @see #DEFAULT_PERMISSIONS_QUERY
     * @see #setPermissionsLookupEnabled(boolean)
     */
    public void setPermissionsQuery(String permissionsQuery) {
        this.permissionsQuery = permissionsQuery;
    }

    /**
     * Enables lookup of permissions during authorization.  The default is "false" - meaning that only roles
     * are associated with a user.  Set this to true in order to lookup roles <b>and</b> permissions.
     *
     * @param permissionsLookupEnabled true if permissions should be looked up during authorization, or false if only
     *                                 roles should be looked up.
     */
    public void setPermissionsLookupEnabled(boolean permissionsLookupEnabled) {
        this.permissionsLookupEnabled = permissionsLookupEnabled;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /*--------------------------------------------
    |               M E T H O D S               |
    ============================================*/

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }
        char[] password = ((UsernamePasswordToken) token).getPassword();
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(username, password, getName());

        return simpleAuthenticationInfo;
    }

    /**
     * This implementation of the interface expects the principals collection to return a String username keyed off of
     * this realm's {@link #getName() name}
     *
     * @see #getAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        String username = (String) getAvailablePrincipal(principals);
        Connection conn = null;

        String roleName = null;
        Set<String> permissions = null;
        try {
            conn = getPhoenixConnection();

            // Retrieve roles and permissions from database
            roleName = getRoleNameForUser(conn, username);
            if (permissionsLookupEnabled) {
                permissions = getPermissions(conn, roleName);
            }

        } catch (SQLException e) {
            final String message = "There was a SQL error while authorizing user [" + username + "]";
            if (log.isErrorEnabled()) {
                log.error(message, e);
            }

            // Rethrow any SQL errors as an authorization exception
            throw new AuthorizationException(message, e);
        } catch (Exception e) {
            log.error("error getting a connection", e);
        } finally {
            JdbcUtils.closeConnection(conn);
        }

        CustomAuthorizationInfo info = new CustomAuthorizationInfo(roleName);
        info.setStringPermissions(permissions);
        return info;
    }

    private Connection getPhoenixConnection() throws Exception {
        Connection r = DriverManager.getConnection(connectionURL);
        r.setAutoCommit(true);
        return r;
    }

    protected String getRoleNameForUser(Connection conn, String username) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String roleName = null;
        try {
            ps = conn.prepareStatement(userRolesQuery);
            ps.setString(1, username);

            // Execute query
            rs = ps.executeQuery();
            while (rs.next()) {
                roleName = rs.getString(1);
            }

        } catch (Exception e) {
            log.error("error", e);
        } finally{
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
        }
        return roleName;
    }

    protected Set<String> getPermissions(Connection conn, String roleName) throws SQLException {
        PreparedStatement ps = null;
        Set<String> permissions = new LinkedHashSet<String>();
        try {
            ps = conn.prepareStatement(permissionsQuery);
            ps.setString(1, roleName);
            ResultSet rs = null;

            try {
                // Execute query
                rs = ps.executeQuery();

                // Loop over results and add each returned role to a set
                while (rs.next()) {
                    Array array = rs.getArray(1);

                    // Add the permission to the set of permissions
                    permissions.addAll(Arrays.asList((String[]) array.getArray()));
                }
            } finally {
                JdbcUtils.closeResultSet(rs);
            }
        } finally {
            JdbcUtils.closeStatement(ps);
        }

        return permissions;
    }

}
