package fr.irit.smac.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import fr.irit.smac.model.Snapshot;

/**
 * Links: A tool to visualize agents and their relations over time.
 * 
 * @author Nicolas Verstaevel - nicolas.verstaevel@irit.fr
 * @version 1.0
 * @since 29/03/2017
 *
 */
/**
 * @author Bob
 *
 */
public class Links implements ILinks, Serializable {

	private static final long serialVersionUID = -8051187441385020519L;

	/**
	 * Name of the MongoDB data base used by the application.
	 */
	public static final String dataBaseName = "LinksDataBase";

	/**
	 * Name of the MongoDB collection used by the application to list the
	 * experiments name and path to CSS files.
	 */
	public static final String collectionNameExperimentList = "xpList";

	/**
	 * The MongoClient. By default, its connects to the local host.
	 */
	public static MongoClient mongoClient;

	/**
	 * The MongoDataBase for the Links application.
	 */
	public static MongoDatabase database;

	/**
	 * The mongoPath if we want to execute it
	 */
	public String mongoPath;

	/**
	 * The path of the config
	 */
	public String mongoConfig;

	/**
	 * The file which will store the path of mongoDB
	 */
	private String resMong = "setMongo.txt";

	protected String currentXP;

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor enables to start the application with the
	 * selection of the experiment name.
	 */
	public Links() {
		this(null, null);
	}

	/**
	 * Creates a new Links instance connection to the specified address of
	 * MongoDB. This constructor intializes the experiment to the name passed in
	 * parameter.
	 * 
	 * @param addr
	 *            The ServerAddress of the MongoDB database.
	 * 
	 */
	public Links(ServerAddress addr) {
		this(addr, null);
	}

	/**
	 * Creates a new Links instance connection to the specified address of
	 * MongoDB. This constructor intialise the experiment to the name passed in
	 * parameter.
	 * 
	 * @param addr
	 *            The ServerAddress of the MongoDB database.
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * 
	 */
	public Links(ServerAddress addr, String xpName) {
		lireMongoPath();
		initMongoConnection(addr);

		if (StringUtils.isNotBlank(xpName)) {
			if (!existsExperiment(xpName)) {
				createExperiment(xpName);
			}
			this.currentXP = xpName;
		}
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
		} catch (Exception e) {

			e.printStackTrace();
			System.err.println(
					"It seems that you have not a running mongoDB server. If you whish not to use mongoDB, be sure to use only the method viewSnapshot.");
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
	 * Add a new Snapshot to the model. The number of this snapshot is
	 * automatically choose.
	 * 
	 * @param s
	 *            The snapshot to add.
	 */
	@Override
	public void addSnapshot(Snapshot s) {

	}

	/**
	 * Add a new Snapshot to the model. The number of this snapshot is
	 * automatically choose.
	 * 
	 * @param s
	 *            The snapshot to add.
	 */
	@Override
	public void addSnapshot(Snapshot s, String xpName) {
	}

	/**
	 * Create a new experiment with the given name. Drop if any other experiment
	 * with the same name already exists.
	 * 
	 * @param xpName
	 *            The name of the experiment
	 */
	@Override
	public void createExperiment(String xpName) {
		MongoDBDelegate.create(xpName, this.existsExperiment(xpName));
	}

	/**
	 * Create a new experiment with the given name. Drop if any other experiment
	 * with the same name already exists.
	 * 
	 * @param xpName
	 *            The name of the experiment
	 * @Param cssPath The path to the css
	 */
	@Override
	public void createExperiment(String xpName, String cssPath) {
		MongoDBDelegate.create(xpName, cssPath);
	}

	/**
	 * Delete an experiment with the given name.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 */
	@Override
	public void deleteExperiment(String xpName) {
		MongoDBDelegate.delete(xpName);
	}

	/**
	 * Test if an experiment with the given name has been created.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 * @return True if the experiment exists, false otherwise.
	 */
	@Override
	public boolean existsExperiment(String xpName) {
		MongoCollection<Document> maCollection = Links.database.getCollection(Links.collectionNameExperimentList);
		Document myXP = maCollection.find(Filters.eq("xpName", xpName)).first();
		if (myXP != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Drop the experiment with the given name and reset the current snapNumber
	 * at 0.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 */
	@Override
	public void dropExperiment(String xpName) {
		MongoDBDelegate.drop(xpName);
	}

	/**
	 * Release memory
	 */
	@Override
	public void informClose() {
	}

	/**
	 * Static method which gets the CSS path file associated to an experiment in
	 * the MongoDB database.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 * @return The path to the CSS file.
	 */
	public static String getCssFilePathFromXpName(String xpName) {
		MongoCollection<Document> maCollection = Links.database.getCollection(Links.collectionNameExperimentList);
		Document myXP = maCollection.find(Filters.eq("xpName", xpName)).first();
		Iterator<Entry<String, Object>> it = myXP.entrySet().iterator();
		it.next(); // Skip first
		it.next(); // Skip second
		return it.next().getValue().toString();
	}

	/**
	 * Return the mongoPath
	 * 
	 * @return mongoPath
	 */
	@Override
	public String getMongoPath() {
		return mongoPath;
	}

	public String getCurrentXP() {
		return currentXP;
	}

}
