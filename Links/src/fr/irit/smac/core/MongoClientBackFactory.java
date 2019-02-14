package fr.irit.smac.core;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoClientBackFactory {
	private static final Map<String, MongoDatabase> existingLocalhostMongoDB = new HashMap<>();
	private static MongoClient currentLocalhostClient;

	private MongoClientBackFactory() {
	}

	/**
	 * addr == null => local Mongo
	 */
	public static MongoClient getMongoClientInstance(ServerAddress addr) {
		MongoClient resultClient = currentLocalhostClient;
		if (null == currentLocalhostClient) {
			currentLocalhostClient = new MongoClient();
			resultClient = currentLocalhostClient;
		}
		return resultClient;
	}

	@SuppressWarnings("resource")
	public static MongoDatabase getDatabase(ServerAddress addr, String dataBaseName) {
		MongoClient mongoClient = getMongoClientInstance(addr);
		MongoDatabase databaseResult = null;
		if (null == addr) {
			databaseResult = existingLocalhostMongoDB.computeIfAbsent(dataBaseName, mongoClient::getDatabase);
		}

		return databaseResult;
	}

	public static void closeAll(ServerAddress addr) {
		if (null == addr) {
			existingLocalhostMongoDB.clear();
			if (null != currentLocalhostClient) {
				currentLocalhostClient.close();
				currentLocalhostClient = null;
			}
		}
	}

	public static void trueCloseAll() {
		throw new RuntimeException("TODO uncomment");
		// existingLocalhostMongoDB.clear();
		// if (null != currentLocalhostClient) {
		// currentLocalhostClient.close();
		// currentLocalhostClient = null;
		// }
	}
}
