/**********************************************
//                   _ooOoo_
//                  o8888888o
//                  88" . "88
//                  (| -_- |)
//                  O\  =  /O
//               ____/`---'\____
//             .'  \\|     |//  `.
//            /  \\|||  :  |||//  \
//           /  _||||| -:- |||||-  \
//           |   | \\\  -  /// |   |
//           | \_|  ''\---/''  |   |
//           \  .-\__  `-`  ___/-. /
//         ___`. .'  /--.--\  `. . __
//      ."" '<  `.___\_<|>_/___.'  >'"".
//     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//     \  \ `-.   \_ __\ /__ _/   .-` /  /
//======`-.____`-.___\_____/___.-`____.-'======
//                   `=---='
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//             菩萨保佑       平安运转
//             纵有 BUG   也不亏钱
************************************************/
package yueyueGo;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ProcessData {

	
	public static final String C_ROOT_DIRECTORY = "C:\\Users\\robert\\Desktop\\提升均线策略\\";
	public static final String NOMINAL_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"models\\01-二分类器\\";
	public static final String CONTINOUS_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"models\\02-连续分类器\\";
	public static final String BACKTEST_RESULT_DIR=C_ROOT_DIRECTORY+"testResult\\";
	public static final String PREDICT_WORK_DIR=C_ROOT_DIRECTORY+"03-预测模型\\";
	public static final String TRANSACTION_ARFF_PREFIX="AllTransaction20052016-ext";
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	
//	public static final String MLP_PREDICT_MODEL= "\\交易分析2005-2016 by month-new-mlp-201605 MA ";
//	public static final String MLP_EVAL_MODEL= "\\交易分析2005-2016 by month-new-mlp-201605 MA ";
//	public static final String M5P_PREDICT_MODEL="\\交易分析2005-2016 by month-new-m5p-201605 MA ";
//	public static final String M5P_EVAL_MODEL="\\交易分析2005-2016 by month-new-m5p-201605 MA ";

	public static final String MLP_PREDICT_MODEL= "\\extData2005-2016 month-new-mlp-2016 MA ";
	public static final String MLP_EVAL_MODEL= "\\extData2005-2016 month-new-mlp-201606 MA ";
	public static final String M5P_PREDICT_MODEL="\\extData2005-2016-m5p-201606 MA ";
	public static final String M5P_EVAL_MODEL="\\extData2005-2016-m5p-201606 MA ";
	
	//经过主成分分析后的数据
	public static final String M5PAB_PREDICT_MODEL="\\extData2005-2016-m5pAB-201606 MA ";
	public static final String M5PAB_EVAL_MODEL="\\extData2005-2016-m5pAB-201606 MA ";
	public static final String MLPAB_PREDICT_MODEL="\\extData2005-2016 month-new-mlpAB-2016 MA ";
	public static final String MLPAB_EVAL_MODEL="\\extData2005-2016 month-new-mlpAB-201606 MA ";
	
	public static final String[] splitYear ={
//		"2008","2009","2010","2011","2012","2013","2014","2015","2016"
		  "200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606"
		};

	public static void main(String[] args) {
		try {
			//用模型预测每日增量数据
//			callDailyPredict();

			//调用回测函数回测
			callTestBack();
			
			//用最新的单次交易数据，更新原始的交易数据文件
//			callRefreshInstances();

//			compareRefreshedInstancesForYear(2016,100);
			//为原始的历史文件Arff添加计算变量，并分拆，因为其数据量太大，所以提前处理，不必每次分割消耗内存
//			processHistoryFile();
			
			//合并历史扩展数据
//			mergeExtData();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}



	/**
	 * @throws Exception
	 */
	protected static void callRefreshInstances() throws Exception {
		int startYear=2016;
		int endYear=2016;
		refreshArffFile(startYear,endYear);
		compareRefreshedInstancesForYear(2010,100);
		compareRefreshedInstancesForYear(2016,5);
	}



	/**
	 * @throws Exception
	 */
	protected static void callDailyPredict() throws Exception {
		//用二分类模型预测每日增量数据
//		MLPClassifier nModel=new MLPClassifier();
		
		MLPABClassifier nModel=new MLPABClassifier();
		predictWithDB(nModel,PREDICT_WORK_DIR);
		
		//用连续模型预测每日增量数据
//		M5PClassifier cModel=new M5PClassifier();
//		predictWithDB(cModel,PREDICT_WORK_DIR);
		
		M5PABClassifier cABModel=new M5PABClassifier();
		//读取数据库预测
		predictWithDB(cABModel,PREDICT_WORK_DIR);

//			//使用文件预测
//			String dataFileName=("t_stock_avgline_increment_zuixin_v"+FormatUtility.getDateStringFor(-1)).trim();
//			//用二分类模型预测每日增量数据
//			MLPClassifier nModel=new MLPClassifier();
//			predictWithFile(nModel,PREDICT_WORK_DIR,dataFileName);
//			//用连续模型预测每日增量数据
//			M5PClassifier cModel=new M5PClassifier();
//			predictWithFile(cModel,PREDICT_WORK_DIR,dataFileName);
	}



	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected static void callTestBack() throws Exception, IOException {
		//按二分类器回测历史数据
		//	投票感知器
//		VotedPerceptionClassifier nModel = new VotedPerceptionClassifier();
//		Instances nominalResult=testBackward(nModel);

		//REP树（C45树的变种，规则过于简单）
//		REPTreeClassifier nModel = new REPTreeClassifier();
//		Instances nominalResult=testBackward(nModel);

		//神经网络
//		MLPClassifier nModel = new MLPClassifier();
		MLPABClassifier nModel = new MLPABClassifier();
		Instances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		Instances nominalResult=loadBackTestResultFromFile(nModel.classifierName);

		//按连续分类器回测历史数据
//		M5PClassifier cModel=new M5PClassifier();
		M5PABClassifier cModel=new M5PABClassifier();
//		Instances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
		Instances continuousResult=loadBackTestResultFromFile(cModel.classifierName);
		
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		Instances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.arff_format);
		saveSelectedFileForMarkets(m5pOutput,cModel.classifierName);
		Instances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.arff_format);
		saveSelectedFileForMarkets(mlpOutput,nModel.classifierName);
	}



	// replaced by compareRefreshedInstances which is more effecient 
	//此方法用于比较原始文件和refreshed文件之间的差异
	// 根据原始文件的格式ORIGINAL_TRANSACTION_ARFF_FORMAT
	//TRADE_DATE （2）,"code"（3） 和 SELECTED_MA（9） 是唯一性键值
	// checkSample 是指抽样率，如果是1表示每行都要比较，如果是100，则为每100类记录中抽取一条比较 
	protected static void compareRefreshedInstancesForYear(int year,int checkSample) throws Exception {
		
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + year + "01) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ year + "12) ";
		String filePrefix=C_ROOT_DIRECTORY+TRANSACTION_ARFF_PREFIX;
		Instances originData=FileUtility.loadDataFromFile(filePrefix+"-origin.arff");
		originData=FilterData.getInstancesSubset(originData, splitSampleClause);
		
		int originDataSize=originData.numInstances();
		System.out.println("loaded original file into memory, number= "+originDataSize);
		Instances refreshedData=FileUtility.loadDataFromFile(filePrefix+".arff");
		refreshedData=FilterData.getInstancesSubset(refreshedData, splitSampleClause);
		
		int refreshedDataSize=refreshedData.numInstances();
		System.out.println("loaded refreshed file into memory, number= "+refreshedDataSize);
		System.out.println("compare originData and newData header,result is :"+originData.equalHeadersMsg(refreshedData));
		
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=FilterData.findATTPosition(originData, ArffFormat.TRADE_DATE);
		originData.sort(tradeDateIndex-1);

		System.out.println("data sorted on tradeDate");
		
		int codeIndex=FilterData.findATTPosition(originData, "code");
		int maIndex=FilterData.findATTPosition(originData, ArffFormat.SELECTED_MA);
		
		Instances originDailyData=null;
		Instances refreshedDailyData=null;
		Instance originRow=null;
		Instance refreshedRow=null;
		String tradeDate=null;
		String code=null;
		int cursor=0;
		int rowCompare=0;
		int rowDiffer=0;
		int rowAdded=0;
		String lastDate=null;
		String lastCode=null;
		
		
		while (cursor<refreshedDataSize){


			//从刷新数据全集中取出某天某只股票的数据，然后进行比对
			tradeDate=refreshedData.instance(cursor).stringValue(tradeDateIndex-1);
			code=refreshedData.instance(cursor).stringValue(codeIndex-1);
			if (tradeDate.equals(lastDate) && code.equals(lastCode)){
				cursor+=checkSample;
				continue;
			}else{
				lastDate=tradeDate;
				lastCode=code;
			}

			originDailyData=FilterData.getInstancesSubset(originData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
			refreshedDailyData=FilterData.getInstancesSubset(refreshedData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
			
			int refreshedDailyDataSize=refreshedDailyData.numInstances();
			int originDailyDataSize=originDailyData.numInstances();
			
			//如果新旧数据同时存在，记录条数应该一致 （也就是说新数据要么是完全新增的日期，要么需要整个替换之前的旧数据）
			if (refreshedDailyDataSize==originDailyDataSize || originDailyDataSize==0){
				//System.out.println("ready to compare data on date "+ tradeDate+" for code:"+code+" origin/refreshed data number= "+originDailyDataSize);
				//将同一天的数据按均线排序
				originDailyData.sort(maIndex-1);
				refreshedDailyData.sort(maIndex-1);
				//按天、股票代码、均线对比
				for(int i=0;i<refreshedDailyDataSize;i++){
					if (originDailyDataSize>0){ //新旧数据同时存在，比较新旧数据
						originRow=originDailyData.instance(i);
						refreshedRow=refreshedDailyData.instance(i);
						if (compareRefreshedRow(maIndex, originRow,refreshedRow, tradeDate, code)==false){
							rowDiffer++;
						}
						rowCompare++;
						if ((rowCompare % 1000)==0) {
							System.out.println("number of instances compared : "+rowCompare);
						}
					}else{ // 只有新数据，把新数据输出
						rowAdded++;
						if ((rowAdded % 1000)==0){
							System.out.println("number of new rows added="+ rowAdded+ "current is @"+tradeDate+"@"+code);
						}
					}
				}// end for i
				//进到下一个日期
				cursor+=refreshedDailyDataSize+checkSample-1;
			}else{
				System.out.println("daily data size in origin and refreshed data are not same on date "+tradeDate+" for code:"+code+" origin/refreshed data number= "+originDailyDataSize+" vs. "+refreshedDailyDataSize);
				rowDiffer+=originDailyDataSize-refreshedDailyDataSize;
				cursor+=checkSample;
			}			
		}// end while
		System.out.println("mission completed, rowSame="+(rowCompare-rowDiffer) +"row differ="+rowDiffer+"row Added="+rowAdded);
		System.out.println("number of original data="+originDataSize+ " vs. rowCompared"+(rowCompare));
		System.out.println("number of refreshed data="+refreshedDataSize+ " vs. rowCompared+rowAdded"+(rowCompare+rowAdded));
	}


	/**
	 * @param maIndex
	 * @param originRow
	 * @param refreshedRow
	 * @param tradeDate
	 * @param code
	 * @param rowDiffer
	 * @return
	 * @throws IllegalStateException
	 */
	private static boolean compareRefreshedRow(int maIndex, Instance originRow,
			Instance refreshedRow, String tradeDate, String code)
			throws IllegalStateException {
		boolean rowSame=true;
		double originMa;
		double refreshedMa;
		originMa=originRow.value(maIndex-1);
		refreshedMa=refreshedRow.value(maIndex-1);
		if (originMa!=refreshedMa){
			System.out.println("--------------------------probabily original data error---------------------------");
			System.out.println("daily origin and refreshed MA are not same on date "+tradeDate+" for code:"+code+" origin/refreshed MA= "+originMa+" vs. "+refreshedMa);
			System.out.println(originRow.toString());
			System.out.println(refreshedRow.toString());
			rowSame=false;
		}else{
			for (int n = 10; n < originRow.numAttributes() ; n++) { //跳过左边的值 

				Attribute originAtt = originRow.attribute(n);
				Attribute refresedAtt=refreshedRow.attribute(n);
				if (originAtt.isNominal() || originAtt.isString()) {
					String originValue=originRow.stringValue(n);
					String refreshedValue=refreshedRow.stringValue(n);
					if (originValue.equals(refreshedValue)==false){
						String originAttName=originAtt.name();
						String refreshedAttName=refresedAtt.name();
						System.out.println("@"+tradeDate+"@"+code+" Attribute value is not the same. value= "+ originValue+" vs."+refreshedValue+" @ "+originAttName + " & "+ refreshedAttName);;
						rowSame=false;
//										System.out.println(originRow.toString());
//										System.out.println(refreshedRow.toString());
//										break;
					}
				} else if (originAtt.isNumeric()) {
					double originValue=originRow.value(n);
					double refreshedValue=refreshedRow.value(n);
					double difference=FormatUtility.compareDouble(originValue,refreshedValue);

					if ( difference!=0 ){
						String originAttName=originAtt.name();
						String refreshedAttName=refresedAtt.name();
						System.out.println("@"+tradeDate+"@"+code+" Attribute value is not the same. value= "+ originValue+" vs."+refreshedValue+" @ "+originAttName + " & "+ refreshedAttName+ " difference= "+difference);;
						rowSame=false;
//										//临时屏蔽换手率 收益率的详细错误输出
//										if (("换手率".equals(originAttName) || "shouyilv".equals(originAttName))==false){
//											System.out.println(originRow.toString());
//											System.out.println(refreshedRow.toString());										
//											break;
//										}
					}
				} else {
					throw new IllegalStateException("Unhandled attribute type!");
				}
			}//end for n;
		}//end else
		return rowSame;
	}
	
	//这里是用最近一年的数据刷新最原始的文件，调整完再用processHistoryData生成有计算字段之后的数据
	protected static void refreshArffFile(int startYear,int endYear) throws Exception {
		System.out.println("loading original history file into memory "  );
		String originFileName=C_ROOT_DIRECTORY+TRANSACTION_ARFF_PREFIX;
		Instances fullData = FileUtility.loadDataFromFile(originFileName+"-origin.arff");
		
		//做这个处理是因为不知为何有时id之前会出现全角空格
		if (ArffFormat.ID.equals(fullData.attribute(ArffFormat.ID_POSITION-1).name())==false){
			fullData.renameAttribute(ArffFormat.ID_POSITION-1, ArffFormat.ID);
		}
		//将股票代码，交易日期之类的字段变换为String格式
		fullData=FilterData.NominalToString(fullData, "3-6,8");


		System.out.println("finish  loading original File row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		

		for (int i=startYear;i<=endYear;i++){
			fullData = refreshArffForOneYear(i,C_ROOT_DIRECTORY+"sourceData\\onceyield"+i+".txt",fullData);
		}

		//保险起见把新数据按日期重新排序，虽然这样比较花时间，但可以确保日后处理时按tradeDate升序。
		fullData.sort(2);
		System.out.println("refreshed arff file sorted, start to save.... number of rows="+fullData.numInstances());
		FileUtility.SaveDataIntoFile(fullData, originFileName+".arff");
		System.out.println("refreshed arff file saved. ");

		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + (endYear-1) + "06) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ endYear + "12) ";
		Instances sampleData=FilterData.getInstancesSubset(fullData, splitSampleClause);
		FileUtility.SaveDataIntoFile(sampleData, originFileName+"-sample.arff");
	}


	/**
	 * @param year
	 * @param newDataFile
	 * @param originData
	 * @return
	 * @throws Exception
	 * @throws ParseException
	 * @throws IllegalStateException
	 */
	private static Instances refreshArffForOneYear(int year,
			String newDataFile, Instances fullData)
			throws Exception, ParseException, IllegalStateException {
		int originInstancesNum=fullData.numInstances();
		System.out.println ("refreshing year: "+ year + "while fullsize ="+ originInstancesNum);
		
		//将原始文件里的原有的该年数据删除
		String splitCurrentYearClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " < " + year + "01) or ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " > "	+ year + "12) ";
		fullData=FilterData.getInstancesSubset(fullData, splitCurrentYearClause);
		
		int filteredNumber=fullData.numInstances() ;
		System.out.println("number of rows removed = "+ (originInstancesNum-filteredNumber));

		
		Instances newData = FileUtility.loadDataFromIncrementalCSVFile(newDataFile);
		
				
		
		
		//将单次收益率增量数据修正成为原始文件数据格式（yearmonth重新计算，插入一列空的股票代码和year（这两列暂时用不上），改名“均线策略”）
		int yearMonthIndex=FilterData.findATTPosition(newData, ArffFormat.ID); //在ID之后插入
		int stockNameIndex=FilterData.findATTPosition(newData, ArffFormat.SELL_DATE); //在MC_DATA之后插入
		newData.insertAttributeAt(new Attribute("yearmonth"), yearMonthIndex);
		List<String> attLabels=null;
		newData.insertAttributeAt(new Attribute("股票名称",attLabels),stockNameIndex+1);//加1的原因是上面已经插入了yearmonth
		newData.insertAttributeAt(new Attribute("year"),stockNameIndex+2);//加2的原因是上面已经插入了两个属性 
	     
	    
	    
	    System.out.println("!!!!!verifying new data format , you should read this .... "+ fullData.equalHeadersMsg(newData));
	    //重新计算yearmonth
	    Attribute tradeDateAtt=newData.attribute(ArffFormat.TRADE_DATE);
	    Attribute mcDateAtt=newData.attribute(ArffFormat.SELL_DATE);
	    Attribute dataDateAtt=newData.attribute(ArffFormat.DATA_DATE);
	    Attribute yearMonthAtt=newData.attribute("yearmonth");
	    Instance curr;
	    String tradeDate;
	    double ym;
	    for (int i=0;i<newData.numInstances();i++){
	    	curr=newData.instance(i);
	    	
	    	tradeDate=curr.stringValue(tradeDateAtt);
	    	//设置yearmonth
	    	ym=FormatUtility.parseYearMonth(tradeDate);
		    curr.setValue(yearMonthAtt, ym);
		    //修改日期格式
		    curr.setValue(tradeDateAtt, FormatUtility.convertDate(tradeDate));
		    curr.setValue(mcDateAtt, FormatUtility.convertDate(curr.stringValue(mcDateAtt)));
		    curr.setValue(dataDateAtt, FormatUtility.convertDate(curr.stringValue(dataDateAtt)));
		    
	    }
		
		System.out.println("number of new rows added or updated= "+ newData.numInstances());

		calibrateAttributes(newData,fullData);

		System.out.println("number of refreshed dataset = "+fullData.numInstances());
		return fullData;
	}



	//这是处理历史全量数据，重新切割生成各种长、短以及格式文件的方法
	protected static void processHistoryFile() throws Exception {
		System.out.println("loading history file into memory "  );
		String originFileName=C_ROOT_DIRECTORY+TRANSACTION_ARFF_PREFIX;
		Instances fullSetData = FileUtility.loadDataFromFile(originFileName+".arff");
		System.out.println("finish  loading fullset File  row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		// 去除与训练无关的字段
		Instances result=ArffFormat.processAllTransaction(fullSetData);
//		//保存训练用的format，用于做日后的校验 
		Instances format=new Instances(result,0);
		FileUtility.SaveDataIntoFile(format, originFileName+"-format.arff");	
		//保存短格式
		FileUtility.SaveDataIntoFile(result, originFileName+"-short.arff");
		result=ArffFormat.addCalculateAttribute(result);
		FileUtility.SaveDataIntoFile(result, originFileName+"-new.arff");
		System.out.println("full Set Data File saved "  );
		
		// 存下用于计算收益率的数据
		Instances left=ArffFormat.getTransLeftPartFromAllTransaction(fullSetData);
		FileUtility.SaveDataIntoFile(left, C_ROOT_DIRECTORY+TRANSACTION_ARFF_PREFIX+"-left.arff");
		System.out.println("history Data left File saved: "+TRANSACTION_ARFF_PREFIX+"-left.arff"  );
	}

	protected static void addCalculationsToFile(String path, String arffName) throws Exception{
		System.out.println("start to load File for data "  );
		Instances fullSetData = FileUtility.loadDataFromFile(path+arffName + ".arff");
		System.out.println("finish  loading fullset File  row : "
				+ fullSetData.numInstances() + " column:"
				+ fullSetData.numAttributes());
		Instances result=ArffFormat.addCalculateAttribute(fullSetData);
		FileUtility.SaveDataIntoFile(result, path+arffName+"-new.arff");
		System.out.println("file saved "  );
	}


	//使用文件预测每天的增量数据
	protected static void predictWithFile(MyClassifier clModel, String pathName,
			String dataFileName) throws Exception {
		System.out.println("-----------------------------");
		Instances fullData = FileUtility.loadDailyNewDataFromCSVFile(pathName + dataFileName
				+ ".txt");
	
		predict(clModel, pathName, fullData);

		System.out.println("file saved and mission completed.");
	}
	
	//直接访问数据库预测每天的增量数据
	protected static void predictWithDB(MyClassifier clModel, String pathName) throws Exception {
		System.out.println("-----------------------------");
		Instances fullData = DBAccess.LoadDataFromDB(clModel.arff_format);

		
		predict(clModel, pathName, fullData);
		System.out.println("Database updated and mission completed.");
	}
	
	//用模型预测数据
	private static Instances predict(MyClassifier clModel, String pathName, Instances inData) throws Exception {
		Instances newData = null;
		Instances result = null;

		Instances fullData=calibrateAttributesForDailyData(pathName, inData,clModel.arff_format);
	
		//如果模型需要计算字段，则把计算字段加上
		if (clModel.inputAttShouldBeIndependent==false){
			fullData=ArffFormat.addCalculateAttribute(fullData);		
		}
		
		FileUtility.SaveDataIntoFile(fullData, pathName+FormatUtility.getDateStringFor(1)+"dailyInput-new116.arff");
		
		//获得”均线策略"的位置属性
		int maIndex=FilterData.findATTPosition(fullData,ArffFormat.SELECTED_MA);

		if (clModel instanceof NominalClassifier ){
			fullData=((NominalClassifier)clModel).processDataForNominalClassifier(fullData,false);
			//TODO 因为历史原因，201605018之前build的二分类器模型 “均线策略" 都叫"policy"，所以调用模型前先改一下名，调用模型后再改回来
			fullData.renameAttribute(maIndex-1, "policy");
		}

		
		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {

			System.out.println("start to load data for " + ArffFormat.SELECTED_MA+"  : "	+ clModel.m_policySubGroup[j]);
			String expression=null;
			if("3060".equals(clModel.m_policySubGroup[j])==false){
				expression=FilterData.WEKA_ATT_PREFIX+ maIndex+" is '"+ clModel.m_policySubGroup[j] + "'";
			}else{ //尝试把30日线与60日线合并处理，因为这两条线的数据太少，模型常常不理想
				String expression30="("+FilterData.WEKA_ATT_PREFIX+ maIndex+" is '30') or (";
				String expression60=FilterData.WEKA_ATT_PREFIX+ maIndex+" is '60') ";
				expression=expression30+expression60;
			}
			
			newData = FilterData.getInstancesSubset(fullData, expression);

			
			String modelFileName;
			String evalFileName;
			if (clModel instanceof MLPClassifier ){

				modelFileName = pathName+"\\"+clModel.classifierName+ MLP_PREDICT_MODEL
						+ clModel.m_policySubGroup[j]	;				
				evalFileName = pathName+"\\"+clModel.classifierName+MLP_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof M5PClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+M5P_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+M5P_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof M5PABClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+M5PAB_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+M5PAB_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof MLPABClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+MLPAB_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+MLPAB_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}else {
				throw new Exception("undefined predict model");
			}

			clModel.setModelFileName(modelFileName);
			clModel.setEvaluationFilename(evalFileName);
	
			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());
			if (result == null) {// initialize result instances
				// remove unnecessary data,leave 均线策略 & shouyilv alone
				Instances header = new Instances(newData, 0);
				result = FilterData.removeAttribs(header,
						"4-" + String.valueOf(header.numAttributes() - 1)); 
				if (clModel instanceof NominalClassifier ){
					result = FilterData.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
							result.numAttributes());
				}else{
					result = FilterData.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
						result.numAttributes());
				}
				result = FilterData.AddAttribute(result, ArffFormat.RESULT_SELECTED,
						result.numAttributes());

			}
 
			clModel.predictData(newData, result);
			System.out.println("accumulated predicted rows: "+ result.numInstances());
			System.out.println("complete for 均线策略: " + clModel.m_policySubGroup[j]);
		}
		if (result.numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.numInstances());
		}
		
		result.renameAttribute(1, ArffFormat.SELECTED_MA_IN_OTHER_SYSTEM); //输出文件的“均线策略”名字不一样
		FileUtility.saveCSVFile(result, pathName + clModel.classifierName+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");		
		clModel.outputClassifySummary();
		return result;
	}





	//历史回测
	protected static Instances testBackward(MyClassifier clModel) throws Exception,
			IOException {
		Instances fullSetData = null;
		Instances result = null;
		StringBuffer evalResultSummary=new StringBuffer();
		evalResultSummary.append("时间段,均线策略,整体正收益股数,整体股数,整体TPR,所选正收益股数,所选总股数,所选股TPR,提升率,所选股平均收益率,整体平均收益率,收益率差,是否改善,阀值下限,阀值上限\r\n");
		System.out.println("test backward using classifier : "+clModel.classifierName);
		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) { // if i starts from 1, the
														// first one is to use
														// as validateData
			
			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   "+splitMark);
			// 加载原始arff文件
			if (fullSetData == null) {

				// 根据模型来决定是否要使用有计算字段的ARFF
				String arffFile=null;
				if (clModel.inputAttShouldBeIndependent==true){
					arffFile=ArffFormat.SHORT_ARFF_FILE;
				}else{
					arffFile=ArffFormat.LONG_ARFF_FILE;
				}

				System.out.println("start to load File for fullset from File: "+ arffFile  );
				fullSetData = FileUtility.loadDataFromFile( C_ROOT_DIRECTORY+arffFile);
				System.out.println("finish loading fullset Data. row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());

				if (clModel instanceof NominalClassifier ){
					//TODO 因为历史原因，201605018之前build的二分类器模型 “均线策略" 都叫"policy"，所以调用模型前先改一下名，调用模型后再改回来
					//获得”均线策略"的位置属性
					int maIndex=FilterData.findATTPosition(fullSetData,ArffFormat.SELECTED_MA);
					fullSetData.renameAttribute(maIndex-1, "policy");
				}
			}
			// 准备输出数据格式
			if (result == null) {// initialize result instances
				Instances header = new Instances(fullSetData, 0);
				// 去除不必要的字段，保留ID（第1），均线策略（第3）、bias5（第4）、收益率（最后一列）、增加预测值、是否被选择。
				result = FilterData.removeAttribs(header, ArffFormat.YEAR_MONTH_INDEX + ",5-"
						+ (header.numAttributes() - 1));
				if (clModel instanceof NominalClassifier ){
					result = FilterData.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
							result.numAttributes());
				}else{
					result = FilterData.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
						result.numAttributes());
				}
				result = FilterData.AddAttribute(result, ArffFormat.RESULT_SELECTED,
						result.numAttributes());

			}
	
			String splitTrainYearClause = "";
			String splitTestYearClause = "";

			String attribuateYear = "ATT" + ArffFormat.YEAR_MONTH_INDEX;
			if (splitMark.length() == 6) { // 按月分割时
				splitTrainYearClause = "(" + attribuateYear + " < "
						+ splitYear[i] + ") ";
				splitTestYearClause = "(" + attribuateYear + " = "
						+ splitYear[i] + ") ";
			} else if (splitMark.length() == 4) {// 按年分割
				splitTrainYearClause = "(" + attribuateYear + " < "
						+ splitYear[i] + "01) ";
				splitTestYearClause = "(" + attribuateYear + " >= "
						+ splitYear[i] + "01) and (" + attribuateYear + " <= "
						+ splitYear[i] + "12) ";
			}

			String policy = null;
			double lower_limit = 0;
			double upper_limit = 0;
			double tp_fp_ratio=0;
			String splitTrainClause = "";
			String splitTestClause = "";

			if (clModel.NO_SUB_GROUP == true) {
				policy = "ALL";
				lower_limit = clModel.SAMPLE_LOWER_LIMIT[0];
				upper_limit = clModel.SAMPLE_UPPER_LIMIT[0];
				tp_fp_ratio= clModel.TP_FP_RATIO_LIMIT[0];
				splitTrainClause = splitTrainYearClause;
				splitTestClause = splitTestYearClause;
				String resultSummary = doOneModel(clModel, fullSetData, result, splitMark,
						policy, lower_limit, upper_limit,tp_fp_ratio, splitTrainClause,
						splitTestClause);
				evalResultSummary.append(resultSummary);
			} else {
				for (int j = 0; j < clModel.m_policySubGroup.length; j++) {
					policy = clModel.m_policySubGroup[j];
					lower_limit = clModel.SAMPLE_LOWER_LIMIT[j];
					upper_limit = clModel.SAMPLE_UPPER_LIMIT[j];
					tp_fp_ratio= clModel.TP_FP_RATIO_LIMIT[j];
					if("3060".equals(policy)==false){
						splitTrainClause = splitTrainYearClause + " and (ATT3 is '"	+ policy + "')";
						splitTestClause = splitTestYearClause + " and (ATT3 is '"+ policy + "')";
					}else{ //把30日线与60日线合并处理，因为这两条线的数据太少，模型常常不理想
						splitTrainClause = splitTrainYearClause + " and ((ATT3 is '30') or (ATT3 is '60'))";								
						splitTestClause = splitTestYearClause + " and ((ATT3 is '30') or (ATT3 is '60'))";
					}
					String resultSummary = doOneModel(clModel, fullSetData, result,
							splitMark, policy, lower_limit, upper_limit,tp_fp_ratio,
							splitTrainClause, splitTestClause);
					evalResultSummary.append(resultSummary);
				}
			}
			
			System.out.println("********************complete **************************** " + splitMark);
			System.out.println(" ");
		}
		

		FileUtility.write(BACKTEST_RESULT_DIR+clModel.classifierName+"-monthlySummary.csv", evalResultSummary.toString(), "GBK");
		if (clModel instanceof NominalClassifier ){
			//TODO 因为历史原因，201605018之前build的二分类器模型 “均线策略" 都叫"policy"，所以调用模型前先改一下名，调用模型后再改回来
			int maIndex=FilterData.findATTPosition(result,"policy");
			result.renameAttribute(maIndex-1, ArffFormat.SELECTED_MA);
		}		
		saveBacktestResultFile(result,clModel.classifierName);
		
		System.out.println(clModel.classifierName+" test result file saved.");
		return result;
	}

	// paremeter result will be changed in the method! 
	protected static String doOneModel(MyClassifier clModel,
			Instances fullSetData, Instances result, String yearSplit,
			String policySplit, double lower_limit, double upper_limit, double tp_fp_ratio,
			String splitTrainClause, String splitTestClause) throws Exception,
			IOException {
		Instances trainingData = null;
		Instances testingData = null;
		
		System.out.println("-----------------start for " + yearSplit + "-----------均线策略: ------" + policySplit);
		clModel.generateModelAndEvalFileName(yearSplit,policySplit);

		Classifier model = null;
		if (clModel.m_skipTrainInBacktest == false || clModel.m_skipEvalInBacktest==false ) { //如果不需要培训和评估，则无需训练样本
			System.out.println("start to split training set");
			trainingData = FilterData.getInstancesSubset(fullSetData,
					splitTrainClause);
			trainingData = FilterData.removeAttribs(trainingData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);
			
			//对于二分类器，这里要把输入的收益率转换为分类变量
			if (clModel instanceof NominalClassifier ){
				trainingData=((NominalClassifier)clModel).processDataForNominalClassifier(trainingData,false);
			}
			System.out.println(" training data size , row : "
					+ trainingData.numInstances() + " column: "
					+ trainingData.numAttributes());
			if (clModel.m_saveArffInBacktest) {
				clModel.saveArffFile(trainingData,"train", yearSplit, policySplit);
			}
		}
		
		//是否需要重做训练阶段
		if (clModel.m_skipTrainInBacktest == false) { 
			System.out.println("start to build model");
			model = clModel.trainData(trainingData);
		} else {
			model = clModel.loadModel(yearSplit,policySplit);
		}
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			clModel.evaluateModel(trainingData, model, lower_limit,
					upper_limit,tp_fp_ratio);
		}
		trainingData=null;

		// apply model part
		System.out.println("start to split testing set");
		testingData = FilterData
				.getInstancesSubset(fullSetData, splitTestClause);
		//对于二分类器，这里要把输入的收益率转换为分类变量
		if (clModel instanceof NominalClassifier ){
			testingData=((NominalClassifier)clModel).processDataForNominalClassifier(testingData,true);
		}
		testingData = FilterData.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);
		System.out.println("testing data size, row: "
				+ testingData.numInstances() + " column: "
				+ testingData.numAttributes());

		if (clModel.m_saveArffInBacktest) {
			clModel.saveArffFile(testingData,"test", yearSplit, policySplit);
		}

		String evalSummary=yearSplit+","+policySplit+",";
		evalSummary+=clModel.predictData(testingData, result);
		testingData=null;
		System.out.println("accumulated predicted rows: "
				+ result.numInstances());
		System.out.println("complete for " + yearSplit + "均线策略: " + policySplit);
		return evalSummary;
	}

	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private static Instances calibrateAttributesForDailyData(String pathName,Instances incomingData,int formatType) throws Exception {
		
		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		String formatFile=null;
		switch (formatType) {
		case ArffFormat.LEGACY_FORMAT:
			formatFile="AllTransaction20052016-format.arff";
			break;
		case ArffFormat.EXT_FORMAT:
			formatFile=TRANSACTION_ARFF_PREFIX+"-format.arff";
			break;
		default:
			break;
		}

		Instances format=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+formatFile);
		format=FilterData.removeAttribs(format, "2");
		System.out.println("!!!!!verifying input data format , you should read this .... "+ format.equalHeadersMsg(incomingData));
		calibrateAttributes(incomingData, format);
		return format;
	}

	/**
	 * read all data from input , add them to the output instances using output format
	 * output instances will be changed in this method!
	 */
	private static void calibrateAttributes(Instances input,
			Instances output) throws Exception, IllegalStateException {
		for (int m=0; m<input.numInstances();m++){
			Instance inst=new DenseInstance(output.numAttributes());
			inst.setDataset(output);
			for (int n = 0; n < input.numAttributes() ; n++) { 
				Attribute incomingAtt = input.attribute(n);
				Attribute formatAtt=output.attribute(n);
				
				String formatAttName=formatAtt.name();
				String incomingAttName=incomingAtt.name();
				Instance curr=input.instance(m);
				if (formatAttName.equals(incomingAttName)){
					if (formatAtt.isNominal()) {
						String label = curr.stringValue(incomingAtt);
						if ("?".equals(label)){
							System.out.println("Attribute value is empty. value= "+ label+" @ "+ incomingAttName );
						}else {
							int index = formatAtt.indexOfValue(label);
							if (index != -1) {
								inst.setValue(n, index);
							}else{
								throw new Exception("Attribute value is invalid. value= "+ label+" @ "+ incomingAttName + " & "+ formatAttName);
							}
						}
					} else if (formatAtt.isString()) {
						String label = curr.stringValue(incomingAtt);
						inst.setValue(n, label);
					} else if (formatAtt.isNumeric()) {
						inst.setValue(n, input.instance(m).value(incomingAtt));
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
				}else {
					throw new Exception("Attribute order error! "+ incomingAttName + " vs. "+ formatAttName);
				}
			}
			output.add(inst);
		}
	}

	
	
	protected static Instances mergeResultWithData(Instances resultData,Instances referenceData,String dataToAdd,int format) throws Exception{
		//读取磁盘上预先保存的左侧数据
		Instances left=null;
		
		//TODO 过渡期 有少量模型尚使用原有格式
		if (format==ArffFormat.EXT_FORMAT){
			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+TRANSACTION_ARFF_PREFIX+"-left.arff");
		}else if (format==ArffFormat.LEGACY_FORMAT){ //LEGACY 有少量模型尚使用原有格式
			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+"AllTransaction20052016-left.arff");
		}
		System.out.println("incoming resultData size, row="+resultData.numInstances()+" column="+resultData.numAttributes());
		System.out.println("incoming referenceData size, row="+referenceData.numInstances()+" column="+referenceData.numAttributes());
		System.out.println("Left data loaded, row="+left.numInstances()+" column="+left.numAttributes());


	    // 创建输出结果
	    Instances mergedResult = new Instances(left, 0);
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_PROFIT, mergedResult.numAttributes());
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_WIN_RATE, mergedResult.numAttributes());
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_SELECTED, mergedResult.numAttributes());

		
		Instance leftCurr;
		Instance resultCurr;
		Instance referenceCurr;
		Instance newData;
		Attribute leftMA=left.attribute(ArffFormat.SELECTED_MA);
		Attribute resultMA=resultData.attribute(ArffFormat.SELECTED_MA);
		Attribute leftBias5=left.attribute("bias5");
		Attribute resultBias5=resultData.attribute("bias5");
		Attribute resultSelectedAtt=resultData.attribute(ArffFormat.RESULT_SELECTED);
		Attribute outputSelectedAtt=mergedResult.attribute(ArffFormat.RESULT_SELECTED);
		Attribute outputPredictAtt=mergedResult.attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
		Attribute outputWinrateAtt=mergedResult.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE);
				
		
		
		//传入的结果集result不是排序的,而left的数据是按tradeDate日期排序的， 所以都先按ID排序。
		left.sort(ArffFormat.ID_POSITION-1);
		resultData.sort(ArffFormat.ID_POSITION-1);
		referenceData.sort(ArffFormat.ID_POSITION-1);
		
		
		double idInResults=0;
		double idInLeft=0;
		double idInReference=0;
		int resultIndex=0;
		int leftIndex=0;
		int referenceIndex=0;
		int referenceDataNum=referenceData.numInstances();


		while (leftIndex<left.numInstances() && resultIndex<resultData.numInstances()){				
			resultCurr=resultData.instance(resultIndex);
			leftCurr=left.instance(leftIndex);
			idInResults=resultCurr.value(0);
			idInLeft=leftCurr.value(0);
			if (idInLeft<idInResults){ // 如果左边有未匹配的数据，这是正常的，因为left数据是从2005年开始的全量
				leftIndex++;
				continue;
			}else if (idInLeft>idInResults){ // 如果右边result有未匹配的数据，这个不大正常，需要输出
				System.out.println("!!!unmatched result===="+ resultCurr.toString());	
				System.out.println("!!!current left   ====="+ leftCurr.toString());
				resultIndex++;
				continue;
			}else if (idInLeft==idInResults ){//找到相同ID的记录了
				//去reference数据里查找相应的ID记录
				referenceCurr=referenceData.instance(referenceIndex);
				idInReference=referenceCurr.value(0);

				//这段代码是用于应对reference的数据与result的数据不一致情形的。
				//reference数据也是按ID排序的，所以可以按序查找
				int oldIndex=referenceIndex;//暂存一下
				while (idInReference<idInResults ){ 
					if (referenceIndex<referenceDataNum-1){
						referenceIndex++;
						referenceCurr=referenceData.instance(referenceIndex);
						idInReference=referenceCurr.value(0);
					}else { //当前ID比result的ID小，需要向后找，但向后找到最后一条也没找到
						referenceCurr=new DenseInstance(referenceData.numAttributes());
						referenceIndex=oldIndex; //这一条设为空，index恢复原状
						break;
					}
				}
				while (idInReference>idInResults ){
					if (referenceIndex>0){
						referenceIndex--;
						referenceCurr=referenceData.instance(referenceIndex);
						idInReference=referenceCurr.value(0);
					}else {  //当前ID比result的ID大，需要向前找，但向前找到第一条也没找到
						referenceCurr=new DenseInstance(referenceData.numAttributes());
						referenceIndex=oldIndex; //这一条设为空，index恢复原状
						break;
					}
				}
				
				
				//接下来做冗余字段的数据校验
				if ( checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA,leftBias5, resultBias5)) {
					newData=new DenseInstance(mergedResult.numAttributes());
					newData.setDataset(mergedResult);
					int srcStartIndex=0;
					int srcEndIndex=leftCurr.numAttributes()-1;
					int targetStartIndex=0;
					copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);

					//根据传入的参数判断需要当前有什么，需要补充的数据是什么
					double profit;
					double winrate;
					if (dataToAdd.equals(ArffFormat.RESULT_PREDICTED_WIN_RATE)){
						//当前结果集里有什么数据
						profit=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
						//需要添加参考集里的什么数据
						winrate=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
					}else{
						//当前结果集里有什么数据
						winrate=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
						//需要添加参考集里的什么数据
						profit=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
					}

					newData.setValue(outputPredictAtt, profit);
					newData.setValue(outputWinrateAtt, winrate);
					newData.setValue(outputSelectedAtt, resultCurr.value(resultSelectedAtt));
					mergedResult.add(newData);
					resultIndex++;
					leftIndex++;
					if (mergedResult.numInstances() % 100000 ==0){
						System.out.println("number of results processed:"+ mergedResult.numInstances());
					}
				}else {
					throw new Exception("data value in header data and result data does not equal "+leftCurr.value(leftMA)+" = "+resultCurr.value(resultMA)+ " / "+leftCurr.value(leftBias5) + " = "+resultCurr.value(resultBias5));
				}
			}
		}// end left processed
		if (mergedResult.numInstances()!=resultData.numInstances()){
//			throw new Exception
			System.out.println("------Attention!!! not all data in result have been processed , processed= "+mergedResult.numInstances()+" ,while total result="+resultData.numInstances());
		}else {
			System.out.println("number of results merged and processed: "+ mergedResult.numInstances());
		}
		
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=FilterData.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		
		//输出前改名
		mergedResult.renameAttribute(mergedResult.attribute(ArffFormat.SELECTED_MA), ArffFormat.SELECTED_MA_IN_OTHER_SYSTEM);
		//给mergedResult瘦身。
		mergedResult=FilterData.removeAttribs(mergedResult, "2,6-9,11");


		return mergedResult;
	}


	/**
	 * @param leftCurr
	 * @param newData
	 * @param startIndex
	 * @param endIndex
	 * @throws IllegalStateException
	 */
	private static void copyToNewInstance(Instance leftCurr, Instance newData,
			int srcStartIndex, int srcEndIndex,int targetStartIndex) throws IllegalStateException {
		for (int n = srcStartIndex; n <= srcEndIndex; n++) { 
			Attribute att = leftCurr.attribute(n);
			if (att != null) {
				if (att.isNominal()) {
					String label = leftCurr.stringValue(att);
					int index = att.indexOfValue(label);
					if (index != -1) {
						newData.setValue(targetStartIndex+n-srcStartIndex, index);
					} //这里如果left里面的数据没有值，也就不必设值了
				} else if (att.isNumeric()) {
					newData.setValue(targetStartIndex+n-srcStartIndex, leftCurr.value(att));
				} else if (att.isString()) {
					String label = leftCurr.stringValue(att);
					newData.setValue(targetStartIndex+n-srcStartIndex, label);
				} else {
					throw new IllegalStateException("Unhandled attribute type!");
				}
			}
		}
	}

