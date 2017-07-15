package com.github.bilak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @author lvasek.
 */
@Configuration
public class SecurityConfiguration {

	@Configuration
	@EnableAuthorizationServer
	public static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;

		@Bean
		protected AuthorizationCodeServices authorizationCodeServices() {
			return new InMemoryAuthorizationCodeServices();
		}

		@Bean
		AccessTokenConverter accessTokenConverter() {
			return new DefaultAccessTokenConverter();
		}

		@Bean
		TokenStore tokenStore() {
			return new InMemoryTokenStore();
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints)
				throws Exception {
			endpoints
					.authorizationCodeServices(authorizationCodeServices())
					.accessTokenConverter(accessTokenConverter())
					.authenticationManager(authenticationManager)
					.tokenStore(tokenStore())
					.approvalStoreDisabled();
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
			security
					.tokenKeyAccess("permitAll()")
					.checkTokenAccess("isAuthenticated()");
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			// @Formatter:off
			clients
					.inMemory()
						.withClient("demo")
						.secret("demo")
						.scopes("read", "write", "trust")
						.authorizedGrantTypes("authorization_code", "client_credentials", "refresh_token", "implicit", "password");
			// @Formatter:on
		}
	}

	@Configuration
	@EnableResourceServer
	public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
					.antMatcher("/user")
					.authorizeRequests().anyRequest().authenticated()
			;
		}
	}

	@Configuration
	@Order(6)
	public static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.formLogin().loginPage("/login").permitAll()
					.and()
					.requestMatchers().antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access")
					.and()
					.authorizeRequests().anyRequest().authenticated()
					.and()
					.logout().logoutUrl("/logout").logoutSuccessUrl("/login");
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.parentAuthenticationManager(authenticationManager);
		}
	}

	@Configuration
	static class GlobalSecurityConfigurer extends GlobalAuthenticationConfigurerAdapter {

		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication()
					.withUser("user").password("password").roles("USER")
					.and()
					.withUser("admin").password("admin").roles("USER", "ADMIN")
					.and();
		}
	}
}
