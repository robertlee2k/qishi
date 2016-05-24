package yueyueGo;


//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用于中证500及全市场， 沪深300上不合适。
public class M5PClassifier extends ContinousClassifier {
	
	// 1. 全市场 2008-2016最优10单元格年均15% （5单元格有27%，但估计偶然性比较大），hs300 20格可以有13% （不单独评估）
	// 参数：  eval 0.1 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	
	// 2. 全市场及 中证5002008-2016最优10单元格年均19% （5单元格有26%，但估计偶然性比较大），hs300 5单元21% ，10-20格有15% （单独评估有）
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5

	// 3.  中证5002008-2016 10单元格年均 19%，hs300 10-20格有7% （单独评估）
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.7

	// 4.  中证5002008-2016 10单元格年均 19%（20格 14%），全市场10单元15%，hs300 10格7% （单独评估）
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.7

	public M5PClassifier() {
		super();
		classifierName = "m5p";
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
				//{ 0.07, 0.09, 0.12, 0.15, 0.2 }; // 各条均线选择样本比例，按这个可以做到08-16 年均15% （全市场 20格仅参与60日线上机会）
		SAMPLE_UPPER_LIMIT = new double[] { 0.07, 0.09, 0.1, 0.1, 0.1 };//{ 0.07, 0.09, 0.11, 0.15, 0.2 };
		TP_FP_RATIO_LIMIT = new double[]  { 1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO到了何种值就可以停止了。{ 1.6, 1.4, 1.3, 1.1, 0.9 }
		TP_FP_BOTTOM_LINE=0.7; //TP/FP的下限
	}
	
//	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
//	//TODO 这是临时做的读取年初的数据
//	//全年都取同一个的模型
//	if(yearSplit.length()==6){
//		String filename=WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + yearSplit.substring(0, 4) + MA_PREFIX + policySplit;
//		this.setModelFileName(filename);
//	}
//	return loadModelFromFile();
//}
}
