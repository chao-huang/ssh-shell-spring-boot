package com.github.fonimus.ssh.shell.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Spring security ssh shell authentication provider
 */
@Slf4j
public class SshShellSecurityAuthenticationProvider
		implements SshShellAuthenticationProvider {

	public static final String AUTHENTICATION_ATTRIBUTE = "authentication";

	private final String authProviderBeanName;

	private ApplicationContext context;

	private AuthenticationManager authenticationManager;

	public SshShellSecurityAuthenticationProvider(ApplicationContext context, String authProviderBeanName) {
		this.context = context;
		this.authProviderBeanName = authProviderBeanName;
	}

	@PostConstruct
	public void init() {
		Map<String, AuthenticationManager> map = context.getBeansOfType(AuthenticationManager.class);
		if (map.isEmpty()) {
			throw new BeanCreationException(
					"Could not find any beans of class: " + AuthenticationManager.class.getName());
		}
		String beanName = authProviderBeanName;
		Set<String> available = map.keySet();
		if (beanName != null && !beanName.isEmpty()) {
			this.authenticationManager = map.get(beanName);
			if (this.authenticationManager == null) {
				throw new BeanCreationException(
						"Could not find bean with name: " + beanName + " and class: " + AuthenticationManager.class
								.getName() + ". Available are: "
								+ available);
			}
		} else {
			if (map.size() != 1) {
				throw new BeanCreationException(
						"Found too many beans of class: " + AuthenticationManager.class.getName() + ". Please specify" +
								" name with property '" + SSH_SHELL_PREFIX
								+ ".authProviderBeanName'");
			}
			Map.Entry<String, AuthenticationManager> e = map.entrySet().iterator().next();
			beanName = e.getKey();
			this.authenticationManager = e.getValue();
		}
		LOGGER.info("Using authentication manager named: {} [class={}]", beanName,
				this.authenticationManager.getClass().getName());
	}

	@Override
	public boolean authenticate(String username, String pass,
			ServerSession serverSession) throws PasswordChangeRequiredException {
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, pass));
			LOGGER.debug("User {} authenticated with authorities: {}", username, auth.getAuthorities());
			List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
			serverSession.getIoSession().setAttribute(AUTHENTICATION_ATTRIBUTE,
					new SshAuthentication(auth.getPrincipal(), auth.getDetails(), auth.getCredentials(), authorities));
			return auth.isAuthenticated();
		} catch (AuthenticationException e) {
			LOGGER.error("Unable to authenticate user [{}] : {}", username, e.getMessage());
			LOGGER.debug("Unable to authenticate user [{}]", username, e);
			return false;
		}
	}
}
