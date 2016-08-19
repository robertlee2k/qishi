package yueyueGo.classifier.nominal;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import yueyueGo.classifier.NominalClassifier;

public class RandomForestClassifier extends NominalClassifier	 {
	public RandomForestClassifier() {
		super();
		classifierName = "randomForest";
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

		RandomForest model = new RandomForest();

		int minNumObj=train.numInstances()/300;
		if (minNumObj<1000){
			minNumObj=1000; 
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);

		model.buildClassifier(train);
		System.out.println("finish buiding"+this.classifierName+" model.");

		return model;
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
