package com.irit.smac.core;

import com.irit.smac.attributes.DoubleAttribute;
import com.irit.smac.attributes.StringAttribute;
import com.irit.smac.model.Attribute.AttributeStyle;
import com.irit.smac.model.Entity;
import com.irit.smac.model.Relation;
import com.irit.smac.model.Snapshot;

public class Example {

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            The parameters of the application.
	 */
	public static void main(String[] args) {

		Links links = new Links("test");

		Snapshot s = new Snapshot();

		Entity a = s.addEntity("Toto", "Humain");

		a.addOneAttribute("Charct", "Age", 24);
		a.addOneAttribute("Charct", "Bonbons", 2., AttributeStyle.BAR);
		a.addOneAttribute("Charct", "Nom", "Toto");

		// a.addOneAttribute("Domain", new AVRT("VRange1", new AVT("Up", 1, 0),
		// new AVT("Down", 1, -5), 10, -10));

		Entity b = s.addEntity("Rufus", "Dog");
		Relation r = s.addRelation("Toto", "Rufus", "TotoPossedeRufus", false, "possede");

		links.addSnapshot(s);

		/* ************************ */

		Snapshot s2 = new Snapshot();

		a = s2.addEntity("Toto", "Humain");

		a.addOneAttribute("Charct", "Age", 24);
		a.addOneAttribute("Charct", "Bonbons", 2., AttributeStyle.BAR);
		a.addOneAttribute("Charct", "Nom", "Toto");

		// a.addOneAttribute("Domain", new AVRT("VRange1", new AVT("Up", 1, 0),
		// new AVT("Down", 1, -5), 10, -10));

		b = s2.addEntity("Rufus", "Dog");
		s2.addRelation("Toto", "Rufus", "TotoPossedeRufus", false, "possede");

		Entity c = s2.addEntity("Luna", "Cat");
		((DoubleAttribute) s2.getEntity("Toto").getAttributesWithName("Age")).setValue(32);
		((DoubleAttribute) s2.getEntity("Toto").getAttributesWithName("Bonbons")).setValue(32);

		s2.addRelation("Luna", "Rufus", "LR", true, "seBattre");
		s2.addRelation("Toto", "Luna", "LU", true, "seBattre");
		links.addSnapshot(s2);
	}
}
