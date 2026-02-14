package cucumber.trainee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.gym_crm_spring.dto.TraineeActivationRequest;
import com.gymcrm.gym_crm_spring.dto.TraineeProfileUpdateRequest;
import com.gymcrm.gym_crm_spring.dto.TraineeRegistrationRequest;
import com.gymcrm.gym_crm_spring.facade.GymFacade;
import com.gymcrm.gym_crm_spring.service.TraineeService;
import cucumber.TestContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TraineeProfileSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private Flyway flyway;

    @Autowired
    private TestContext testContext;

    @Before
    public void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Given("the system is initialized with a trainee {string}")
    public void systemIsInitializedWithTrainee(String username) {
        if (traineeService.findByUsername(username).isEmpty()) {
            String[] names = username.split("\\.");
            String firstName = names[0];
            String lastName = (names.length > 1) ? names[1] : "Default";

            var request = new TraineeRegistrationRequest(
                    firstName,
                    lastName,
                    Optional.empty(),
                    Optional.of("Mellitopol"));
            gymFacade.registerTrainee(request);
        }
    }

    @Given("I am authenticated as trainee {string}")
    public void iAmAuthenticatedAs(String username) {
        // MockMvc security context is handled in @When steps
    }

    @When("I request my trainee profile")
    public void iRequestMyTraineeProfile() throws Exception {
        testContext.setResponse(mockMvc.perform(get("/api/trainee/profile")
                        .with(user("natali.ageeva").roles("TRAINEE")))
                .andDo(print()));
    }

    @And("the profile should contain the lastName {string}")
    public void profileShouldContainLastName(String lastName) throws Exception {
        testContext.getResponse().andExpect(jsonPath("$.lastName", equalToIgnoringCase(lastName)));
    }

    @When("I update my trainee profile with the following details:")
    public void iUpdateMyTraineeProfile(io.cucumber.datatable.DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);

        TraineeProfileUpdateRequest request = new TraineeProfileUpdateRequest(
                "natali.ageeva",
                data.get("firstName"),
                data.get("lastName"),
                Optional.empty(),
                Optional.ofNullable(data.get("address")),
                Boolean.parseBoolean(data.get("isActive"))
        );

        testContext.setResponse(mockMvc.perform(put("/api/trainee/profile")
                .with(user("natali.ageeva"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @And("the updated trainee firstName should be {string}")
    public void updatedTraineeFirstName(String firstName) throws Exception {
        testContext.getResponse().andExpect(jsonPath("$.firstName").value(firstName));
    }

    @And("the updated trainee address should be {string}")
    public void updatedTraineeAddress(String address) throws Exception {
        testContext.getResponse().andExpect(jsonPath("$.address").value(address));
    }

    @When("I send a request to set trainee activation status to {word}")
    public void iSetTraineeActivationStatus(String status) throws Exception {
        TraineeActivationRequest request = new TraineeActivationRequest(
                "natali.ageeva",
                Boolean.parseBoolean(status)
        );

        testContext.setResponse( mockMvc.perform(patch("/api/trainee/activate")
                .with(user("natali.ageeva"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @And("my trainee profile status should be {word}")
    public void verifyTraineeStatus(String status) throws Exception {
        String content = testContext.getResponse().andReturn().getResponse().getContentAsString();

        if (content.isEmpty()) {
            testContext.setResponse(mockMvc.perform(get("/api/trainee/profile")
                    .with(user("natali.ageeva").roles("TRAINEE"))));
        }
        testContext.getResponse().andExpect(jsonPath("$.isActive").value(Boolean.parseBoolean(status)));
    }

    @When("I delete my trainee profile")
    public void iDeleteMyTraineeProfile() throws Exception {
        testContext.setResponse(mockMvc.perform(delete("/api/trainee/profile")
                .with(user("natali.ageeva"))));
    }

    @And("the trainee {string} should no longer exist")
    public void traineeShouldNoLongerExist(String username) {
        org.junit.jupiter.api.Assertions.assertTrue(
                traineeService.findByUsername(username).isEmpty()
        );
    }

    @Then("the trainee response status should be {int}")
    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) throws Exception {
        testContext.getResponse().andExpect(status().is(statusCode));
    }

    @When("I request my trainee profile without credentials")
    public void iRequestProfileWithoutAuth() throws Exception {
        // Performing request without .with(user(...))
        testContext.setResponse(mockMvc.perform(get("/api/trainee/profile")));
    }

    @When("I update trainee {string} profile with the following details:")
    public void iUpdateOtherUserProfile(String targetUsername, io.cucumber.datatable.DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);

        TraineeProfileUpdateRequest request = new TraineeProfileUpdateRequest(
                targetUsername,
                data.get("firstName"),
                data.get("lastName"),
                Optional.empty(),
                Optional.empty(),
                Boolean.parseBoolean(data.get("isActive"))
        );

        testContext.setResponse(mockMvc.perform(put("/api/trainee/profile")
                .with(user("natali.ageeva").roles("TRAINEE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @When("I attempt to delete a non-existent trainee {string}")
    public void iDeleteNonExistentTrainee(String username) throws Exception {
        testContext.setResponse(mockMvc.perform(delete("/api/trainee/profile/" + username)
                .with(user("natali.ageeva").roles("TRAINEE"))));
    }

    @When("I update my trainee profile with invalid empty firstName")
    public void iUpdateWithInvalidData() throws Exception {
        TraineeProfileUpdateRequest request = new TraineeProfileUpdateRequest(
                "natali.ageeva",
                "",
                "Ageeva",
                Optional.empty(),
                Optional.empty(),
                true
        );

        testContext.setResponse(mockMvc.perform(put("/api/trainee/profile")
                .with(user("natali.ageeva").roles("TRAINEE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }
}
