package yueyueGo;

import weka.classifiers.Classifier;

//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用沪深300, 全市场不大合适大熊市（因为2008年亏损大收益率偏低）
public class MLPClassifier extends NominalClassifier {
	// 1. HS300 2008-2016 20-30单元格年均10% 中证500 20/30/50  8%;全市场20/30/50  8-9% 但全市场因为2008年起步亏损大(2008净值最差0.55），累计净值不高;  全市场整体胜率 38183/98804
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer { 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.5
	//lower_limit  { 0.01, 0.01, 0.02, 0.02,0.02 } DEFAULT_THRESHOLD=0.6

	//2. HS300 2008-2016最优10-20单元格 12%（中证500 20/30/50 9%-11%） 全市场整体胜率61709/158507  
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer{ 0.2, 0.2, 0.2, 0.2, 0.2 } TP_FP_BOTTOM_LINE=0.8
	
	//3. HS300 2008-2016 10单元格10%  20格8%；中证500 20/30/50  9%-11%; 全市场20/30/50  13-10% 但全市场因为2008年起步亏损大(2008净值最差0.58），累计净值不高；  全市场整体胜率39967/102262
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer{ 0.07, 0.09, 0.11, 0.15, 0.18 } TP_FP_BOTTOM_LINE=0.8
	
	
	//*******当前选择的模型
	//4. HS300 2008-2016 10-20单元格13%;中证500 20/30/50  6%;全市场20/30/50  12-9% 但全市场因为2008年起步亏损大(2008净值最差0.55），累计净值不高;  全市场整体胜率 38,105/98,447
	// 参数：  eval 0.7 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.2, 1.2 }, UPPer{ 0.07, 0.09, 0.1, 0.13, 0.15 } TP_FP_BOTTOM_LINE=0.8
	// lower_limit  { 0.01, 0.01, 0.01, 0.01,0.01 } DEFAULT_THRESHOLD=0.7
	
	//5. HS300 2008-2016 10-20单元格10%;中证500 20/30/50 7%-8%;全市场20/30/50 6%-8% ;  全市场整体胜率 37849/97947
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer { 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.6
	// lower_limit  { 0.01, 0.01, 0.01, 0.01,0.01 } DEFAULT_THRESHOLD=0.7
	public MLPClassifier() {
		super();
		classifierName="mlp";
		inputAttShouldBeIndependent=true; //这个模型是用短格式的 		
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		
		EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.01,0.02,0.02}; //0.01,0.01 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.09, 0.1, 0.1,0.1};//0.13, 0.15 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.5, 1.2, 1.0, 1.0 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.7; // 找不出threshold时缺省值。
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//TODO 这是为MLP单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		//TODO 临时处理
		if (inputYear>=2014 )
			inputYear=2014;
//		String modelYear=null;
//		if (inputYear>=2008 && inputYear<2010)
//			modelYear="2008";
//		else if (inputYear>=2010 && inputYear<2012)
//			modelYear="2010";
//		else if (inputYear>=2012 && inputYear<2014)
//			modelYear="2012";
//		else if (inputYear>=2014 && inputYear<2015)
//			modelYear="2014";
//		else if (inputYear>=2015 && inputYear<2016)
//			modelYear="2015";
//		else if (inputYear>=2016)
//			modelYear="2016";
		String filename=this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}
}
