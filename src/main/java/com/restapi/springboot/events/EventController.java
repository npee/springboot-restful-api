package com.restapi.springboot.events;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;


@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors) {

        if (errors.hasErrors()) {
            // return ResponseEntity.badRequest().build();
            return ResponseEntity.badRequest().body(errors); // Runtime Error - Need serializers
        }

        eventValidator.validate(eventDTO, errors);
        if (errors.hasErrors()) {
            // return  ResponseEntity.badRequest().build();
            return ResponseEntity.badRequest().body(errors); // Runtime Error - Need serializers
        }

        Event event = modelMapper.map(eventDTO, Event.class);

        event.update();

        Event newEvent = this.eventRepository.save(event);


        // URI createdURI = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdURI = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(newEvent);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(linkTo(EventController.class).withRel("update-events"));

        // return ResponseEntity.created(createdURI).body(newEvent);
        return ResponseEntity.created(createdURI).body(eventResource);
    }
}
