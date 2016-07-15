package yueyueGo;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;


public class MLPABClassifier extends MLPClassifier {

	public MLPABClassifier() {
		super();
		classifierName="mlpAB";
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

		AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();

		PrincipalComponents pca = new PrincipalComponents();
		Ranker rank = new Ranker();
		classifier.setEvaluator(pca);
		classifier.setSearch(rank);	

		cachedOldClassInstances=null; 
		int minNumObj=train.numInstances()/300;
		if (minNumObj<1000){
			minNumObj=1000; 
		}
		String batchSize=Integer.toString(minNumObj);
		MultilayerPerceptron model=new MultilayerPerceptron();
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);
		model.setHiddenLayers("a");

		classifier.setClassifier(model);
	
		classifier.buildClassifier(train);
		System.out.println("finish buiding mlp-AB model.");

		return classifier;
	}
}
