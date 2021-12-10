package com.deliverydada.wip;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

public class GetRestaurants implements RequestHandler<String, List> {
	private static final String TABLE_NAME = "dd-cc-restaurant-datastore-dynamodb-unit";

	@Override
	public List handleRequest(String restaurantId, Context context) {
		return fetchRestaurants(restaurantId);
	}

	public List<Map<String, AttributeValue>> fetchRestaurants(String restaurantId) {

		List<Map<String, AttributeValue>> items = new ArrayList<>();

		Map<String, String> expressionAttributesNames = new HashMap<>();
		expressionAttributesNames.put("#restaurantId", "restaurantId");

		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":restaurantIdValue", new AttributeValue().withS(restaurantId));

		QueryRequest queryRequest = new QueryRequest().withTableName(TABLE_NAME)
				.withKeyConditionExpression("#restaurantId = :restaurantIdValue")
				.withExpressionAttributeNames(expressionAttributesNames)
				.withExpressionAttributeValues(expressionAttributeValues).withScanIndexForward(false);

		Map<String, AttributeValue> lastKey = null;
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion("ap-south-1")
				.withCredentials(new DefaultAWSCredentialsProviderChain()).build();

		do {
			QueryResult queryResult = dynamoDBClient.query(queryRequest);
			List<Map<String, AttributeValue>> results = queryResult.getItems();
			items.addAll(results);
			lastKey = queryResult.getLastEvaluatedKey();
			queryRequest.withExclusiveStartKey(lastKey);
		} while (lastKey != null);

		return items;
	}

}
