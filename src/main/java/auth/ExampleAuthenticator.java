package auth;

import com.google.common.collect.ImmutableSet;
import core.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {

    /**
     * Valid users with mapping user -> roles
     */
    private static final Set<String> VALID_USERS = ImmutableSet.of("guest", "testuser1", "chief-wizard", "testuser2");


    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (VALID_USERS.contains(credentials.getUsername()) && "testpassword".equals(credentials.getPassword())) {

            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(credentials.getUsername(), credentials.getPassword());
            Subject subject = SecurityUtils.getSubject();
            subject.login(usernamePasswordToken);

            //char[] password = ((UsernamePasswordToken) usernamePasswordToken).getPassword();
            //SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(usernamePasswordToken.getUsername(), password, "phoenix");
            //Subject subject1 = createSubject(usernamePasswordToken, info, subject);

            return Optional.of(new User(credentials.getUsername(), credentials.getPassword(), subject));
        }
        return Optional.empty();
    }

//    private Subject createSubject(AuthenticationToken token, AuthenticationInfo info, Subject existing) {
//        SubjectContext context = createSubjectContext();
//        context.setAuthenticated(true);
//        context.setAuthenticationToken(token);
//        context.setAuthenticationInfo(info);
//        if (existing != null) {
//            context.setSubject(existing);
//        }
//        return doCreateSubject(context);
//    }
//
//    private SubjectContext createSubjectContext() {
//        return new DefaultSubjectContext();
//    }
//
//
//    private Subject doCreateSubject(SubjectContext context) {
//        return new DefaultSubjectFactory().createSubject(context);
//    }

}
