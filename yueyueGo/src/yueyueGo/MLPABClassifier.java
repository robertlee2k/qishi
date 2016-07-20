package yueyueGo;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;


public class MLPABClassifier extends NominalClassifier {

	public MLPABClassifier() {
		super();
		classifierName="mlpAB";
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		WORK_PATH =WORK_PATH+classifierName+"\\";
		WORK_FILE_PREFIX = "extData2005-2016 month-new";
		
		inputAttShouldBeIndependent=true; //这个模型是用短格式的
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		
		EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		m_sepeperate_eval_HS300=false;//太耗时间了，就不单独评估了
		m_seperate_classify_HS300=false;
		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.7; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
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
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是为MLP单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
}
