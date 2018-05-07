package fr.irit.smac.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import fr.irit.smac.model.Experiment;

public class MongoDBDelegate {

	/**
	 * Name of the MongoDB collection used by the application to list the
	 * experiments name and path to CSS files.
	 */
	private static final String collectionNameExperimentList = "xpList";

	/**
	 * The MongoClient. By default, its connects to the local host.
	 */
	private static MongoClient mongoClient;

	/**
	 * The MongoDataBase for the Links application.
	 */
	private static MongoDatabase database;

	/**
	 * The mongoPath if we want to execute it
	 */
	private String mongoPath;

	/**
	 * The path of the config
	 */
	private String mongoConfig;

	/**
	 * The file which will store the path of mongoDB
	 */
	private String resMong = "setMongo.txt";

	private final String dataBaseName;

	private final static Map<String, MongoDatabase> currentMongoClient = new HashMap<>();

	public MongoDBDelegate(ServerAddress addr, String xpName, String dataBaseName) {
		this.dataBaseName = dataBaseName;
		initMongo(xpName, addr);
	}

	private void initMongo(String xpName, ServerAddress addr) {
		lireMongoPath();
		initMongoConnection(addr);

		if (StringUtils.isNotBlank(xpName)) {
			if (!existsExperiment(xpName)) {
				create(xpName);
			}
			// this.currentXP = xpName;
		}
	}

	/**
	 * Connect Links to the MongoDB server.
	 * 
	 * @param addr
	 *            The address of the server. If null use the default address.
	 */
	private void initMongoConnection(ServerAddress addr) {
		checkMongo();
		try {
			if (null == addr) {
				mongoClient = new MongoClient();
			} else {
				mongoClient = new MongoClient(addr);
			}
			database = mongoClient.getDatabase(dataBaseName);
			currentMongoClient.put(dataBaseName, database);
		} catch (Exception e) {

			e.printStackTrace();
			System.err.println(
					"It seems that you have not a running mongoDB server. If you whish not to use mongoDB, be sure to use only the method viewSnapshot.");
		}
	}

	/**
	 * Test if an experiment with the given name has been created.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 * @return True if the experiment exists, false otherwise.
	 */
	public boolean existsExperiment(String xpName) {
		MongoCollection<Document> maCollection = database.getCollection(collectionNameExperimentList);
		Document myXP = maCollection.find(Filters.eq("xpName", xpName)).first();
		return myXP != null;
	}

	public void updateCollectionDescription(String collectionNameToAddDescription, String descriptionToAdd) {
		MongoCollection<Document> collection = database.getCollection(collectionNameToAddDescription);
		Document doc = collection.find(Filters.eq("LinksDescriptionXP", "The description")).first();
		Document newDocument = new Document("LinksDescriptionXP", "The description").append("Desc : ",
				"DescriptionOfXP " + descriptionToAdd);
		if (doc != null) {
			collection.findOneAndReplace(new BasicDBObject().append("LinksDescriptionXP", "The description"),
					newDocument);
		} else {
			collection.insertOne(newDocument);
		}
	}

	public void update(String xpName, String propertyKey, Object propertyValue) {
		MongoCollection<Document> collection = MongoDBDelegate.getDataBase(Links.defaultDataBaseName)
				.getCollection(collectionNameExperimentList);
		collection.deleteMany(Filters.eq("xpName", xpName));
		collection.insertOne(new Document("xpName", xpName).append(propertyKey, propertyValue));

	}

	public void delete(String xpName) {
		database.getCollection(xpName).drop();
		database.getCollection(collectionNameExperimentList).findOneAndDelete(Filters.eq("xpName", xpName));
	}

	public Experiment create(String xpName) {
		/*
		 * MongoCollection<Document> collection =
		 * database.getCollection(collectionNameExperimentList);
		 * collection.deleteMany(Filters.eq("xpName", xpName));
		 * collection.insertOne(new Document("xpName", xpName).append("cssFile",
		 * cssPath));
		 */

		MongoCollection<Document> collection2 = database.getCollection(xpName);
		collection2.deleteMany(Filters.eq("xpName", xpName));
		collection2.insertOne(new Document("xpName", xpName).append("maxNum", 0));
		return new Experiment(xpName);
	}

