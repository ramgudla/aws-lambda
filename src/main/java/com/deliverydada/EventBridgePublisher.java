package com.deliverydada;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

public class EventBridgePublisher implements RequestHandler<DynamodbEvent, Void> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	static final String EVENT_SOURCE = "dd-enterprise-restaurant-profile";
	static final String EVENT_DETAIL_TYPE = "dd-enterprise-restaurant-profile-stream-event";
	static final String EVENT_BUS_NAME = "dd-enterprise-eventbus-eventbridge-unit-1";

	@Override
	public Void handleRequest(final DynamodbEvent dynamodbEvent, final Context context) {
		context.getLogger().log("No.of events received: " + dynamodbEvent.getRecords().size());
		for (DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			context.getLogger().log(toString(record));
		}
		publish(dynamodbEvent, context);
		return null;
	}

	private void publish(final DynamodbEvent dynamodbEvent, final Context context) {
		EventBridgeClient eventBridgeClient = EventBridgeClient.builder().build();
		Instant time = Instant.now();
		List<PutEventsRequestEntry> requestEntries = dynamodbEvent.getRecords().stream()
				.map(record -> PutEventsRequestEntry.builder().time(time).source(EVENT_SOURCE)
						.eventBusName(EVENT_BUS_NAME).detailType(EVENT_DETAIL_TYPE).detail(toString(record))
						.resources(record.getEventSourceARN()).build())
				.collect(Collectors.toList());

		PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(requestEntries).build();
		PutEventsResponse result = eventBridgeClient.putEvents(eventsRequest);

		for (PutEventsResultEntry resultEntry : result.entries()) {
			if (resultEntry.eventId() != null) {
				System.out.println("Event Id: " + resultEntry.eventId());
			} else {
				System.out.println("PutEvents failed with Error Code: " + resultEntry.errorCode());
			}
		}
	}

	private String toString(final DynamodbEvent.DynamodbStreamRecord record) {
		try {
			return OBJECT_MAPPER.writeValueAsString(record);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "{}";
	}

}
