package yueyueGo;


//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用于中证500及全市场， 沪深300上大不合适（选股少）。
public class M5PClassifier extends ContinousClassifier {
	//方案1适合中证500
	// 1. 全市场 2008-2016最优10单元格年均14% （20单元格11%），中证500 10-20都是14%，30-50都是12%（均为不单独评估），hs300 10格有13% （20格9%） 29941/82650
	// 参数：  eval 0.1 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	
	// 2. 全市场2008-2016最优10单元格年均16% （20格15%，30格12%），中证500 最优10格19% 20格15%，30格13%（均为不单独评估），hs300 10格有13% （20格12% 30格11%） 23504/63522 ----- 选择这个参数作为模型20160524
	// 参数：  eval 0.5 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5     

	// 3.  中证5002008-2016 10单元格年均 18%（20格 13%），全市场10单元15%，hs300 10格7% （单独评估）  18357/47673
	// 参数：  eval 0.9 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.7

	public M5PClassifier() {
		super();
		classifierName = "m5p";
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		EVAL_RECENT_PORTION = 0.5; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
				
		SAMPLE_UPPER_LIMIT = new double[] {0.07, 0.09, 0.1, 0.1, 0.1 };//{ 0.07, 0.09, 0.11, 0.15, 0.2 };
		TP_FP_RATIO_LIMIT = new double[]  { 1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO到了何种值就可以停止了。
		TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
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
