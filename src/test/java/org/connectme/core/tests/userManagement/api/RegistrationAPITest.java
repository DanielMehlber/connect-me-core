package org.connectme.core.tests.userManagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.connectme.core.tests.userManagement.testUtil.UserDataRepository;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.api.RegistrationAPI;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.logic.StatefulRegistrationBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationAPITest {

    @Autowired
    private MockMvc client;

    @Autowired
    private UserManagement userManagement;

    @Test
    public void happyPath() throws Exception {

        /*
         * SCENARIO: test the 'happy path' with no complications or failures.
         */

        // create mock session (must be passed in client request)
        MockHttpSession session = new MockHttpSession();

        // 1) init registration
        client.perform(post("/users/registration/init").session(session)).andExpect(status().isOk());

        // 2) send user registration data
        final RegistrationUserData userData = UserDataRepository.assembleValidRegistrationUserData();
        String json = new ObjectMapper().writeValueAsString(userData);

        client.perform(post("/users/registration/set/userdata")
                        .contentType("application/json")
                        .content(json)
                        .session(session))
                .andExpect(status().isOk());

        // 3) start verification process
        client.perform(post("/users/registration/start/verify")
                        .session(session))
                .andExpect(status().isOk());

        // 4) pass verification code
        StatefulRegistrationBean registrationObject = (StatefulRegistrationBean) session.getAttribute(RegistrationAPI.SESSION_REGISTRATION);
        String code = registrationObject.getVerificationCode();

        client.perform(post("/users/registration/verify")
                        .contentType("text/plain")
                        .content(code)
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    public void attemptInvalidUserData() {}

    @Test
    public void attemptIllegalAccess() {}

    @Test
    public void exceedVerificationAttempts() {}

    @Test
    public void attemptInvalidReset() {}
}
