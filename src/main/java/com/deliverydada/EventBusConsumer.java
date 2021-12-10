package com.deliverydada;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class EventBusConsumer implements RequestHandler<DynamodbStreamRecord, String> {
	
	public static final String TABLE_NAME="dd-customerconnect-datastore-dynamo-db-unit";
	private static final String INSERT = "INSERT";
    private static final String MODIFY = "MODIFY";

	@Override
	public String handleRequest(DynamodbStreamRecord record, Context context) {
		Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
		
		Item item = ItemUtils.toItem(newImage);
		context.getLogger().log(item.toJSONPretty());
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder
	            .standard()
	            .withRegion(record.getAwsRegion())
	            .withCredentials(new DefaultAWSCredentialsProviderChain())
	            .build();
		// Write a new item
        Map<String, AttributeValue> itemToInsert = new HashMap<>();
        itemToInsert.put("eventName", new AttributeValue().withS(item.getString("restaurantName")));
        itemToInsert.put("receivedTimeStamp", new AttributeValue().withN("5"));
        dynamoDBClient.putItem(TABLE_NAME, itemToInsert);
        context.getLogger().log("Item inserted successfully to: " + TABLE_NAME);
		return null;
	}
	
	
	public static void main(String[] args) {
		HashMap<String, AttributeValue> newImage = new HashMap<>();
		
		HashMap<String, AttributeValue> map = new HashMap<>();
		map.put("restaurantType", new AttributeValue().withS("A"));
		
		newImage.put("restaurantName", new AttributeValue().withS("ABC Restaurant"));
		newImage.put("requestId", new AttributeValue().withS("12345"));
		newImage.put("details", new AttributeValue().withM(map));
		
		System.out.println(newImage.get("restaurantName").getS());
		
		System.out.println(newImage);
		
		System.out.println(ItemUtils.toItem(newImage).get("details"));
	}
}