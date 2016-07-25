package it.infn.mw.iam.registration;

import static com.jayway.restassured.RestAssured.given;
import static it.infn.mw.iam.test.RegistrationUtils.createRegistrationRequest;
import static it.infn.mw.iam.test.RegistrationUtils.deleteUser;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import it.infn.mw.iam.IamLoginService;
import it.infn.mw.iam.core.IamRegistrationRequestStatus;
import it.infn.mw.iam.test.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IamLoginService.class)
@WebIntegrationTest
public class RegistrationTests {

  @Autowired
  private PersistentUUIDTokenGenerator generator;

  @BeforeClass
  public static void init() {

    TestUtils.initRestAssured();
  }

  @Test
  public void testCreateRequest() {

    RegistrationRequestDto reg = createRegistrationRequest("test_create");

    Assert.notNull(reg);

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testListNewRequests() {

    String accessToken =
        TestUtils.getAccessToken("registration-client", "secret", "registration:read");

    RegistrationRequestDto reg = createRegistrationRequest("test_list_new");

    // @formatter:off
    given()
      .port(8080)
      .auth()
        .preemptive()
        .oauth2(accessToken)
      .param("status", IamRegistrationRequestStatus.NEW)
    .when()
      .get("/registration")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body("size()", Matchers.greaterThanOrEqualTo(1));
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testListRequestsUnauthorized() {

    // @formatter:off
    given()
      .port(8080)
      .param("status", IamRegistrationRequestStatus.NEW)
    .when()
      .get("/registration")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.UNAUTHORIZED.value());
    // @formatter:on
  }

  @Test
  public void testConfirmRequest() {

    RegistrationRequestDto reg = createRegistrationRequest("test_confirm");

    String token = generator.getLastToken();
    Assert.notNull(token);

    confirmRegistrationRequest(token);

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testConfirmRequestFailureWithWrongToken() {

    RegistrationRequestDto reg = createRegistrationRequest("test_confirm_fail");

    String badToken = "abcdefghilmnopqrstuvz";

    // @formatter:off
    given().port(8080).pathParam("token", badToken).when().post("/registration/confirm/{token}")
        .then().log().body(true).statusCode(HttpStatus.NOT_FOUND.value());
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testApproveRequest() {

    String accessToken =
        TestUtils.getAccessToken("registration-client", "secret", "registration:write");

    // create new request
    RegistrationRequestDto reg = createRegistrationRequest("test_approve");
    Assert.notNull(reg);

    String token = generator.getLastToken();
    Assert.notNull(token);

    // confirm
    confirmRegistrationRequest(token);

    // approve it
    // @formatter:off
    given()
      .port(8080)
      .auth()
        .preemptive()
        .oauth2(accessToken)
      .pathParam("uuid", reg.getUuid())
      .pathParam("decision", IamRegistrationRequestStatus.APPROVED.name())
    .when()
      .post("/registration/{uuid}/{decision}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body("status", Matchers.equalTo(IamRegistrationRequestStatus.APPROVED.name()))
      .body("uuid", Matchers.equalTo(reg.getUuid()));
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testRejectRequest() {

    String accessToken =
        TestUtils.getAccessToken("registration-client", "secret", "registration:write");

    // create new request
    RegistrationRequestDto reg = createRegistrationRequest("test_reject");
    Assert.notNull(reg);

    String token = generator.getLastToken();
    Assert.notNull(token);

    // confirm
    confirmRegistrationRequest(token);

    // @formatter:off
    // reject it
    given()
      .port(8080)
        .auth()
          .preemptive()
          .oauth2(accessToken)
      .pathParam("uuid", reg.getUuid())
      .pathParam("decision", IamRegistrationRequestStatus.REJECTED.name())
    .when()
      .post("/registration/{uuid}/{decision}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body("status", Matchers.equalTo(IamRegistrationRequestStatus.REJECTED.name()))
      .body("uuid", Matchers.equalTo(reg.getUuid()));
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testApproveRequestNotConfirmedFailure() {

    String accessToken =
        TestUtils.getAccessToken("registration-client", "secret", "registration:write");

    // create new request
    RegistrationRequestDto reg = createRegistrationRequest("test_approve_fail");
    Assert.notNull(reg);

    // @formatter:off
    // approve it without confirm
    given()
      .port(8080)
      .auth()
        .preemptive()
        .oauth2(accessToken)
      .pathParam("uuid", reg.getUuid())
      .pathParam("decision", IamRegistrationRequestStatus.APPROVED.name())
    .when()
      .post("/registration/{uuid}/{decision}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.BAD_REQUEST.value());
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testApproveRequestUnauthorized() {

    // create new request
    RegistrationRequestDto reg = createRegistrationRequest("test_approve_unauth");
    Assert.notNull(reg);

    String token = generator.getLastToken();
    Assert.notNull(token);

    // confirm
    confirmRegistrationRequest(token);

    // approve it
    // @formatter:off
    given()
      .port(8080)
      .pathParam("uuid", reg.getUuid())
      .pathParam("decision", IamRegistrationRequestStatus.APPROVED.name())
    .when()
      .post("/registration/{uuid}/{decision}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.UNAUTHORIZED.value());
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testWrongDecisionFailure() {

    String accessToken =
        TestUtils.getAccessToken("registration-client", "secret", "registration:write");

    // create new request
    RegistrationRequestDto reg = createRegistrationRequest("test_wrong_decision");
    Assert.notNull(reg);

    String token = generator.getLastToken();
    Assert.notNull(token);

    // confirm
    confirmRegistrationRequest(token);

    // approve it
    // @formatter:off
    given()
      .port(8080)
      .auth()
        .preemptive()
        .oauth2(accessToken)
      .pathParam("uuid", reg.getUuid())
      .pathParam("decision", "wrong")
    .when()
      .post("/registration/{uuid}/{decision}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.BAD_REQUEST.value());
    // @formatter:on

    deleteUser(reg.getAccountId());
  }

  @Test
  public void testUsernameAvailable() {
    String username = "tester";
    // @formatter:off
    given()
      .port(8080)
      .pathParam("username", username)
    .when()
      .get("registration/username-available/{username}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body(Matchers.equalTo("true"))
    ;
    // @formatter:on
  }

  @Test
  public void testUsernameAlreadyTaken() {
    String username = "admin";
    // @formatter:off
    given()
      .port(8080)
      .pathParam("username", username)
    .when()
      .get("registration/username-available/{username}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body(Matchers.equalTo("false"))
    ;
    // @formatter:on
  }

  private void confirmRegistrationRequest(final String token) {

    // @formatter:off
    given()
      .port(8080)
      .pathParam("token", token)
    .when()
      .post("/registration/confirm/{token}")
    .then()
      .log()
        .body(true)
      .statusCode(HttpStatus.OK.value())
      .body("status", Matchers.equalTo(IamRegistrationRequestStatus.CONFIRMED.name()))
    ;
    // @formatter:on
  }

}
