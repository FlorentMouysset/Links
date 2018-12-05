package fr.irit.smac.model;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import fr.irit.smac.attributes.AVRTAttribute;
import fr.irit.smac.attributes.AVTAttribute;
import fr.irit.smac.attributes.DoubleAttribute;
import fr.irit.smac.attributes.StringAttribute;
import fr.irit.smac.model.Attribute.AttributeStyle;

public class Experiment {

	private MongoCollection<Document> collection;
	private String experimentName;
	private Integer currentMaxSnapshotNum;

	public Integer getCurrentMaxSnapshotNum() {
		return currentMaxSnapshotNum;
	}

	public Experiment(MongoDatabase database, String experimentName) {
		this.experimentName = experimentName;
		collection = database.getCollection(experimentName);
		Document myXP = collection.find(Filters.eq("xpName", experimentName)).first();
		if (myXP != null) {
			Iterator<Entry<String, Object>> it = myXP.entrySet().iterator();
			String _id = (String) it.next().toString();
			String xpName = (String) it.next().toString();
			this.currentMaxSnapshotNum = (Integer) it.next().getValue();
		}
	}

	public void addSnapshotToExperiment(Snapshot s) {
		collection.deleteMany(Filters.eq("snapNum", currentMaxSnapshotNum));

		Document doc = new Document("snapNum", currentMaxSnapshotNum);
		Document caract;
		Document attributeList;
		Document relationCaract;

		for (Entity a : s.getEntityList()) {
			caract = new Document("Type", "Entity").append("Name", a.getName()).append("Class", a.getType().toString())
					.append("CoorX", "" + a.getCoorX()).append("CoorY", "" + a.getCoorY());

			for (String atName : a.getAttributes().keySet()) {
				attributeList = new Document();
				for (Attribute t : a.getAttributes().get(atName)) {
					attributeList.append(t.getName(),
							new Document("TypeToDraw", t.getTypeToDraw().toString()).append("toString", t.toString()));
				}
				caract.append(atName, attributeList);
			}
			doc.append(a.getName(), caract);
		}

		for (Relation a : s.getRelations()) {
			attributeList = new Document("Type", "Relation").append("RelationName", a.getName())
					.append("A", a.getA().getName()).append("B", a.getB().getName())
					.append("isDirectionnal", a.isDirectional()).append("Class", a.getType().toString());

			for (String atName : a.getAttributes().keySet()) {
				relationCaract = new Document();
				for (Attribute t : a.getAttributes().get(atName)) {
					relationCaract.append(t.getName(),
							new Document("TypeToDraw", t.getTypeToDraw().toString()).append("toString", t.toString()));
				}
				attributeList.append(atName, relationCaract);
			}
			doc.append(a.getName(), attributeList);
		}
		collection.insertOne(doc);

		// links.newSnap(maxNum);
		currentMaxSnapshotNum++;

		BasicDBObject newDocument = new BasicDBObject().append("$inc", new BasicDBObject().append("maxNum", 1));

		collection.findOneAndUpdate(new BasicDBObject().append("xpName", this.experimentName), newDocument);

	}

	/**
	 * Return the Snapshot with the given number
	 * 
	 * @param s
	 *            The number of the snapshot
	 * @return snap
	 */
	public Snapshot getSnaptshot(long s) {
		/* recreate snapshot */
		Snapshot snap = new Snapshot();

		/* R�cup�ration de la snapshot */
		Document myDoc = collection.find(Filters.eq("snapNum", s)).first();

		if (myDoc == null)
			return null;

		Iterator<Entry<String, Object>> it = myDoc.entrySet().iterator();

		String id = (String) it.next().getValue().toString();
		Integer snapNum = (Integer) it.next().getValue();

		while (it.hasNext()) {
			Document d = (Document) it.next().getValue();
			addToSnap(snap, d);
		}

		return snap;
	}

