package com.deliverydada.wip;

import java.util.ArrayList;
import java.util.List;

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


public class Handler implements RequestHandler<DynamodbEvent, Void> {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

	@Override
	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		EventBridgeClient eventBridgeClient = EventBridgeClient.builder().build();
		List<PutEventsRequestEntry> requestEntries = new ArrayList<PutEventsRequestEntry>();

		for (DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			PutEventsRequestEntry requestEntry;
			try {
				requestEntry = PutEventsRequestEntry.builder().resources(record.getEventSourceARN())
						.source(record.getEventSource()).detailType("myDetailType").eventBusName("mybus")
						.detail(toString(record)).build();
				requestEntries.add(requestEntry);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		
		PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(requestEntries).build();
		PutEventsResponse result = eventBridgeClient.putEvents(eventsRequest);
		for (PutEventsResultEntry resultEntry: result.entries()) {
		    if (resultEntry.eventId() != null) {
		        System.out.println("Event Id: " + resultEntry.eventId());
		    } else {
		        System.out.println("PutEvents failed with Error Code: " + resultEntry.errorCode());
		    }
		}  
		return null;
	}
	
	private String toString(final DynamodbEvent.DynamodbStreamRecord record) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(record);
    }

}