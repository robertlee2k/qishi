package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public abstract class MyClassifier {
	//统一常量
	public static final String MA_PREFIX = " MA ";
	public static final String ARFF_EXTENSION = ".arff";
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	public static final String THRESHOLD_EXTENSION = ".eval";
	public static final String TXT_EXTENSION = ".txt";
	public static final String WEKA_MODEL_EXTENSION = "-WEKA.model";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	
	//子类定义的工作路径
	public String classifierName;	
	public String WORK_PATH ;
	public String WORK_FILE_PREFIX;
	
	//用于策略分组
    public String[] m_policySubGroup;//在子类构造函数中赋值覆盖 = {"5","10","20","30","60" };
    public boolean NO_SUB_GROUP = false; //缺省为false，除非想测试混合不分组策略
    
    //用于回测中使用
	public boolean m_skipTrainInBacktest = true; //在子类构造函数中赋值覆盖
	public boolean m_skipEvalInBacktest = true;  //在子类构造函数中赋值覆盖
	public boolean m_saveArffInBacktest = false; //缺省为false
	public boolean m_sepeperate_eval_HS300=false;//单独为HS300评估阀值
	
	//无须由外界函数设置的，在各子类中近乎常量的值
	protected double EVAL_RECENT_PORTION;// 计算最近数据阀值从历史记录中选取多少比例的最近样本
	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限
	protected double[] TP_FP_RATIO_LIMIT; //各条均线TP/FP选择阀值比例上限
	protected double TP_FP_BOTTOM_LINE=0.5; //TP/FP的缺省下限
	
	//用于评估时使用
	public boolean m_seperate_classify_HS300=false;//使用单独的阀值为HS300分类。


	
	protected String model_filename;
	protected String evaluation_filename;
	
	public String getEvaluationFilename() {
		return evaluation_filename;
	}

	public void setEvaluationFilename(String evaluation_filename) {
		this.evaluation_filename = evaluation_filename;
	}

	//统计信息
	protected DescriptiveStatistics summary_selected_TPR;
	protected DescriptiveStatistics summary_selected_positive;
	protected DescriptiveStatistics summary_lift;
	protected DescriptiveStatistics summary_selected_count;
	protected DescriptiveStatistics summary_judge_result;
	
	
	public MyClassifier() {
		summary_selected_TPR= new DescriptiveStatistics();
		summary_selected_positive= new DescriptiveStatistics();
		summary_lift= new DescriptiveStatistics();
		summary_selected_count=new DescriptiveStatistics();
		summary_judge_result=new DescriptiveStatistics();
	}
	
	//一系列需要子类实现的抽象方法
	public abstract Classifier trainData(Instances train) throws Exception;
	public abstract Vector<Double> evaluateModel(Instances train,Classifier model,double sample_limit, double sample_upper,double tp_fp_ratio) throws Exception;
	protected abstract double classify(Classifier model,Instance curr) throws Exception ;
	


	
	//评估模型，eval_start_portion为0到1的值， 为0时表示利用全部Instances做评估，否则取其相应比例评估
	protected Evaluation getEvaluation(Instances train, Classifier model, double eval_start_portion)
			throws Exception {
		Instances evalTrain;
		Instances evalSamples;

		if (eval_start_portion==0){ //全样本评估
			System.out.println("evluation with full dataset, size: "+train.numInstances());
			evalTrain=train;
			evalSamples=train;
		}else{
			int evaluateFrom=new Double(train.numInstances()*eval_start_portion).intValue(); //选取开始评估的点。
			int evaluateCount=train.numInstances()-evaluateFrom;
			System.out.println("evluation Sample starts From : " + evaluateFrom+" evaluation sample size: "+evaluateCount);
			evalTrain=new Instances(train,0,evaluateFrom);  //前部分作为训练样本
			evalSamples=new Instances(train,evaluateFrom,evaluateCount);  //后部分作为评估样本
		}
			
		Evaluation eval = new Evaluation(evalTrain);
		System.out.println("evaluating.....");
		eval.evaluateModel(model, evalSamples); // evaluate on the training data to get threshold
		System.out.println(eval.toSummaryString("\nEvaluate Model Results\n\n", true));
		if (this instanceof NominalClassifier){
			System.out.println(eval.toMatrixString ("\nEvaluate Confusion Matrix\n\n"));
			System.out.println(eval.toClassDetailsString("\nEvaluate Class Details\n\n"));
		}
		return eval;
	}	
	
	// result parameter will be changed in this method!
	public  Instances predictData(Instances test, Instances result)
			throws Exception {

		
		// read classify model and header
		String modelFileName=this.getModelFileName()+MODEL_FILE_EXTENSION;
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
		Classifier model = (Classifier) v.get(0);
		Instances header = (Instances) v.get(1);
		System.out.println("Classifier Model Loaded From: "+ modelFileName);

		//读取Threshold数据文件
		@SuppressWarnings("unchecked")
		Vector<Object> v_threshold = loadEvaluationFromFile();
		double thresholdMin = ((Double) v_threshold.get(0)).doubleValue();
		double thresholdMax = ((Double) v_threshold.get(1)).doubleValue();
		System.out.println("full market thresholding value：between "	+ thresholdMin + " , "+ thresholdMax);		
		double thresholdMin_hs300=-1;
		double thresholdMax_hs300=-1;
		if (m_seperate_classify_HS300==true){
			thresholdMin_hs300=((Double) v_threshold.get(2)).doubleValue();
			//TODO 是否真的要设置上限需要评估9999;//
			thresholdMax_hs300=((Double) v_threshold.get(3)).doubleValue();
			System.out.println("HS300 index thresholding value：between "	+ thresholdMin_hs300 + " , "+ thresholdMax_hs300);
		}
		// There is additional ID attribute in test instances, so we should save it and remove before doing prediction
		double[] ids=test.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);  
		//删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		test=FilterData.removeAttribs(test,  Integer.toString(ArffFormat.ID_POSITION));
		//验证数据格式是否一致
		verifyDataFormat(test, header);
		
		//开始用分类模型和阀值进行预测
		System.out.println("actual -> predicted....... ");
		
		int positive=0;
		int negative=0;
		int selectedCount = 0;
		int selectedPositive=0;
		int selectedNegative=0;
		int testInstancesNum=test.numInstances();
		for (int i = 0; i < testInstancesNum; i++) {
			Instance curr = (Instance) test.instance(i).copy();
//			double id = curr.value(ProcessData.ID_POSITION - 1);
//			curr.setDataset(null);
//			curr.deleteAttributeAt(ProcessData.ID_POSITION - 1); // delete id
//			curr.setDataset(header); // set back data set for prediction use

			double pred=classify(model,curr);  //调用子类的分类函数

			Instance inst = new DenseInstance(result.numAttributes());
			inst.setDataset(result);
			//将相应的ID赋值回去
			inst.setValue(ArffFormat.ID_POSITION - 1, ids[i]);
			for (int n = 1; n < inst.numAttributes() - 3; n++) { // ignore the
																	// first ID.
				Attribute att = test.attribute(inst.attribute(n).name());
				// original attribute is also present in the current data set
				if (att != null) {
					if (att.isNominal()) {
						String label = test.instance(i).stringValue(att);
						int index = att.indexOfValue(label);
						if (index != -1) {
							inst.setValue(n, index);
						}
					} else if (att.isNumeric()) {
						inst.setValue(n, test.instance(i).value(att));
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
				}
			}

			inst.setValue(result.numAttributes() - 3, curr.classValue());

			if (curr.classValue()>0){
				positive++;
			}else {
				negative++;
			}
			inst.setValue(result.numAttributes() - 2, pred);

			double t_min=thresholdMin;
			double t_max=thresholdMax;
			double selected = 0.0;
			if (m_seperate_classify_HS300==true){
				if (ArffFormat.isHS300(curr)==true){
					t_min=thresholdMin_hs300;
					t_max=thresholdMax_hs300;
				}	
			}
			
			if (pred >=t_min  && pred <= t_max) {
				selected = 1.0;
				selectedCount++;
				if (curr.classValue()>0){
					selectedPositive++;
				}else {
					selectedNegative++;
				}
			}
			
			inst.setValue(result.numAttributes() - 1, selected);
			result.add(inst);
		}
		evaluateResults(positive, negative, selectedCount, selectedPositive,
				selectedNegative, testInstancesNum);
		return result;
	}

	protected void verifyDataFormat(Instances test, Instances header) throws Exception {
		System.out.println("compare model and testing data structure. Here is the difference(null means the same): "+header.equalHeadersMsg(test));
	}

	//用于评估单次分类的效果。 对于回测来说，评估的规则有以下几条：
	//1. 市场牛市时（量化定义为total_TPR>0.5)， 应保持绝对胜率（selected_TPR>0.5）且选择足够多的机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于2*20/5
	//2. 市场小牛市时（量化定义为total_TPR介于0.33与0.5之间)， 应提升胜率（final_lift>1），且保持机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于20/5
	//3. 市场小熊市时（量化定义为total_TPR介于0.2到0.33之间)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于半仓 selectedCount小于20/4/2
	//3. 市场小熊市时（量化定义为total_TPR<0.2)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于2成仓 selectedCount小于20/4/5
	protected void evaluateResults(int positive, int negative,
			int selectedCount, int selectedPositive, int selectedNegative,
			int testInstancesNum) {
		double selected_TPR=0;
		double total_TPR=0;
		double final_lift=0;
		if (selectedCount>0) 
			selected_TPR=(double)selectedPositive/selectedCount;
		if (testInstancesNum>0) 
			total_TPR=(double)positive/testInstancesNum;
		if (total_TPR>0) 
			final_lift=(double)selected_TPR/total_TPR;	
		System.out.println("*** selected count= " + selectedCount + " selected positive: " +selectedPositive + "  selected negative: "+selectedNegative); 
		System.out.println("*** total    count= "	+ testInstancesNum+ " actual positive: "+ positive + " actual negtive: "+ negative);
		System.out.println("*** selected TPR= " + FormatUtility.formatPercent(selected_TPR) + " total TPR= " +FormatUtility.formatPercent(total_TPR) + "  lift up= "+FormatUtility.formatDouble(final_lift)); 

		int resultJudgement=0;
		//评估此次成功与否
		if (total_TPR>=0.5){
			if (selected_TPR>=0.5 && selectedCount>=2*20)
				resultJudgement=1;
			else 
				resultJudgement=0;
		}else if (total_TPR>=0.33 && total_TPR<0.5){
			if (final_lift>=1&& selectedCount>=20)
				resultJudgement=1;
			else 
				resultJudgement=0;
		}else if (total_TPR>=0.2 && total_TPR<0.33){
			if (selected_TPR>0.33 || selectedCount<=20/2)
				resultJudgement=1;
			else
				resultJudgement=0;
		}else {
			if (selected_TPR>0.33 || selectedCount<=20/5)
				resultJudgement=1;
			else
				resultJudgement=0;
		}
		System.out.println("*** evaluation result for this period :"+resultJudgement);
		summary_judge_result.addValue(resultJudgement);
		summary_selected_TPR.addValue(selected_TPR);
		summary_selected_positive.addValue(selectedPositive);
		summary_selected_count.addValue(selectedCount);
		summary_lift.addValue(final_lift);
		System.out.println("Predicting finished!");
	}


	public void outputClassifySummary(){
		
		System.out.println("selected_TPR mean: "+FormatUtility.formatPercent(summary_selected_TPR.getMean()));
		System.out.println("selected_LIFT mean : "+FormatUtility.formatDouble(summary_lift.getMean()));
		System.out.println("selected_positive summary: "+FormatUtility.formatDouble(summary_selected_positive.getSum(),8,0));
		System.out.println("selected_count summary: "+FormatUtility.formatDouble(summary_selected_count.getSum(),8,0));
		System.out.println("summary_judge_result summary: good number= "+FormatUtility.formatDouble(summary_judge_result.getSum(),8,0) + " bad number=" +FormatUtility.formatDouble((summary_judge_result.getN()-summary_judge_result.getSum()),8,0));
	}
	
	public String getModelFileName() {
		return model_filename;
	}
	
	public void setModelFileName(String modelFileName) {
		model_filename=modelFileName;
	}
	
	//生成回测时使用的model文件和eval文件名称
	public void generateModelAndEvalFileName(String yearSplit,String policySplit) {
		String modelFile=this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + yearSplit + MA_PREFIX + policySplit;
		setModelFileName(modelFile);
		setEvaluationFilename(modelFile+THRESHOLD_EXTENSION);
	}
	
	
	//缺省读取model_filename的文件模型，但对于某些无法每月甚至每年生成模型的分类算法，在子类里override这个方法
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		return loadModelFromFile();
	}


	protected Classifier loadModelFromFile() throws Exception{
		String modelFileName=this.getModelFileName()+ MODEL_FILE_EXTENSION;
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
		Classifier model = (Classifier) v.get(0);
		System.out.println("Classifier Model Loaded: "+ modelFileName);
		return model;
	}	

	protected void saveModelToFiles(Classifier model, Vector<Object> v)
			throws IOException, Exception {
		String modelFileName=this.getModelFileName();
		FileUtility.write(modelFileName+"-"+this.classifierName+TXT_EXTENSION, model.toString(), "utf-8");
		SerializationHelper.write(modelFileName+MODEL_FILE_EXTENSION, v);
		SerializationHelper.write(modelFileName+WEKA_MODEL_EXTENSION, model);
		System.out.println("models saved to :"+ modelFileName);
	}
	
	protected void saveEvaluationToFile(Vector<Double> v) throws Exception {
		String evalFileName=this.getEvaluationFilename();
		SerializationHelper.write( evalFileName, v);
		System.out.println("evaluation saved to :"+ evalFileName);
	}
	
	@SuppressWarnings("rawtypes")
	protected Vector loadEvaluationFromFile()	throws Exception {
		String evalFileName=this.getEvaluationFilename();
		System.out.println("Classifier Threshold Loaded From: "+ evalFileName);
		return (Vector) SerializationHelper.read( evalFileName);
	}
	
	public void saveResultFile(Instances result) throws IOException{
		FileUtility.saveCSVFile(result, this.WORK_FILE_PREFIX+"-"+ this.classifierName + RESULT_EXTENSION);
	}
	
	public void saveSelectedFileForMarkets(Instances fullOutput) throws Exception{
		//输出全市场结果
		Instances fullMarketSelected=FilterData.getInstancesSubset(fullOutput, FilterData.WEKA_ATT_PREFIX +fullOutput.numAttributes()+" = 1");
		FileUtility.saveCSVFile(fullMarketSelected, this.WORK_FILE_PREFIX+"-"+ this.classifierName+"-full" + RESULT_EXTENSION );
		//输出沪深300
		Instances subsetMarketSelected=FilterData.filterDataForIndex(fullMarketSelected,ArffFormat.IS_HS300);
		FileUtility.saveCSVFile(subsetMarketSelected, this.WORK_FILE_PREFIX+"-"+ this.classifierName+"-hs300" + RESULT_EXTENSION );
		//输出中证300
		subsetMarketSelected=FilterData.filterDataForIndex(fullMarketSelected,ArffFormat.IS_ZZ500);
		FileUtility.saveCSVFile(subsetMarketSelected, this.WORK_FILE_PREFIX+"-"+ this.classifierName+"-zz500" + RESULT_EXTENSION );
	}
	
	// arffType="train" or "test" or "eval"
	public void saveArffFile(Instances trainingData,String arffType,String yearSplit,String policySplit) throws IOException{
		String trainingFileName = this.WORK_FILE_PREFIX + " "+arffType+" " + yearSplit + MA_PREFIX+ policySplit + ARFF_EXTENSION;
		FileUtility.SaveDataIntoFile(trainingData, trainingFileName);
	}
	

	
}
