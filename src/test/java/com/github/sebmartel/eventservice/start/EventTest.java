package com.github.sebmartel.eventservice.start;

import static org.junit.Assert.*;

import java.io.IOException;

import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventTest {

	@Before
	public void setUp() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public void testSerializationDeserialization() throws IOException {
		
		Item item = new Item(1, 2);
		Event ev = new Event(item);
		
		ObjectMapper om = new ObjectMapper();
		String json = om.writeValueAsString(ev);
		assertThat(json, is("{\"item\":{\"timestamp\":1,\"id\":2}}"));
		
		Event[] evs = { ev, ev };		
		String jsonArray = om.writeValueAsString(evs);
		assertThat(jsonArray, is("[{\"item\":{\"timestamp\":1,\"id\":2}},{\"item\":{\"timestamp\":1,\"id\":2}}]"));
		
		Event in = om.readValue("{\"item\":{\"timestamp\":1,\"id\":2}}", Event.class);
		assertThat(in, equalTo(ev));
		
	    Event[] ins = om.readValue("[{\"item\":{\"timestamp\":1,\"id\":2}},{\"item\":{\"timestamp\":1,\"id\":2}}]", Event[].class);
	    assertThat(ins, equalTo(evs));
	}
}
