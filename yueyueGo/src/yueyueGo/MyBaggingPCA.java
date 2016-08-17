package yueyueGo;

import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

public class MyBaggingPCA extends Bagging {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5420728887076821323L;

	public void buildClassifier(Instances data) throws Exception {
		super.buildClassifier(data);
		cleanupPCA();

	}

	private void cleanupPCA(){
		MyPrincipalComponents myPCA=null;
		AttributeSelectedClassifier ab=null;
		for(int i=0;i<m_Classifiers.length;i++){
			ab=(AttributeSelectedClassifier)(m_Classifiers[i]);
			myPCA=(MyPrincipalComponents)ab.getEvaluator();
			myPCA.cleanUpInstanceAfterBuilt();	
		}
		 
	}
}
