package it.infn.mw.iam.test.oauth;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Sets;

import it.infn.mw.iam.IamLoginService;
import it.infn.mw.iam.core.web.IamDiscoveryEndpoint;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IamLoginService.class)
@WebAppConfiguration
@Transactional
public class DiscoveryEndpointTests {

  private String endpoint = "/" + IamDiscoveryEndpoint.OPENID_CONFIGURATION_URL;

  private Set<String> iamSupportedGrants = Sets.newLinkedHashSet(Arrays.asList("authorization_code",
      "implicit", "refresh_token", "client_credentials", "password",
      "urn:ietf:params:oauth:grant-type:jwt-bearer", "urn:ietf:params:oauth:grant_type:redelegate",
      "urn:ietf:params:oauth:grant-type:token-exchange"));

  private static final String IAM_ORGANISATION_NAME_CLAIM = "organisation_name";
  private static final String IAM_GROUPS_CLAIM = "groups";
  private static final String IAM_EXTERNAL_AUTHN_CLAIM = "external_authn";

  @Autowired
  private WebApplicationContext context;

  private MockMvc mvc;

  @Before
  public void setup() throws Exception {
    mvc = MockMvcBuilders.webAppContextSetup(context)
      .apply(springSecurity())
      .alwaysDo(print())
      .build();
  }

  @Test
  public void testGrantTypesSupported() throws Exception {

    // @formatter:off
    mvc.perform(post(endpoint))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grant_types_supported").isNotEmpty())
        .andExpect(jsonPath("$.grant_types_supported").isArray())
        .andExpect(jsonPath("$.grant_types_supported").value(containsInAnyOrder(iamSupportedGrants.toArray())));
    // @formatter:on
  }

  @Test
  public void testSupportedClaims() throws Exception {

    // @formatter:off
    mvc.perform(post(endpoint))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.claims_supported").isNotEmpty())
        .andExpect(jsonPath("$.claims_supported").isArray())
        .andExpect(jsonPath("$.claims_supported", hasItem(IAM_ORGANISATION_NAME_CLAIM)))
        .andExpect(jsonPath("$.claims_supported", hasItem(IAM_GROUPS_CLAIM)))
        .andExpect(jsonPath("$.claims_supported", hasItem(IAM_EXTERNAL_AUTHN_CLAIM)));
    // @formatter:on
  }
}
