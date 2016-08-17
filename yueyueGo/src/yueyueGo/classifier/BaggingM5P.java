package yueyueGo.classifier;

import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import yueyueGo.ContinousClassifier;

public class BaggingM5P extends ContinousClassifier {

	public BaggingM5P() {
		super();
		classifierName = "baggingM5P";
		WORK_PATH =WORK_PATH+classifierName+"\\";
		m_noCaculationAttrib=false; //添加计算字段
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
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
		//设置基础的m5p classifier参数
		AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
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
		

	    // set up the bagger and build the classifier
	    Bagging bagger = new Bagging();
	    bagger.setClassifier(model);
	    bagger.setNumIterations(10);
	    bagger.setNumExecutionSlots(3);
	    bagger.buildClassifier(train);
		return bagger;
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
}
