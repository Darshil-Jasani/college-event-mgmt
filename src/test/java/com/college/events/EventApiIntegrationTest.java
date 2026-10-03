package com.college.events;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventApiIntegrationTest {

    private static final String EVENT_JSON = """
        {"title":"Tech Fest","description":"Annual fest","venue":"Auditorium",
         "eventDate":"2030-01-15T10:00:00","capacity":1}
        """;

    @Autowired
    private MockMvc mvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotCreateEvent() throws Exception {
        mvc.perform(post("/api/events").with(httpBasic("student", "student123"))
                .contentType(MediaType.APPLICATION_JSON).content(EVENT_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void organizerCanCreateAndStudentCanRegisterOnce() throws Exception {
        mvc.perform(post("/api/events").with(httpBasic("organizer", "organizer123"))
                .contentType(MediaType.APPLICATION_JSON).content(EVENT_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Tech Fest"))
            .andExpect(jsonPath("$.createdBy").value("organizer"));

        mvc.perform(get("/api/events").with(httpBasic("student", "student123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Tech Fest"));
    }

    @Test
    void invalidEventIsRejected() throws Exception {
        mvc.perform(post("/api/events").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"venue\":\"x\",\"capacity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unknownEventReturns404() throws Exception {
        mvc.perform(get("/api/events/99999").with(httpBasic("admin", "admin123")))
            .andExpect(status().isNotFound());
    }
}
