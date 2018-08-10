package fr.irit.smac.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import fr.irit.smac.model.Experiment;
import fr.irit.smac.model.Snapshot;

public class MongoDBDelegate {

	private static final String SPECIAL_DOC_NAME = "xpName";

	/**
	 * The MongoClient. By default, its connects to the local host.
	 */
	private MongoClient mongoClient;

	/**
	 * The MongoDataBase for the Links application.
	 */
	private MongoDatabase database;

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

	private ServerAddress addr;

	// private final String dataBaseName;


	public MongoDBDelegate(ServerAddress addr, String xpName, String dataBaseName) {
		initMongo(xpName, addr, dataBaseName);
	}

	private void initMongo(String xpName, ServerAddress addr, String dataBaseName) {
		lireMongoPath();
		this.addr = addr;
		initMongoConnection(addr, dataBaseName);

		initExperiment(xpName);
	}

	private void initExperiment(String xpName) {
		if (StringUtils.isNotBlank(xpName)) {
			if (!existsExperiment(xpName)) {
				create(xpName);
			}
		}
	}

	/**
	 * Connect Links to the MongoDB server.
	 * 
	 * @param addr
	 *            The address of the server. If null use the default address.
	 * @param dataBaseName
	 */
	private void initMongoConnection(ServerAddress addr, String dataBaseName) {
		checkMongo();
		try {
			mongoClient = MongoClientBackFactory.getMongoClientInstance(addr);
			database = MongoClientBackFactory.getDatabase(addr, dataBaseName);

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
		return null != database.getCollection(xpName);
	}

	// public void updateCollectionDescription(String
	// collectionNameToAddDescription, String descriptionToAdd) {
	// MongoCollection<Document> collection =
	// database.getCollection(collectionNameToAddDescription);
	// Document doc = collection.find(Filters.eq("LinksDescriptionXP", "The
	// description")).first();
	// Document newDocument = new Document("LinksDescriptionXP", "The
	// description").append("Desc : ",
	// "DescriptionOfXP " + descriptionToAdd);
	// if (doc != null) {
	// collection.findOneAndReplace(new
	// BasicDBObject().append("LinksDescriptionXP", "The description"),
	// newDocument);
	// } else {
	// collection.insertOne(newDocument);
	// }
	// }

	public void update(String xpName, String propertyKey, Object propertyValue) {
		MongoCollection<Document> collection = database.getCollection(xpName);
		// collection.deleteMany(Filters.eq("xpName", xpName));
		collection.insertOne(new Document(SPECIAL_DOC_NAME, xpName).append(propertyKey, propertyValue));

	}

	public void delete(String xpName) {
		database.getCollection(xpName).drop();
		// database.getCollection(collectionNameExperimentList).findOneAndDelete(Filters.eq(SPECIAL_DOC_NAME,
		// xpName));
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
		collection2.deleteMany(Filters.eq(SPECIAL_DOC_NAME, xpName));
		collection2.insertOne(new Document(SPECIAL_DOC_NAME, xpName).append("maxNum", 0));
		return new Experiment(database, xpName);
	}

	public Set<String> getExperiencesList() {
		ListCollectionsIterable<Document> allDocs = database.listCollections();
		Set<String> result = new HashSet<String>();

		MongoCursor<Document> ite = allDocs.iterator();
		while (ite.hasNext()) {
			Document doc = ite.next();
			result.add(doc.getString("name"));
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


	public void closeConnexion() {
		MongoClientBackFactory.closeAll(addr);
		mongoClient = null;
	}

	public Experiment getExperiment(String experimentName) {
		Experiment result = new Experiment(database, experimentName);
		return result;
	}

	public void duplicateExperiment(String collectionNameSource, String collectionNameTarget) {
		Experiment experimentSource = getExperiment(collectionNameSource);
		Experiment experimentTarget = create(collectionNameTarget);
		for (int currentSnapnum = 0; currentSnapnum < experimentSource.getExperimentSize(); currentSnapnum++) {
			Snapshot snap = experimentSource.getSnaptshot(currentSnapnum);
			if (null != snap) {
				experimentTarget.addSnapshotToExperiment(snap);
			}
		}
	}
}
