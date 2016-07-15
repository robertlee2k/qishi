package yueyueGo;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;

// attribution Selection for M5P
public class M5PABClassifier extends ContinousClassifier {
	public M5PABClassifier() {
		super();
		classifierName = "m5pAB";
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_sepeperate_eval_HS300=false;//单独为HS300评估阀值
		m_seperate_classify_HS300=false; //M5P不适用沪深300，缺省不单独评估HS300
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 2.1, 2.0, 1.8, 1.5, 1.1 }; //{ 1.8, 1.7, 1.7, 1.0, 0.7 };//选择样本阀值时TP FP RATIO到了何种值就可以停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}
	
	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

		AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
//		CfsSubsetEval eval = new CfsSubsetEval();
//		GreedyStepwise search = new GreedyStepwise();
//		search.setSearchBackwards(true);
//		classifier.setEvaluator(eval);
//		classifier.setSearch(search);

		PrincipalComponents pca = new PrincipalComponents();
		Ranker rank = new Ranker();
		classifier.setEvaluator(pca);
		classifier.setSearch(rank);	

		M5P model = new M5P();
		int minNumObj=train.numInstances()/300;
		if (minNumObj<1000){
			minNumObj=1000; //防止树过大
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setMinNumInstances(minNumObj);
		model.setNumDecimalPlaces(6);

		classifier.setClassifier(model);
	
		classifier.buildClassifier(train);
		System.out.println("finish buiding m5p-AB model. minNumObj value:"+minNumObj);

		return classifier;
	}
}