/**
  比较两个instances中的均线策略和bias5字段是否一致（数据冗余校验）
 * @return
 */
private static boolean checkSumBeforeMerge(Instance leftCurr,
		Instance rightCurr, Attribute leftMA, Attribute rightMA,
		Attribute leftBias5, Attribute rightBias5) {
	boolean result=false;
	String leftMAValue=leftCurr.stringValue(leftMA);  //这里不应该为null，否则就抛出异常算了
	String rightMAValue=rightCurr.stringValue(rightMA);//这里不应该为null，否则就抛出异常算了
	double leftBias5Value=leftCurr.value(leftBias5); //这里有可能为NaN
	double rightBias5Value=rightCurr.value(rightBias5);
	
	if (leftMAValue.equals(rightMAValue)){
		if ( (leftBias5Value==rightBias5Value) || Double.isNaN(leftBias5Value) && Double.isNaN(rightBias5Value))
			result=true;
	}
	return result;
}	

private static void saveBacktestResultFile(Instances result,String classiferName) throws IOException{
	FileUtility.SaveDataIntoFile(result, BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
}
protected static Instances loadBackTestResultFromFile(String classiferName) throws Exception{
	Instances result=FileUtility.loadDataFromFile(BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
	return result;
}

protected static void saveSelectedFileForMarkets(Instances fullOutput,String classiferName) throws Exception{
	//输出全市场结果
	Instances fullMarketSelected=FilterData.getInstancesSubset(fullOutput, FilterData.WEKA_ATT_PREFIX +fullOutput.numAttributes()+" = 1");
	FileUtility.saveCSVFile(fullMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-full" + RESULT_EXTENSION );
	//输出沪深300
	Instances subsetMarketSelected=FilterData.filterDataForIndex(fullMarketSelected,ArffFormat.IS_HS300);
	FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-hs300" + RESULT_EXTENSION );
	//输出中证300
	subsetMarketSelected=FilterData.filterDataForIndex(fullMarketSelected,ArffFormat.IS_ZZ500);
	FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-zz500" + RESULT_EXTENSION );
}


//这个函数是将201606之前的数据（只有第一组）合并上新的第二组，第三组数据
protected static void mergeExtData() throws Exception{
	String file1=null;
	String file2=null;
	Instances extData=null;
	
	file1=C_ROOT_DIRECTORY+"sourceData\\单次收益率第二组数据2005_2010.txt";
	file2=C_ROOT_DIRECTORY+"sourceData\\单次收益率第二组数据2011_20160531.txt";
	extData = mergeInstancesFromTwoFiles(file1, file2);
	System.out.println("Group 2 full ext data loaded. number="+extData.numInstances());
	
	//加载原始arff文件
	String originFileName=C_ROOT_DIRECTORY+"AllTransaction20052016";
	Instances fullData = FileUtility.loadDataFromFile(originFileName+".arff");
	System.out.println("full trans data loaded. number="+fullData.numInstances());
	
	//将两边数据以ID排序
	fullData.sort(ArffFormat.ID_POSITION-1);
	extData.sort(ArffFormat.ID_POSITION-1);
	System.out.println("all data sorted by id");
	

	Instances result=mergeTransactionWithExtension(fullData,extData,ArffFormat.INCREMENTAL_EXT_ARFF_RIGHT);
	System.out.println("group 2 ext data processed. number="+result.numInstances()+" columns="+result.numAttributes());
	extData=null;
	fullData=null;
	
	
	//处理第三组数据
	file1=C_ROOT_DIRECTORY+"sourceData\\单次收益率第三组数据2005_2010.txt";
	file2=C_ROOT_DIRECTORY+"sourceData\\单次收益率第三组数据2011_20160531.txt";
	extData = mergeInstancesFromTwoFiles(file1, file2);
	System.out.println("Group 3 full ext data loaded. number="+extData.numInstances());

	extData.sort(ArffFormat.ID_POSITION-1);
	

	result=mergeTransactionWithExtension(result,extData,ArffFormat.INCREMENTAL_EXT_ARFF_RIGHT2);
	System.out.println("group 2 ext data processed. number="+result.numInstances()+" columns="+result.numAttributes());

	//返回结果之前需要按TradeDate重新排序
	int tradeDateIndex=FilterData.findATTPosition(result, ArffFormat.TRADE_DATE);
	result.sort(tradeDateIndex-1);
	
	//保留原始的ext文件
	FileUtility.SaveDataIntoFile(result, originFileName+"-ext.arff");
	System.out.println("history Data File saved: "+originFileName+"-ext.arff");
	
	// 存下用于计算收益率的数据
	Instances left=ArffFormat.getTransLeftPartFromAllTransaction(result);
	FileUtility.SaveDataIntoFile(left, C_ROOT_DIRECTORY+originFileName+"-ext-left.arff");
	System.out.println("history Data left File saved: "+originFileName+"-ext-left.arff");	
	
	// 去除与训练无关的字段
	result=ArffFormat.processAllTransaction(result);
//	//保存训练用的format，用于做日后的校验 
	Instances format=new Instances(result,0);
	FileUtility.SaveDataIntoFile(format, originFileName+"-ext-format.arff");	
	//保存短格式
	FileUtility.SaveDataIntoFile(result, originFileName+"-ext-short.arff");
	//添加计算字段
	result=ArffFormat.addCalculateAttribute(result);
	FileUtility.SaveDataIntoFile(result, originFileName+"-ext-new.arff");
	System.out.println("full ext Data File saved "  );
	

}



/**
 * @param firstFile
 * @param secondFile
 * @return
 * @throws Exception
 * @throws IllegalStateException
 */
private static Instances mergeInstancesFromTwoFiles(String firstFile,
		String secondFile) throws Exception, IllegalStateException {
	Instances extData=FileUtility.loadDataFromExtCSVFile(firstFile);
	Instances extDataSecond=FileUtility.loadDataFromExtCSVFile(secondFile);

	
	//如果不是用这种copy的方式和setDataSet的方式，String和nominal数据会全乱掉。
	Instance oldRow=null;
	int colSize=extData.numAttributes()-1;
	for (int i=0;i<extDataSecond.numInstances();i++){
		Instance newRow=new DenseInstance(extData.numAttributes());
		newRow.setDataset(extData);
		oldRow=extDataSecond.instance(i);
		copyToNewInstance(oldRow,newRow,0,colSize,0);
		extData.add(newRow);
	}

	return extData;
}


//数据必须是以ID排序的。
private static Instances mergeTransactionWithExtension(Instances transData,Instances extData,String[] extDataFormat) throws Exception{



	//找出transData中的所有待校验字段
	Attribute[] attToCompare=new Attribute[ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length];
	for (int i=0;i<ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length;i++){
		attToCompare[i]=transData.attribute(ArffFormat.INCREMENTAL_EXT_ARFF_LEFT[i]);
	}
    Instances mergedResult=prepareMergedFormat(new Instances(transData,0), new Instances(extData,0));
    System.out.println("merged output column number="+mergedResult.numAttributes());

    
	//开始准备合并
	if (transData.numInstances()!=extData.numInstances()){
		System.out.println("=============warning================= transData number ="+transData.numInstances() +" while extData="+extData.numInstances());
	}
	
	int leftProcessed=0;
	int rightProcessed=0;
	Instance leftCurr;
	Instance rightCurr;
	Instance newData;

	while (leftProcessed<transData.numInstances() && rightProcessed<extData.numInstances()){	
		leftCurr=transData.instance(leftProcessed);
		rightCurr=extData.instance(rightProcessed);
		double leftID = leftCurr.value(0);
		double rightID = rightCurr.value(0);

		if (leftID<rightID){ // 如果左边有未匹配的数据
			System.out.println("unmatched left====="+ leftCurr.toString());
			System.out.println("current right ====="+ rightCurr.toString());
			System.out.println("leftProcessed="+leftProcessed+" rightProcessed="+rightProcessed);
			leftProcessed++;
			continue;
		}else if (leftID>rightID){ // 如果右边有未匹配的数据
			System.out.println("unmatched right===="+ rightCurr.toString());	
			System.out.println("current left  ====="+ leftCurr.toString());
			System.out.println("leftProcessed="+leftProcessed+" rightProcessed="+rightProcessed);
			rightProcessed++;
			continue;
		}else if (leftID==rightID ){//找到相同ID的记录了
			//先对所有的冗余数据进行校验
			for (int j=0;j<ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length;j++){
				Attribute att = attToCompare[j];
				if (att.isNominal() || att.isString()) {
					String leftLabel = leftCurr.stringValue(att);
					String rightLabel=rightCurr.stringValue(j);
					if (leftLabel.equals(rightLabel)){
						//do nothing
					}else{
						//可能是因为输入文件的date格式不一样，尝试转换一下
						rightLabel=FormatUtility.convertDate(rightLabel);
						if (leftLabel.equals(rightLabel)==false){
							System.out.println("current left====="+ leftCurr.toString());
							System.out.println("current right===="+ rightCurr.toString());		
							throw new Exception("data not equal! at attribute:"+ArffFormat.INCREMENTAL_EXT_ARFF_LEFT[j]+ " left= "+leftLabel+" while right= "+rightLabel +" @id="+leftID);
						}
					}
				} else if (att.isNumeric()) {
					double leftValue=leftCurr.value(att);
					double rightValue=rightCurr.value(j);
					if ( (leftValue==rightValue) || Double.isNaN(leftValue) && Double.isNaN(rightValue)){
						// do nothing
					}else {
						System.out.println("current left====="+ leftCurr.toString());
						System.out.println("current right===="+ rightCurr.toString());						
						System.out.println("=========data not equal! at attribute:"+ArffFormat.INCREMENTAL_EXT_ARFF_LEFT[j]+ " left= "+leftValue+" while right= "+rightValue);							
					}
				} else {
					throw new IllegalStateException("Unhandled attribute type!");
				}

			}//end for j

			newData=new DenseInstance(mergedResult.numAttributes());
			newData.setDataset(mergedResult);

			//先拷贝transData中除了classvalue以外的数据
			int srcStartIndex=0;
			int srcEndIndex=leftCurr.numAttributes()-2;//注意这里的classvalue先不拷贝 
			int targetStartIndex=0;
			copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);

			//再拷贝extData中除校验数据之外的数据				
			srcStartIndex=ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length;
			srcEndIndex=ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length+extDataFormat.length-1;
			targetStartIndex=leftCurr.numAttributes()-1; //接着拷贝
			copyToNewInstance(rightCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);

			//再设置classValue
			srcStartIndex=leftCurr.numAttributes()-1;
			srcEndIndex=leftCurr.numAttributes()-1;
			targetStartIndex=newData.numAttributes()-1;
			copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
			
			mergedResult.add(newData);
			leftProcessed++;
			rightProcessed++;
			if (leftProcessed % 100000 ==0){
				System.out.println("number of results processed. left="+ leftProcessed+ " right="+rightProcessed);
			}

		}// end if leftCurr

	}//end for
	if (rightProcessed!=extData.numInstances()){
		System.out.println("not all data in extData have been processed , processed= "+rightProcessed+" ,while total="+extData.numInstances());
	}else {
		System.out.println("number of data merged and processed: "+ rightProcessed+" origin data columns="+transData.numAttributes()+" new data columns="+mergedResult.numAttributes());
	}
	

	return mergedResult;
}


/**
 * Merges two sets of Instances together（这里仅生成空格式不实际合并数据）. The resulting set will have all the
 * attributes of the first set plus all the attributes of the second set. 
 * 将第一个Instances的classvalue（最后一列）作为合并后的classvalue
 * 
 */
private static Instances prepareMergedFormat(Instances transData, Instances extData) {
	// Create the vector of merged attributes
    ArrayList<Attribute> newAttributes = new ArrayList<Attribute>(transData.numAttributes() +
    		extData.numAttributes()-ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length);
    Enumeration<Attribute> enu = transData.enumerateAttributes();
    while (enu.hasMoreElements()) {
    	newAttributes.add((Attribute) (enu.nextElement().copy()));// Need to copy because indices will change.
    }
    enu = extData.enumerateAttributes();
    while (enu.hasMoreElements()) {
    	//去掉冗余字段，将有效数据字段加入新数据集
    	// Need to copy because indices will change.
    	Attribute att=(Attribute)enu.nextElement().copy();
    	if (att.index()>= ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length){
    		newAttributes.add(att);
    	}
    }
    //将第一个数据集里的Class属性作为新数据集的class属性
    Attribute classAttribute=(Attribute)transData.classAttribute().copy();// Need to copy because indices will change.
    newAttributes.add(classAttribute);

    // Create the set of mergedInstances
    Instances merged = new Instances(transData.relationName(), newAttributes, 0);
    merged.setClassIndex(merged.numAttributes()-1);
    
    return merged;
}



}