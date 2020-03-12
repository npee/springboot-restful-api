package com.restapi.springboot.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class EventTest {
    @Test
    public void builder() {
        Event event = Event.builder()
                .name("Spring REST API")
                .description("REST API development").build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        // Given
        String name = "Event";
        String description = "Spring REST API";

        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

    @Test
    @Parameters
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        event.update();

        assertThat(event.isFree()).isEqualTo(isFree);
    }

    private Object[] parametersForTestFree() {
        return new Object[] {
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false},
        };
    }

    @Test
    @Parameters
    public void testOffline(String loacation, boolean isOffline) {
        Event event = Event.builder()
                .location("Npee Corp.")
                .build();

        event.update();

        assertThat(event.isOffline()).isTrue();
    }

    private Object[] parametersForTestOffline() {
        return new Object[] {
                new Object[] {"강남", true},
                new Object[] {null, false},
                new Object[] {"  ", false},
        };
    }

}