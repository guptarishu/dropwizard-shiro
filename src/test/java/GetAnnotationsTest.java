import org.apache.shiro.util.JdbcUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class GetAnnotationsTest {

    @Test
    public void testPermissionsMissingInDatabase() throws Exception {
        Class[] classes = getClasses("resources");
        Set<String> permissions = new HashSet<String>();

        for(Class classs : classes) {
            Method[] methods = classs.getMethods();
            for(Method method:  methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                for(Annotation annotation : annotations) {
                    if(annotation.annotationType() == org.apache.shiro.authz.annotation.RequiresPermissions.class) {
                        String annotationStr = annotation.toString();
                        int index = annotationStr.indexOf("value=");
                        String value = annotationStr.substring(index + "value=".length(), annotationStr.length() - 1);
                        String strippedStr = value.substring(1, value.length()-1);
                        permissions.add(strippedStr);
                    }
                }
            }
        }
        //check permissions against the phoenix tables
        Connection connection = getPhoenixConnection();
        Set<String> databasePermissions = getPermissions(connection);

        System.out.println();
        //permissions that need to be added in the database

        Set<String> diffPermissions = new HashSet<String>();
        for(String permission : permissions) {
            if(!databasePermissions.contains(permission)) {
                diffPermissions.add(permission);
            }
        }
        System.out.println();
    }

    private Connection getPhoenixConnection() throws Exception {
        Connection r = DriverManager.getConnection("jdbc:phoenix:10.10.30.51");
        r.setAutoCommit(true);
        return r;
    }

    protected Set<String> getPermissions(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        Set<String> permissions = new LinkedHashSet<String>();
        try {
            ps = conn.prepareStatement("select PERMISSIONS from ROLE_PERMISSIONS");
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

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            Object resource = resources.nextElement();
            URL resource1 = (URL) resource;
            dirs.add(new File(resource1.getFile()));
            System.out.println();
        }

        ArrayList classes = new ArrayList();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
