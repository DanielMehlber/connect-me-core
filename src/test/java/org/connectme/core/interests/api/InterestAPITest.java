package org.connectme.core.interests.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Before;
import org.connectme.core.authentication.beans.UserAuthenticationBean;
import org.connectme.core.authentication.filter.UserAuthenticationFilter;
import org.connectme.core.global.exceptions.InternalErrorException;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.impl.jpa.InterestRepository;
import org.connectme.core.interests.impl.jpa.InterestTermRepository;
import org.connectme.core.interests.testUtil.TestInterestData;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.UserDataInsufficientException;
import org.connectme.core.userManagement.impl.jpa.UserRepository;
import org.connectme.core.userManagement.testUtil.TestUserDataRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
public class InterestAPITest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthenticationBean authenticationBean;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private InterestTermRepository interestTermRepository;

    @Autowired
    private UserManagement userManagement;

    private String currentJWT;

    private User currentUser;

    @Autowired
    private MockMvc client;

    @BeforeEach
    private void prepare() throws Exception {
        // fill repository
        interestRepository.deleteAll();
        interestTermRepository.deleteAll();
        TestInterestData.fillRepository(interestRepository);

        // login user (required to access api)
        userRepository.deleteAll();
        currentUser = new User(TestUserDataRepository.assembleValidPassedUserData());
        userManagement.createNewUser(currentUser);
        currentJWT = authenticationBean.login(currentUser);
    }

    /**
     * searches for interest terms by their string value. The user will do this in order to add or search for
     * interests.
     * @throws Exception unit test failed
     */
    @Test
    public void searchTerms() throws Exception {
        // -- arrange --
        InterestTerm term = TestInterestData.getRandomInterestTerm(interestTermRepository);

        // -- act --
        String jsonResult = client.perform(get("/interests/search/term").param("term", term.getTerm()).contentType("text/plain").header("authentication", currentJWT))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // -- assert
        List<InterestTerm> interestTerms = new ObjectMapper().readValue(jsonResult, new TypeReference<List<InterestTerm>>() {});
        for(InterestTerm _term : interestTerms) {
            Assertions.assertEquals(term.getTerm(), _term.getTerm());
        }
    }

    /**
     * The access to the interest api is restricted. Every path to /interests/* must be protected by the {@link UserAuthenticationFilter}.
     * When there is no logged-in user or a JWT is not provided access should be forbidden.
     * @throws Exception unit test failed
     */
    @Test
    public void attemptUnauthorizedAccess() throws Exception {
        // -- arrange --
        authenticationBean.logout(currentUser);

        // -- act --
        // try to access anything in path /interests/*
        client.perform(get("/interests/something")).andExpect(status().isUnauthorized());
        // -- assert --
    }
}
