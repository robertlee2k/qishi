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
	public static final String THRESHOLD_EXTENSION = ".eval";
	public static final String TXT_EXTENSION = ".txt";
	public static final String WEKA_MODEL_EXTENSION = "-WEKA.model";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	
	
	//子类定义的工作路径
	public String classifierName;	
	public String WORK_PATH ;
	public String WORK_FILE_PREFIX;
	

	public boolean inputAttShouldBeIndependent=false;  //缺省情况下，不限制输入文件中的计算字段 （在子类中覆盖）

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
//	protected String m_yearSplit; //仅供统计使用
//	protected String m_policySplit; //仅供统计使用
	//统计信息
	protected DescriptiveStatistics summary_selected_TPR;
	protected DescriptiveStatistics summary_selected_positive;
	protected DescriptiveStatistics summary_lift;
	protected DescriptiveStatistics summary_selected_count;
	protected DescriptiveStatistics summary_judge_result;
	
	protected DescriptiveStatistics summary_selectedShouyilv;
	protected DescriptiveStatistics summary_totalShouyilv;	
	public String getEvaluationFilename() {
		return evaluation_filename;
	}

	public void setEvaluationFilename(String evaluation_filename) {
		this.evaluation_filename = evaluation_filename;
	}


	public MyClassifier() {
		summary_selected_TPR= new DescriptiveStatistics();
		summary_selected_positive= new DescriptiveStatistics();
		summary_lift= new DescriptiveStatistics();
		summary_selected_count=new DescriptiveStatistics();
		summary_judge_result=new DescriptiveStatistics();
		
		summary_selectedShouyilv= new DescriptiveStatistics();
		summary_totalShouyilv= new DescriptiveStatistics();

		
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

		//读取Threshold数据文件
		@SuppressWarnings("unchecked")
		Vector<Object> v_threshold = loadEvaluationFromFile();
		double thresholdMin = ((Double) v_threshold.get(0)).doubleValue();
		//TODO 是否真的要设置上限需要评估
		double thresholdMax = 8888;//((Double) v_threshold.get(1)).doubleValue();
		System.out.println("full market thresholding value：between "	+ thresholdMin + " , "+ thresholdMax);		
		double thresholdMin_hs300=-1;
		double thresholdMax_hs300=-1;
		if (m_seperate_classify_HS300==true){
			thresholdMin_hs300=((Double) v_threshold.get(2)).doubleValue();
			//TODO 是否真的要设置上限需要评估
			thresholdMax_hs300=8888;//((Double) v_threshold.get(3)).doubleValue();
			System.out.println("HS300 index thresholding value：between "	+ thresholdMin_hs300 + " , "+ thresholdMax_hs300);
		}
		
//		//20160622临时放开预测阀值
//		if (thresholdMin>=0.218369919967342 && thresholdMin<0.22) thresholdMin=0.2;
		
		
		return predictWithThresHolds(test, result, thresholdMin, thresholdMax,
				thresholdMin_hs300, thresholdMax_hs300);
	}

	/**
	 * @param test
	 * @param result
	 * @param thresholdMin
	 * @param thresholdMax
	 * @param thresholdMin_hs300
	 * @param thresholdMax_hs300
	 * @return
	 * @throws Exception
	 * @throws IllegalStateException
	 */
	private Instances predictWithThresHolds(Instances test, Instances result,
			double thresholdMin, double thresholdMax,
			double thresholdMin_hs300, double thresholdMax_hs300)
			throws Exception, IllegalStateException {
		// read classify model and header
		String modelFileName=this.getModelFileName()+MODEL_FILE_EXTENSION;
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
		Classifier model = (Classifier) v.get(0);
		Instances header = (Instances) v.get(1);
		System.out.println("Classifier Model Loaded From: "+ modelFileName);


		// There is additional ID attribute in test instances, so we should save it and remove before doing prediction
		double[] ids=test.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);  
		//删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		test=FilterData.removeAttribs(test,  Integer.toString(ArffFormat.ID_POSITION));
		//验证数据格式是否一致
		verifyDataFormat(test, header);
		
		//开始用分类模型和阀值进行预测
		System.out.println("actual -> predicted....... ");
		

		int testInstancesNum=test.numInstances();
		DescriptiveStatistics totalPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics totalNegativeShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedNegativeShouyilv=new DescriptiveStatistics();			
		
		
		
		for (int i = 0; i < testInstancesNum; i++) {
			Instance curr = (Instance) test.instance(i).copy();
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
			inst.setValue(result.numAttributes() - 2, pred);
			
			double shouyilv=getShouyilv(i,ids[i],curr.classValue());
			
			if (shouyilv>0){
				totalPositiveShouyilv.addValue(shouyilv);
			}else {
				totalNegativeShouyilv.addValue(shouyilv);
			}


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

				if (shouyilv>0){
					selectedPositiveShouyilv.addValue(shouyilv);
				}else {
					selectedNegativeShouyilv.addValue(shouyilv);
				}
			}
			
			inst.setValue(result.numAttributes() - 1, selected);
			result.add(inst);
		}
		evaluateResults(totalPositiveShouyilv,totalNegativeShouyilv,selectedPositiveShouyilv,selectedNegativeShouyilv);
		return result;
	}

	// 对于连续分类器， 收益率就是classvalue，缺省直接返回， 对于nominal分类器，调用子类的方法获取暂存的收益率
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		return newClassValue;
	}

	protected void verifyDataFormat(Instances test, Instances header) throws Exception {
		String result=header.equalHeadersMsg(test);
		if (result!=null){
			throw new Exception("fatal error! model and testing data structure is not the same. Here is the difference: "+result);
		}else {
			System.out.println("model and testing data structure compared");
		}
	}

	//用于评估单次分类的效果。 对于回测来说，评估的规则有以下几条：
	//1. 市场牛市时（量化定义为total_TPR>0.5)， 应保持绝对胜率（selected_TPR>0.5）且选择足够多的机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于2*20/5
	//2. 市场小牛市时（量化定义为total_TPR介于0.33与0.5之间)， 应提升胜率（final_lift>1），且保持机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于20/5
	//3. 市场小熊市时（量化定义为total_TPR介于0.2到0.33之间)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于半仓 selectedCount小于20/4/2
	//3. 市场小熊市时（量化定义为total_TPR<0.2)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于2成仓 selectedCount小于20/4/5
	protected void evaluateResults(DescriptiveStatistics totalPositiveShouyilv,DescriptiveStatistics totalNegativeShouyilv,DescriptiveStatistics selectedPositiveShouyilv,DescriptiveStatistics selectedNegativeShouyilv) {
		double selected_TPR=0;
		double total_TPR=0;
		double tpr_lift=0;
		double selectedShouyilv=0.0;
		double totalShouyilv=0.0;
		double shouyilv_lift=0.0;
		long selectedPositive=selectedPositiveShouyilv.getN();
		long selectedNegative=selectedNegativeShouyilv.getN();
		long selectedCount=selectedPositive+selectedNegative;
		long positive=totalPositiveShouyilv.getN();
		long negative=totalNegativeShouyilv.getN();
		long totalCount=positive+negative;

		
		if (selectedCount>0) {
			selected_TPR=(double)selectedPositive/selectedCount;
			selectedShouyilv=(selectedPositiveShouyilv.getSum()+selectedNegativeShouyilv.getSum())/selectedCount;
		}
		if (totalCount>0) {
			total_TPR=(double)positive/totalCount;
			totalShouyilv=(totalPositiveShouyilv.getSum()+totalNegativeShouyilv.getSum())/totalCount;
		}
		if (total_TPR>0) {
			tpr_lift=selected_TPR/total_TPR;
		}

		shouyilv_lift=selectedShouyilv-totalShouyilv;

		System.out.println("*** selected count= " + selectedCount + " selected positive: " +selectedPositive + "  selected negative: "+selectedNegative); 
		System.out.println("*** total    count= "	+ totalCount+ " actual positive: "+ positive + " actual negtive: "+ negative);
		System.out.println("*** selected TPR= " + FormatUtility.formatPercent(selected_TPR) + " total TPR= " +FormatUtility.formatPercent(total_TPR) + "  lift up= "+FormatUtility.formatDouble(tpr_lift));
		
		System.out.println("*** selected average Shouyilv= " + FormatUtility.formatPercent(selectedShouyilv) + " total average Shouyilv= " +FormatUtility.formatPercent(totalShouyilv)+ "  lift difference= "+FormatUtility.formatPercent(shouyilv_lift) );
		
		
		int resultJudgement=0;
		
		// 评估收益率是否有提升是按照选择平均收益率*可买入机会数 是否大于总体平均收益率*20（按20单元格单均线情况计算）
		long buyableCount=0;
		if (selectedCount>20){
			buyableCount=20;
		}else {
			buyableCount=selectedCount;
		}
		
		//评估此次成功与否
		if (selectedShouyilv*buyableCount>=totalShouyilv*20)
			resultJudgement=1;
		else 
			resultJudgement=0;
//		if (total_TPR>=0.5){
//			if (selected_TPR>=0.5 && selectedShouyilv*buyableCount>=totalShouyilv*20)
//				resultJudgement=1;
//			else 
//				resultJudgement=0;
//		}else if (total_TPR>=0.33 && total_TPR<0.5){
//			if (tpr_lift>=1&& selectedShouyilv*buyableCount>=totalShouyilv*20)
//				resultJudgement=1;
//			else 
//				resultJudgement=0;
//		}else if (total_TPR>=0.2 && total_TPR<0.33){
//			if (selected_TPR>0.33 || selectedShouyilv*buyableCount>=totalShouyilv*20)
//				resultJudgement=1;
//			else
//				resultJudgement=0;
//		}else {
//			if (selected_TPR>0.33 || selectedShouyilv*buyableCount>=totalShouyilv*20)
//				resultJudgement=1;
//			else
//				resultJudgement=0;
//		}
		System.out.println("*** evaluation result for this period :"+resultJudgement);
		summary_judge_result.addValue(resultJudgement);
		summary_selected_TPR.addValue(selected_TPR);
		summary_selected_positive.addValue(selectedPositive);
		summary_selected_count.addValue(selectedCount);
		
		if (total_TPR!=0){ 
			summary_lift.addValue(tpr_lift);
		}else{//如果整体TPR为0则假定lift为1.
			summary_lift.addValue(1);
		}
		summary_selectedShouyilv.addValue(selectedShouyilv);
		summary_totalShouyilv.addValue(totalShouyilv);
		System.out.println("Predicting finished!");
	}


	public void outputClassifySummary(boolean writeFile) throws Exception{
		String selected_TPR_mean=FormatUtility.formatPercent(summary_selected_TPR.getMean());
		String selected_TPR_SD=FormatUtility.formatPercent(summary_selected_TPR.getStandardDeviation());
		String selected_TPR_SKW=FormatUtility.formatDouble(summary_selected_TPR.getSkewness());
		String selected_TPR_Kur=FormatUtility.formatDouble(summary_selected_TPR.getKurtosis());
		String lift_mean=FormatUtility.formatDouble(summary_lift.getMean());
		String selected_positive_sum=FormatUtility.formatDouble(summary_selected_positive.getSum(),8,0);
		String selected_count_sum=FormatUtility.formatDouble(summary_selected_count.getSum(),8,0);
		String selectedShouyilvMean = FormatUtility.formatPercent(summary_selectedShouyilv.getMean());
		String selectedShouyilvSD = FormatUtility.formatPercent(summary_selectedShouyilv.getStandardDeviation());
		String selectedShouyilvSKW = FormatUtility.formatDouble(summary_selectedShouyilv.getSkewness());
		String selectedShouyilvKUR = FormatUtility.formatDouble(summary_selectedShouyilv.getKurtosis());
		String totalShouyilvMean = FormatUtility.formatPercent(summary_totalShouyilv.getMean());
		String totalShouyilvSD = FormatUtility.formatPercent(summary_totalShouyilv.getStandardDeviation());
		String totalShouyilvSKW = FormatUtility.formatDouble(summary_totalShouyilv.getSkewness());
		String totalShouyilvKUR = FormatUtility.formatDouble(summary_totalShouyilv.getKurtosis());
		
		
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("===============================output summary=====================================");
		System.out.println("Monthly selected_TPR mean: "+selected_TPR_mean+" standard deviation="+selected_TPR_SD+" Skewness="+selected_TPR_SKW+" Kurtosis="+selected_TPR_Kur);
		System.out.println("Monthly selected_LIFT mean : "+lift_mean);
		System.out.println("Monthly selected_positive summary: "+selected_positive_sum);
		System.out.println("Monthly selected_count summary: "+selected_count_sum);
		System.out.println("Monthly selected_shouyilv average: "+selectedShouyilvMean+" standard deviation="+selectedShouyilvSD+" Skewness="+selectedShouyilvSKW+" Kurtosis="+selectedShouyilvKUR);
		System.out.println("Monthly total_shouyilv average: "+totalShouyilvMean+" standard deviation="+totalShouyilvSD+" Skewness="+totalShouyilvSKW+" Kurtosis="+totalShouyilvKUR);
		if(summary_selected_count.getSum()>0){
			System.out.println("mixed selected positive rate: "+FormatUtility.formatPercent(summary_selected_positive.getSum()/summary_selected_count.getSum()));
		}
		System.out.println("Monthly summary_judge_result summary: good number= "+FormatUtility.formatDouble(summary_judge_result.getSum(),8,0) + " bad number=" +FormatUtility.formatDouble((summary_judge_result.getN()-summary_judge_result.getSum()),8,0));
		System.out.println("===============================end of summary=====================================");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");

		if (writeFile==true){
			String header = "selected_TPR,LIFT,selected_positive,selected_count,selectedShouyilv,totalShouyilv,shouyilvDifference\r\n";
			StringBuffer strBuff = new StringBuffer();
			long size=summary_totalShouyilv.getN();
			for (int i = 0; i < size; i++) {
				strBuff.append(summary_selected_TPR.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_lift.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_selected_positive.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_selected_count.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_selectedShouyilv.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_totalShouyilv.getElement(i));
				strBuff.append(",");
				strBuff.append(summary_selectedShouyilv.getElement(i)-summary_totalShouyilv.getElement(i));
				strBuff.append("\r\n");
			}
			FileUtility.write(ProcessData.C_ROOT_DIRECTORY+"回测结果-"+this.classifierName+"-monthlySummary.csv", header+strBuff, "UTF-8");
		}
	}
	
	public String getModelFileName() {
		return model_filename;
	}
	
	public void setModelFileName(String modelFileName) {
		model_filename=modelFileName;
	}
	
	//生成回测时使用的model文件和eval文件名称
	public void generateModelAndEvalFileName(String yearSplit,String policySplit) {
//		this.m_policySplit=policySplit;
//		this.m_yearSplit=yearSplit;
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
//		SerializationHelper.write(modelFileName+WEKA_MODEL_EXTENSION, model);
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
	

	
	// arffType="train" or "test" or "eval"
	public void saveArffFile(Instances trainingData,String arffType,String yearSplit,String policySplit) throws IOException{
		String trainingFileName = this.WORK_FILE_PREFIX + " "+arffType+" " + yearSplit + MA_PREFIX+ policySplit + ARFF_EXTENSION;
		FileUtility.SaveDataIntoFile(trainingData, trainingFileName);
	}
	

	
}
