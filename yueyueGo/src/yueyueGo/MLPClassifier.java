package yueyueGo;

import weka.classifiers.Classifier;

public class MLPClassifier extends NominalClassifier {
	//这个实践下来感觉比较适合HS300的选股，可以选出足够多的机会，也比较平稳
	// 1. HS300 2008-2016最优10单元格年均16%(用最大TP) 13%（用最大TPR）
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer { 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.5
	// 2. HS300 2008-2016最优10单元格年均13%（5单元格平均22%， 应该是有偶然性）
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 } TP_FP_BOTTOM_LINE=0.5
	//3. HS300 2008-2016最优10单元格年均14%（5单元格平均23%， 应该是有偶然性）  全市场整体胜率39000/100000
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer{ 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.7
	//4. HS300 2008-2016最优10单元格 17%(5单元格平均25%， 应该是有偶然性) 全市场整体胜率57732/150029
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer{ 0.2, 0.2, 0.2, 0.2, 0.2 } TP_FP_BOTTOM_LINE=0.8

	public MLPClassifier() {
		super();
		classifierName="mlp";
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.01, 0.02,0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.5, 1.2, 1.0, 1.0 };//{ 1.8, 1.5, 1.2, 1.0, 1.0 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//TODO 这是为MLP单独准备的模型
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		String modelYear=null;
		if (inputYear>=2008 && inputYear<2010)
			modelYear="2008";
		else if (inputYear>=2010 && inputYear<2012)
			modelYear="2010";
		else if (inputYear>=2012 && inputYear<2014)
			modelYear="2012";
		else if (inputYear>=2014 && inputYear<2015)
			modelYear="2014";
		else if (inputYear>=2015 && inputYear<2016)
			modelYear="2015";
		else if (inputYear>=2016)
			modelYear="2016";
		String filename=this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + modelYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}
}