	public void drop(String xpName) {
		MongoCollection<Document> collection2 = database.getCollection(xpName);
		if (collection2 != null) {
			collection2.drop();
			collection2.insertOne(new Document("xpName", xpName).append("maxNum", 0));
		}
	}

	public Vector<String> getExperiencesList() {
		MongoCollection<Document> maCollection = database.getCollection(collectionNameExperimentList);

		Vector<String> result = new Vector<String>();
		for (Document document : maCollection.find()) {
			Iterator<Entry<String, Object>> it = document.entrySet().iterator();
			it.next();
			String xpName = (String) it.next().getValue();
			result.addElement(xpName);
		}
		return result;
	}

	public String getDescription(String collectionName) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Document doc = collection.find(Filters.eq("LinksDescriptionXP", "The description")).first();
		String result = StringUtils.EMPTY;
		if (doc != null) {
			Iterator<Entry<String, Object>> it = doc.entrySet().iterator();
			// We need to iterate 3 times
			it.next();
			it.next();
			result = it.next().getValue().toString();
		}
		return result;
	}

	/**
	 * Permet de recuperer le chemin d'acces a mongoDB si le fichier a ete
	 * rempli
	 */
	private void lireMongoPath() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(resMong));
			mongoPath = br.readLine();
			mongoConfig = br.readLine();

			br.close();
		} catch (Exception e) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(resMong));
				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Check the OS to know how to execute mongoDB. A file will be created to
	 * save the path and the configuration.
	 */
	private void checkMongo() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			if (mongoPath == null) {
				JOptionPane.showMessageDialog(null, "Can you give the path to mongod.exe ?");
				// creation
				JFileChooser dialogue = new JFileChooser("Give the path to mongod.exe");

				// showing
				dialogue.showOpenDialog(null);
				try {
					if (dialogue.getSelectedFile() == null) {
						System.exit(0);
					}
					mongoPath = dialogue.getSelectedFile().toString();
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Dialogue mongoPath error");
				}
			}
			if (mongoConfig == null) {
				Object[] options = { "Yes", "Use default" };
				int n = JOptionPane.showOptionDialog(null,
						"Can you give the path to the config of mongo or use default ", "Configuration",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 0) {
					// creation
					JFileChooser dialogue = new JFileChooser("Give the path to the config of mongo ");

					// showing
					dialogue.showOpenDialog(null);
					try {
						if (dialogue.getSelectedFile() == null) {
							System.exit(0);
						}
						mongoConfig = dialogue.getSelectedFile().toString();
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Dialogue mongoConfig error");
					}
				} else {
					mongoConfig = "DEFAULT";
				}
			}
			// Execute on Windows
			try {
				if (mongoConfig.equals("DEFAULT")) {
					String[] commande = { "\"" + mongoPath + "\"" };
					ProcessBuilder pb = new ProcessBuilder(commande);
					pb.start();
				} else {
					String[] commande = { "\"" + mongoPath + "\" --config \"" + mongoConfig + "\"" };
					ProcessBuilder pb = new ProcessBuilder(commande);
					pb.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Can't run mongod please check the path");

			}
		} else {
			// Execute on Linux
			String[] commande = { "mongod" };
			try {
				ProcessBuilder pb = new ProcessBuilder(commande);
				pb.start();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Runtime error");
			}
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(resMong));
			bw.write(mongoPath + "\n");
			bw.write(mongoConfig);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("BufferedWriter error");
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the mongoPath
	 * 
	 * @return mongoPath
	 */
	public String getMongoPath() {
		return mongoPath;
	}

	public static MongoDatabase getDataBase(String defaultdatabasename) {
		return currentMongoClient.get(defaultdatabasename);
	}

	public void closeDataBase(String defaultdatabasename) {
		mongoClient.close();
		currentMongoClient.remove(defaultdatabasename);
	}

	public Experiment getExperiment(String xpName) {
		// TODO Auto-generated method stub
		return null;
	}
}
