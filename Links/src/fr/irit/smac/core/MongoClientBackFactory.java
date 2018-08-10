package fr.irit.smac.core;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoClientBackFactory {
	private final static Map<ServerAddress, MongoClient> existingDistantMongoClients = new HashMap<>();
	private final static Map<String, MongoDatabase> existingLocalhostMongoDB = new HashMap<>();
	private final static Map<ServerAddress, Map<String, MongoDatabase>> existingDistantMongoDB = new HashMap<>();
	private static MongoClient currentLocalhostClient;

	/**
	 * addr == null => local Mongo
	 */
	public static MongoClient getMongoClientInstance(ServerAddress addr) {
		MongoClient resultClient = currentLocalhostClient;
		if (null == addr && null == currentLocalhostClient) {
			currentLocalhostClient = new MongoClient();
			resultClient = currentLocalhostClient;
		} else if (null != addr) {
			resultClient = existingDistantMongoClients.get(addr);

			if (null == resultClient) {
				resultClient = new MongoClient(addr);
				existingDistantMongoClients.put(addr, resultClient);
			}
		}
		return resultClient;
	}

	/**
	 * addr == null => local Mongo.
	 */
	public static MongoDatabase getDatabase(ServerAddress addr, String dataBaseName) {
		MongoClient mongoClient = getMongoClientInstance(addr);
		MongoDatabase databaseResult;
		if (null == addr) {
			databaseResult = existingLocalhostMongoDB.get(dataBaseName);
			if (null == databaseResult) {
				databaseResult = mongoClient.getDatabase(dataBaseName);
				existingLocalhostMongoDB.put(dataBaseName, databaseResult);
			}
		}else {
			Map<String, MongoDatabase> distantDBs = existingDistantMongoDB.get(addr);
			if (null == distantDBs) {
				databaseResult = mongoClient.getDatabase(dataBaseName);
				distantDBs = new HashMap<>();
				distantDBs.put(dataBaseName, databaseResult);
				existingDistantMongoDB.put(addr, distantDBs);
			} else {
				databaseResult = distantDBs.get(dataBaseName);
				if (null == databaseResult) {
					databaseResult = mongoClient.getDatabase(dataBaseName);
					distantDBs.put(dataBaseName, databaseResult);
				}
			}
		}

		return databaseResult;
	}

	public static void closeAllDatabases(ServerAddress addr) {
		if (null == addr) {
			existingLocalhostMongoDB.clear();
		} else {
			existingDistantMongoDB.remove(addr);
		}
	}

}
