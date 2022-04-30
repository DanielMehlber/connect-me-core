package org.connectme.core.tests.userManagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.connectme.core.tests.userManagement.testUtil.TestUserDataRepository;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.api.LoginAPI;
import org.connectme.core.userManagement.beans.StatefulLoginBean;
import org.connectme.core.userManagement.beans.UserAuthenticationBean;
import org.connectme.core.userManagement.entities.PassedLoginData;
import org.connectme.core.userManagement.entities.PassedUserData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.impl.jpa.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginAPITest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserManagement userManagement;

    @Autowired
    private UserAuthenticationBean authenticationBean;

    @Autowired
    private MockMvc client;

    @BeforeEach
    public void prepare() {
        userRepository.deleteAll();
    }

    /**
     * The {@link org.connectme.core.userManagement.beans.StatefulLoginBean} is stored in session under a specific attribute
     * name called "scopedTarget.{defined name}". The name of the session attribute is defined
     * in {@link org.connectme.core.userManagement.api.LoginAPI}. Extract the object from the session.
     *
     * @param session session in which the bean is placed
     * @return instance of the {@link org.connectme.core.userManagement.beans.StatefulLoginBean}
     */
    private StatefulLoginBean extractLoginBeanFromSession(MockHttpSession session) {
        return (StatefulLoginBean) session.getAttribute("scopedTarget."+ LoginAPI.SESSION_LOGIN);
    }

    private void performUntilPhoneNumberVerification(MockHttpSession session, PassedLoginData loginData) throws Exception {
        // init login process
        client.perform(post("/users/login/init").session(session)).andExpect(status().isOk());

        // pass login data
        String json = new ObjectMapper().writeValueAsString(loginData);
        client.perform(post("/users/login/userdata").contentType("application/json").content(json).session(session))
                .andExpect(status().isOk());

        // start phone number verification
        client.perform(post("/users/login/verify/start").session(session))
                .andExpect(status().isOk());

    }

    private String performPhoneNumberVerification(MockHttpSession session) throws Exception {
        // extract verification code and pass it to API
        StatefulLoginBean bean = extractLoginBeanFromSession(session);
        String code = bean.getPhoneNumberVerification().getVerificationCode();
        String jwt = client.perform(post("/users/login/verify/check").session(session).contentType("text/plain").content(code))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        return jwt;
    }

    @Test
    public void happyPath() throws Exception {
        // -- arrange --
        PassedUserData userData = TestUserDataRepository.assembleValidPassedUserData();
        User user = new User(userData);
        userManagement.createNewUser(user);
        PassedLoginData loginData = new PassedLoginData(user.getUsername(), user.getPasswordHash());
        MockHttpSession session = new MockHttpSession();
        // -- act --
        performUntilPhoneNumberVerification(session, loginData);
        String jwt = performPhoneNumberVerification(session);

        // -- assert --
        Assertions.assertNotNull(jwt);
        Assertions.assertDoesNotThrow(() -> authenticationBean.authenticate(jwt));
    }

    // TODO: Test exceed phone number verification attempts
    // TODO: Test illegal access

}
