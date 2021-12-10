package com.deliverydada;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetRestaurantsById implements RequestStreamHandler {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	private static final String DYNAMODB_TABLE_NAME = "dd-cc-restaurant-datastore-dynamodb-unit";
	private static final String ZIP_CODE = "zipCode";
	private static final String RESTAURANT_ID = "restaurantId";

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		handleGetByParam(input, output, context);
	}

	private void handleGetByParam(InputStream inputStream, OutputStream outputStream, Context context)
			throws IOException {
		JSONParser parser = new JSONParser();
		JSONObject responseJson = new JSONObject();
		JSONObject headerJson = new JSONObject();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			JSONObject event = (JSONObject) parser.parse(reader);
			JSONObject responseBody = new JSONObject();
			String zipCode = "";
			String restaurantId = "";
			if (event.get("pathParameters") != null) {
				JSONObject pps = (JSONObject) event.get("pathParameters");
				if (pps.get(ZIP_CODE) != null) {
					zipCode = (String) pps.get(ZIP_CODE);
					if (event.get("queryStringParameters") != null) {
						JSONObject qps = (JSONObject) event.get("queryStringParameters");
						if (qps.get(RESTAURANT_ID) != null) {
							restaurantId = (String) qps.get(RESTAURANT_ID);
						}
					}
				}

				List<Item> itemList = fetchRestaurants(zipCode, restaurantId, context);
				List<String> items = new ArrayList<>();
				
				for (Item item : itemList) {
					items.add(item.toJSON());
				}

				if (itemList.size() > 0) {
					responseBody.put("items", items);
					responseJson.put("statusCode", 200);
				} else {
					responseBody.put("message",
							"We couldn’t find any restaurants near your location that we currently deliver from. Follow along as we launch in new cities.");
					responseJson.put("statusCode", 404);
				}

				responseJson.put("headers", headerJson);
				responseJson.put("body", responseBody.toString());
			}

		} catch (ParseException pex) {
			responseJson.put("exception", pex);
			responseJson.put("statusCode", 500);
		}

		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
		writer.write(responseJson.toString());
		writer.close();
	}

	private List<Item> fetchRestaurants(String zipCode, String restaurantId, Context context) {
		List<Map<String, AttributeValue>> items = new ArrayList<>();
		Map<String, String> expressionAttributesNames = new HashMap<>();
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

		String conditionExpression = "#restaurantId = :restaurantIdValue";
		expressionAttributesNames.put("#restaurantId", "restaurantId");
		expressionAttributeValues.put(":restaurantIdValue", new AttributeValue().withS(zipCode));

		/*
		 * String conditionExpression = "#zipCode = :zipCodeValue";
		 * expressionAttributesNames.put("#zipCode", "zipCode");
		 * expressionAttributeValues.put(":zipCodeValue", new
		 * AttributeValue().withS(zipCode));
		 */

		if (!isNullOrEmpty(restaurantId)) {
			conditionExpression = conditionExpression + " and #rating = :ratingValue";
			expressionAttributesNames.put("#rating", "rating");
			expressionAttributeValues.put(":ratingValue", new AttributeValue().withN(restaurantId));

			/*
			 * conditionExpression = " and #restaurantId = :restaurantIdValue";
			 * expressionAttributesNames.put("#restaurantId", "restaurantId");
			 * expressionAttributeValues.put(":restaurantIdValue", new
			 * AttributeValue().withS(restaurantId));
			 */

		}

		QueryRequest queryRequest = new QueryRequest().withTableName(DYNAMODB_TABLE_NAME)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributesNames)
				.withExpressionAttributeValues(expressionAttributeValues).withScanIndexForward(false);

		Map<String, AttributeValue> lastKey = null;
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1)
				.withCredentials(new DefaultAWSCredentialsProviderChain()).build();

		do {
			QueryResult queryResult = dynamoDBClient.query(queryRequest);
			List<Map<String, AttributeValue>> results = queryResult.getItems();
			items.addAll(results);
			lastKey = queryResult.getLastEvaluatedKey();
			queryRequest.withExclusiveStartKey(lastKey);
		} while (lastKey != null);

		return ItemUtils.toItemList(items);
	}

	// method check if string is null or empty
	private static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty() ? true : false;
	}
	
	public static void main(String[] args) {
		System.out.println(isNullOrEmpty(null));
		System.out.println(isNullOrEmpty(""));
		System.out.println(isNullOrEmpty(new String()));
		System.out.println(isNullOrEmpty("asdf"));
	}

}
