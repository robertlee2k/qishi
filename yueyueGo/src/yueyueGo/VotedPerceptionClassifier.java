package yueyueGo;

public class VotedPerceptionClassifier extends NominalClassifier {

	public VotedPerceptionClassifier() {
		super();
		classifierName="voted";
		WORK_PATH =WORK_PATH+classifierName+"\\";
		inputAttShouldBeIndependent=false; //这个模型是用长格式的 		
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.12, 0.15, 0.2 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.2, 1.1 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。

	}

}
