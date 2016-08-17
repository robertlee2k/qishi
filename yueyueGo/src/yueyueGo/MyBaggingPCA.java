package yueyueGo;

import weka.classifiers.meta.Bagging;
import weka.core.Instances;

public class MyBaggingPCA extends Bagging {

	public void buildClassifier(Instances data) throws Exception {
		super.buildClassifier(data);
		cleanupPCA();

	}

	private void cleanupPCA(){
		MyPrincipalComponents myPCA=null;
		for(int i=0;i<m_Classifiers.length;i++){
			myPCA=(MyPrincipalComponents)(m_Classifiers[i]);
			myPCA.cleanUpInstanceAfterBuilt();	
		}
		 
	}
}
