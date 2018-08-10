package fr.irit.smac.core;

import fr.irit.smac.model.Experiment;

public interface ILinks {

	boolean existsExperiment(String xpName);

	Experiment createExperiment(String xpName);

	Experiment getExperiment(String string);

	void deleteExperiment(String xpName);

	void shutdown();

}
