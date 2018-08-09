package fr.irit.smac.core;

import java.io.Serializable;
import java.util.Set;

import com.mongodb.ServerAddress;

import fr.irit.smac.model.Experiment;

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
	// TODO maj
	public static final String defaultDataBaseName = "LinksDataBase";

	protected String currentXP;

	private MongoDBDelegate mongoDBDelegate;

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

	public Links(String xpName) {
		mongoDBDelegate = new MongoDBDelegate(null, xpName, defaultDataBaseName);
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
		mongoDBDelegate = new MongoDBDelegate(addr, xpName, defaultDataBaseName);
	}

	/**
	 * Create a new experiment with the given name. Drop if any other experiment
	 * with the same name already exists.
	 * 
	 * @param xpName
	 *            The name of the experiment
	 */
	@Override
	public Experiment createExperiment(String xpName) {
		return mongoDBDelegate.create(xpName);
	}

	@Override
	public Experiment getExperiment(String xpName) {
		return mongoDBDelegate.getExperiment(xpName);

	}

	/**
	 * Delete an experiment with the given name.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 */
	@Override
	public void deleteExperiment(String xpName) {
		mongoDBDelegate.delete(xpName);
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
		return mongoDBDelegate.existsExperiment(xpName);
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
		mongoDBDelegate.drop(xpName);
	}

	/**
	 * Release memory
	 */
	@Override
	public void shutdown() {
		mongoDBDelegate.closeDataBase(defaultDataBaseName);
	}

	public String getCurrentXP() {
		return currentXP;
	}

	public Set<String> getExperiments() {
		return mongoDBDelegate.getExperiencesList();
	}

	public void duplicateExperiments(String collectionNameSource, String collectionNameTarget) {
		mongoDBDelegate.duplicateExperiment(collectionNameSource, collectionNameTarget);
	}

}
