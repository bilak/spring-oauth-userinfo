package com.github.bilak.poc;

import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Arrays;

/**
 * @author lvasek.
 */
@Configuration
public class SecurityConfiguration {

	@Bean
	@Primary
	OAuth2RestOperations authenticationRestTemplate() {
		AccessTokenRequest atr = new DefaultAccessTokenRequest();
		OAuth2ProtectedResourceDetails resourceDetails = clientResourceDetails(defaultClientResources());
		OAuth2RestTemplate template = new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext(atr));
		template.setAccessTokenProvider(
				new AccessTokenProviderChain(Arrays.asList(new AuthorizationCodeAccessTokenProvider(),
						new ClientCredentialsAccessTokenProvider() // comment this line
				)));
		return template;
	}

	@Bean
	@Primary
	@ConfigurationProperties("security.oauth2")
	ClientResources defaultClientResources() {
		ClientResources defaultClientResources = new ClientResources();
		return defaultClientResources;
	}

	private OAuth2ProtectedResourceDetails clientResourceDetails(ClientResources resources) {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(resources.getClient().getAccessTokenUri());
		resource.setClientId(resources.getClient().getClientId());
		resource.setClientSecret(resources.getClient().getClientSecret());
		resource.setScope(resources.getClient().getScope());
		return resource;
	}

	public class ClientResources {

		@NestedConfigurationProperty
		private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

		@NestedConfigurationProperty
		private ResourceServerProperties resource = new ResourceServerProperties();

		public AuthorizationCodeResourceDetails getClient() {
			return client;
		}

		public ResourceServerProperties getResource() {
			return resource;
		}
	}

	@Configuration
	@EnableResourceServer
	@EnableOAuth2Client
	public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

		private ResourceServerProperties resourceServerProperties;
		private OAuth2RestOperations oAuth2RestOperations;

		public ResourceServerConfiguration(ResourceServerProperties resourceServerProperties,
				OAuth2RestOperations oAuth2RestOperations) {
			this.resourceServerProperties = resourceServerProperties;
			this.oAuth2RestOperations = oAuth2RestOperations;
		}

		@Bean
		ResourceServerTokenServices resourceServerTokenServices() {
			UserInfoTokenServices userInfoTokenServices =
					new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(),
							resourceServerProperties.getClientId());
			userInfoTokenServices.setRestTemplate(oAuth2RestOperations);
			return userInfoTokenServices;
		}

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
			resources.tokenServices(resourceServerTokenServices());
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @Formatter:off
			http
				.authorizeRequests()
					.anyRequest()
					.authenticated()
				.and()
			;
			// @Formatter:on
		}
	}
}
