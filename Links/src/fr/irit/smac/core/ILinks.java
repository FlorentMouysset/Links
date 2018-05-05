package fr.irit.smac.core;

import fr.irit.smac.model.Snapshot;

public interface ILinks {

	void addSnapshot(Snapshot s);

	void addSnapshot(Snapshot s, String xpName);

	void createExperiment(String xpName);

	void createExperiment(String xpName, String pathCss);

	void deleteExperiment(String xpName);

	boolean existsExperiment(String xpName);

	void dropExperiment(String xpName);

	void informClose();

	String getMongoPath();

}
