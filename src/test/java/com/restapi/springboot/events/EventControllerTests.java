package com.restapi.springboot.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restapi.springboot.common.TestDescription;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired // Enable to test cases because web server doesn't work
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext context;

    @Before
    public void setUp() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    @TestDescription("정상적으로 이벤트 입력")
    public void createEvent() throws Exception {
        // 직렬화
        // Event event = Event.builder()
        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 30, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 11, 30, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2019, 12, 19, 11, 30))
                .endEventDateTime(LocalDateTime.of(2019, 12, 20, 11, 30))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Npee Corp.")
                .build();

        mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON_UTF8)
                    // 직렬화된 DTO 사용
                    .content(objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                // hateoas
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-events").exists())
                .andDo(document("create-event"));

    }

    @Test
    public void createEvent_Bad_Requests() throws Exception {

        Event event = Event.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,1,1,12,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,3,1,12,0))
                .beginEventDateTime(LocalDateTime.of(2020,3,11,12,0))
                .endEventDateTime(LocalDateTime.of(2020,3,12,12,0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("NPEE")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON_UTF8)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 비어있을 때")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDTO eventDTO = EventDTO.builder().build();

        this.mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못되었을 때")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {

        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,3,1,12,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,1,1,12,0))
                .beginEventDateTime(LocalDateTime.of(2020,3,12,12,0))
                .endEventDateTime(LocalDateTime.of(2020,3,11,12,0))
                .basePrice(1100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("NPEE")
                .build();

        /*
        this.mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        */
        this.mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
                .andDo(print());

    }
}
