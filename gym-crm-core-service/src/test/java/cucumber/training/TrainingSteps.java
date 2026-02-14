package cucumber.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.gym_crm_spring.dto.TraineeRegistrationRequest;
import com.gymcrm.gym_crm_spring.dto.TrainerRegistrationRequest;
import com.gymcrm.gym_crm_spring.dto.TrainingCreateRequest;
import com.gymcrm.gym_crm_spring.facade.GymFacade;
import com.gymcrm.gym_crm_spring.service.TraineeService;
import com.gymcrm.gym_crm_spring.service.TrainerService;
import com.gymcrm.gym_crm_spring.service.TrainingService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainingSteps {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private GymFacade gymFacade;
    @Autowired private TraineeService traineeService;
    @Autowired private TrainerService trainerService;
    @Autowired private TrainingService trainingService;
    @Autowired
    private Flyway flyway;

    private ResultActions latestResponse;
    private UUID lastCreatedTrainingId;
    private final String TRAINER_USER = "lilia.levada";

    @Before
    public void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Given("the system has a trainee {string}")
    public void systemHasTrainee(String username) {
        if (traineeService.findByUsername(username).isEmpty()) {
            gymFacade.registerTrainee(new TraineeRegistrationRequest(
                    "Natali",
                    "Ageeva",
                    Optional.empty(),
                    Optional.of("Melitopol")));
        }
    }

    @Given("the system has a trainer {string}")
    public void systemHasTrainer(String username) {
        if (trainerService.findByUsername(username).isEmpty()) {
            // Split "other.trainer" into ["other", "trainer"]
            String[] names = username.split("\\.");
            String firstName = names[0].substring(0, 1).toUpperCase() + names[0].substring(1);
            String lastName = (names.length > 1)
                    ? names[1].substring(0, 1).toUpperCase() + names[1].substring(1)
                    : "Trainer";

            gymFacade.registerTrainer(new TrainerRegistrationRequest(
                    firstName,
                    lastName,
                    "Yoga")); // Specialization can be generic
        }
    }

    @Given("I am authenticated as trainer {string}")
    public void iAmAuthenticatedAs(String username) {}

    @When("I create a training session with the following details:")
    public void iCreateTraining(DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        TrainingCreateRequest request = new TrainingCreateRequest(
                data.get("traineeUsername"),
                TRAINER_USER,
                data.get("trainingName"),
                LocalDate.parse(data.get("date")),
                Integer.parseInt(data.get("duration"))
        );

        latestResponse = mockMvc.perform(post("/api/training/add")
                .with(user(TRAINER_USER).roles("TRAINER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Then("the training response status should be {int}")
    public void theTrainingResponseStatusShouldBe(int statusCode) throws Exception {
        latestResponse.andExpect(status().is(statusCode));
    }

    @Given("a training session exists between {string} and {string}")
    @org.springframework.transaction.annotation.Transactional
    public void aTrainingSessionExistsBetweenAnd(String traineeUsername, String trainerUsername) {
        var request = new TrainingCreateRequest(
                traineeUsername,
                trainerUsername,
                "Setup Session",
                LocalDate.now().plusDays(1),
                60
        );
        gymFacade.addTraining(request);

        var trainings = trainingService.findByCriteriaForTrainee(
                traineeUsername, null, null, null, null
        );

        lastCreatedTrainingId = trainings.stream()
                .filter(t -> t.getTrainer().getUser().getUsername().equalsIgnoreCase(trainerUsername))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Training not found after creation"))
                .getId();
    }

    @When("I delete that training session")
    public void iDeleteThatTrainingSession() throws Exception {
        latestResponse = mockMvc.perform(delete("/api/training/{id}", lastCreatedTrainingId)
                .with(user(TRAINER_USER).roles("TRAINER")));
    }

    @When("I create a training session without credentials")
    public void iCreateTrainingNoAuth() throws Exception {
        TrainingCreateRequest request = new TrainingCreateRequest(
                "natali.ageeva", "lilia.levada", "Ghost Session", LocalDate.now(), 30);

        // No .with(user(...))
        latestResponse = mockMvc.perform(post("/api/training/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @When("I attempt to delete a training with ID {string}")
    public void iDeleteNonExistent(String id) throws Exception {
        latestResponse = mockMvc.perform(delete("/api/training/{id}", UUID.fromString(id))
                .with(user("lilia.levada").roles("TRAINER")));
    }
}
