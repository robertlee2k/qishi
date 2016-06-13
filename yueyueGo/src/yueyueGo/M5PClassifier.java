package yueyueGo;


//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用于中证500及全市场， 沪深300上大不合适（选股少）。
public class M5PClassifier extends ContinousClassifier {
	//方案1适合中证500
	// 1. 全市场 2008-2016最优10单元格年均14% （20单元格11%），中证500 10-20都是14%，30-50都是12%（均为不单独评估），hs300 10格有13% （20格9%） 29941/82650
	// 参数：  eval 0.1 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	
	// 2. 全市场2008-2016 20单元格年均13% （不单独评估16%）（30格15%，不单独评估14%），中证500 最优 20格17%，30格16%（均为不单独评估），hs300单独评估 20格7% （30格8%）  ----- 选择这个参数作为模型20160524
	// 参数：  eval 0.5 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5     
	//运行结果	Monthly selected_TPR mean: 26.9% standard deviation=23.9% Skewness=89.6% Kurtosis=32.8%
	//			Monthly selected_LIFT mean : 1.06
	//			Monthly selected_positive summary: 24,771
	//			Monthly selected_count summary: 71,015
	//			Monthly selected_shouyilv average: 1.1% standard deviation=8.6% Skewness=96.4% Kurtosis=41.2%
	//			Monthly total_shouyilv average: 1.0% standard deviation=6.2% Skewness=00.8% Kurtosis=09.1%
	//			mixed selected positive rate: 34.9%
	//			Monthly summary_judge_result summary: good number= 237 bad number=268

	//3. 全市场2008-2016最优20单元格年均12%，zz500 20格有13.7% ,hs300 20格5%  
	// 参数：  eval 0.9 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	//运行结果	Monthly selected_TPR mean: 24.6% standard deviation=26.3% Skewness=99.8% Kurtosis=29.5%
	//			Monthly selected_LIFT mean : 1.16
	//			Monthly selected_positive summary: 20,732
	//			Monthly selected_count summary: 56,795
	//			Monthly selected_shouyilv average: 1.1% standard deviation=8.4% Skewness=67.7% Kurtosis=13.9%
	//			Monthly total_shouyilv average: 1.0% standard deviation=6.2% Skewness=00.8% Kurtosis=09.1%
	//			mixed selected positive rate: 36.5%
	//			Monthly summary_judge_result summary: good number= 268 bad number=237
	

	// 4.  中证5002008-2016 10单元格年均 18%（20格 13%），全市场10单元15%，hs300 10格7% （单独评估）  18357/47673
	// 参数：  eval 0.9 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.7
	

	public M5PClassifier() {
		super();
		classifierName = "m5p";
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_sepeperate_eval_HS300=false;//单独为HS300评估阀值
		m_seperate_classify_HS300=false; //M5P不适用沪深300，缺省不单独评估HS300
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
				
		SAMPLE_UPPER_LIMIT = new double[] {0.07, 0.09, 0.1, 0.1, 0.1 };
		TP_FP_RATIO_LIMIT = new double[]  { 1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO到了何种值就可以停止了。
		TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
	}
	

}