	/**
	 * Add a Document to a Snapshot
	 * 
	 * @param snap
	 *            The Snapshot
	 * @param d
	 *            The Document
	 */
	private void addToSnap(Snapshot snap, Document d) {
		Iterator<Entry<String, Object>> it = d.entrySet().iterator();
		String type = (String) it.next().getValue();
		switch (type) {
		case "Entity":
			String name = (String) it.next().getValue();
			String uiClass = (String) it.next().getValue();
			double coorx = Double.parseDouble((String) it.next().getValue());
			double coory = Double.parseDouble((String) it.next().getValue());
			Entity a;
			if (coorx == -10000 && coory == -10000)
				a = snap.addEntity(name, uiClass);
			else
				a = snap.addEntity(name, uiClass, coorx, coory);

			while (it.hasNext()) {
				/* For any caracteristic list */
				Entry<String, Object> attributeList = it.next();
				String attListName = attributeList.getKey();
				Document list = (Document) attributeList.getValue();
				Iterator<Entry<String, Object>> entryList = list.entrySet().iterator();
				while (entryList.hasNext()) {
					/* For any caract in this list */
					Entry<String, Object> caract = entryList.next();
					String caracName = caract.getKey();
					Document value = (Document) caract.getValue();
					Iterator<Entry<String, Object>> myValues = value.entrySet().iterator();
					String typeToDraw = (String) myValues.next().getValue();
					String toString = (String) myValues.next().getValue();
					a.addOneAttribute(attListName, buildAttribute(caracName, typeToDraw, toString));
				}
			}

			break;

		case "Relation":
			name = (String) it.next().getValue();
			String A = (String) (String) it.next().getValue();
			String B = (String) it.next().getValue();
			boolean isDirectional = (boolean) it.next().getValue();
			uiClass = (String) it.next().getValue();

			Relation r = snap.addRelation(A, B, name, isDirectional, uiClass);

			while (it.hasNext()) {
				/* For any caracteristic list */
				Entry<String, Object> attributeList = it.next();
				String attListName = attributeList.getKey();
				Document list = (Document) attributeList.getValue();
				Iterator<Entry<String, Object>> entryList = list.entrySet().iterator();
				while (entryList.hasNext()) {
					/* For any caract in this list */
					Entry<String, Object> caract = entryList.next();
					String caracName = caract.getKey();
					Document value = (Document) caract.getValue();
					Iterator<Entry<String, Object>> myValues = value.entrySet().iterator();
					String typeToDraw = (String) myValues.next().getValue();
					String toString = (String) myValues.next().getValue();
					r.addOneAttribute(attListName, buildAttribute(caracName, typeToDraw, toString));
				}
			}
			break;
		}
	}

	private Attribute buildAttribute(String caracName, String typeToDraw, String toString) {
		Attribute t = null;
		if (toString.contains("Double")) {
			AttributeStyle a = AttributeStyle.LINEAR;
			switch (typeToDraw) {
			case "BAR":
				a = AttributeStyle.BAR;
			}
			t = new DoubleAttribute(caracName,
					Double.valueOf(toString.substring(toString.indexOf("=") + 2, toString.length())), a);
		} else {
			if (toString.contains("String")) {
				t = new StringAttribute(caracName, (toString.substring(toString.indexOf("=") + 2, toString.length())));
			} else {
				if (toString.contains("AVRT")) {
					String value = (toString.substring(toString.indexOf("=") + 2, toString.length()));
					Scanner sc = new Scanner(value);
					sc.useDelimiter(":");
					Double lowerValue = Double.valueOf(sc.next());
					Double downcValue = Double.valueOf(sc.next());
					Double downdelta = Double.valueOf(sc.next());
					Double upcValue = Double.valueOf(sc.next());
					Double updelta = Double.valueOf(sc.next());
					Double upperValue = Double.valueOf(sc.next());
					t = new AVRTAttribute(caracName, new AVTAttribute("Up", updelta, upcValue),
							new AVTAttribute("Down", downdelta, downcValue), upperValue, lowerValue);
				}
			}
		}
		return t;
	}

	public int getExperimentSize() {
		return getCurrentMaxSnapshotNum() + 1;
	}
}
