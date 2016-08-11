package yueyueGo.classifier;
//attribution Selection for M5P 用主成份分析法


import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import yueyueGo.ContinousClassifier;


// 2016-07-19 选择 
// 20单元格胜率优先年化16%
//EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以停止了。
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//===============================output summary===================================== for : m5pAB
//Monthly selected_TPR mean: 25.24% standard deviation=26.90% Skewness=0.93 Kurtosis=0.08
//Monthly selected_LIFT mean : 0.78
//Monthly selected_positive summary: 9,700
//Monthly selected_count summary: 25,232
//Monthly selected_shouyilv average: 1.12% standard deviation=7.54% Skewness=4.43 Kurtosis=30.96
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 38.44%
//Monthly summary_judge_result summary: good number= 281 bad number=229
//===============================end of summary=====================================for : m5pAB


public class M5PABClassifier extends ContinousClassifier {
	public M5PABClassifier() {
		super();
		classifierName = "m5pAB";
		WORK_PATH =WORK_PATH+classifierName+"\\";
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_sepeperate_eval_HS300=false;//单独为HS300评估阀值
		m_seperate_classify_HS300=false; //M5P不适用沪深300，缺省不单独评估HS300
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
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
