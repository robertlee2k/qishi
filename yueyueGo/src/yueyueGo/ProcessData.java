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
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ProcessData {

	
	public static final String C_ROOT_DIRECTORY = "C:\\Users\\robert\\Desktop\\提升均线策略\\";
	public static final String NOMINAL_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"01-二分类器\\";
	public static final String CONTINOUS_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"02-连续分类器\\";
	public static final String PREDICT_WORK_DIR=C_ROOT_DIRECTORY+"03-预测模型\\";
	private static final String ALL_TRANSACTION_LEFT_ARFF = "AllTransaction20052016-left.arff";
	
	public static final String[] splitYear ={
		"201604","201605"
//		  "200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603","201604","201605"
		};

	public static void main(String[] args) {
		try {

//			//用二分类模型预测每日增量数据
//			MLPClassifier nModel=new MLPClassifier();
//			predictWithDB(nModel,PREDICT_WORK_DIR);
//			//用连续模型预测每日增量数据
//			M5PClassifier cModel=new M5PClassifier();
//			//读取数据库预测
//			predictWithDB(cModel,PREDICT_WORK_DIR);
		
//			//使用文件预测
//			String dataFileName=("zengliang"+FormatUtility.getDateStringFor(1)).trim();
//			predictWithFile(clModel,PREDICT_WORK_DIR,dataFileName);

//			//按二分类器回测历史数据
//			MLPClassifier nModel = new MLPClassifier();
//			Instances mlpResult=testBackward(nModel);
//			
			//按连续分类器回测历史数据
			M5PClassifier cModel=new M5PClassifier();
			Instances m5pResult=testBackward(cModel);
//
//			//输出用于计算收益率的CSV文件
//			Instances m5pOutput=mergeResultWithData(m5pResult,mlpResult,ArffFormat.RESULT_PREDICTED_WIN_RATE);
//			cModel.saveSelectedFileForMarkets(m5pOutput);
//			Instances mlpOutput=mergeResultWithData(mlpResult,m5pResult,ArffFormat.RESULT_PREDICTED_PROFIT);
//			nModel.saveSelectedFileForMarkets(mlpOutput);
			
			//用最新的单次交易数据，更新原始的交易数据文件
//			int startYear=2005;
//			int endYear=2016;
//			refreshArffFile(startYear,endYear);
//			compareRefreshedInstancesForYear(2008,100);
//			compareRefreshedInstancesForYear(2015,100);

			//为原始的历史文件Arff添加计算变量，并分拆，因为其数据量太大，所以提前处理，不必每次分割消耗内存
//			processHistoryFile();

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}


	@Deprecated 
	// replaced by compareRefreshedInstances which is more effecient 
	//此方法用于比较原始文件和refreshed文件之间的差异
	// 根据原始文件的格式ORIGINAL_TRANSACTION_ARFF_FORMAT
	//TRADE_DATE （2）,"code"（3） 和 SELECTED_MA（9） 是唯一性键值
	// checkSample 是指抽样率，如果是1表示每行都要比较，如果是100，则为每100类记录中抽取一条比较 
	protected static void compareRefreshedInstancesForYear(int year,int checkSample) throws Exception {
		
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + year + "01) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ year + "12) ";
		String filePrefix=C_ROOT_DIRECTORY+"AllTransaction20052016";
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

	protected static void compareRefreshedInstances() throws Exception {
		
		String filePrefix=C_ROOT_DIRECTORY+"AllTransaction20052016";
		Instances originData=FileUtility.loadDataFromFile(filePrefix+"-origin.arff");
		int originDataSize=originData.numInstances();
		System.out.println("loaded original file into memory, number= "+originDataSize);
		
//		Attribute dateCodeMA
//		originData.insertAttributeAt(, position);
		for (int i=0;i<originDataSize;i++){
			
		}

		
		Instances refreshedData=FileUtility.loadDataFromFile(filePrefix+".arff");
		int refreshedDataSize=refreshedData.numInstances();
		System.out.println("loaded refreshed file into memory, number= "+refreshedDataSize);
		System.out.println("compare originData and newData header,result is :"+originData.equalHeadersMsg(refreshedData));
		
		
		
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
		String originFileName=C_ROOT_DIRECTORY+"AllTransaction20052016";
		Instances fullData = FileUtility.loadDataFromFile(originFileName+"-origin.arff");
		
		//做这个处理是因为不知为何有时id之前会出现全角空格
		if (ArffFormat.ID.equals(fullData.attribute(ArffFormat.ID_POSITION-1).name())==false){
			fullData.renameAttribute(ArffFormat.ID_POSITION-1, ArffFormat.ID);
		}
		//将股票代码，交易日期之类的字段变换为String格式
		fullData=FilterData.NominalToString(fullData, "3-6,8");


		System.out.println("finish  loading original File row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		

		for (int i=startYear;i<=endYear;i++){
			fullData = refreshArffForOneYear(i,C_ROOT_DIRECTORY+"onceyield"+i+".txt",fullData);
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



	protected static void processHistoryFile() throws Exception {
		System.out.println("loading history file into memory "  );
		String originFileName=C_ROOT_DIRECTORY+"AllTransaction20052016";
		Instances fullSetData = FileUtility.loadDataFromFile(originFileName+".arff");
		System.out.println("finish  loading fullset File  row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		// 去除与训练无关的字段
		Instances result=ArffFormat.processAllTransaction(fullSetData);
//		//保存训练用的format，用于做日后的校验 
		Instances format=new Instances(result,0);
		FileUtility.SaveDataIntoFile(format, originFileName+"-format.arff");		
		
		result=ArffFormat.addCalculateAttribute(result);
		FileUtility.SaveDataIntoFile(result, originFileName+"-new.arff");
		System.out.println("full Set Data File saved "  );
		
		// 存下用于计算收益率的数据
		Instances left=ArffFormat.getTransLeftPartFromAllTransaction(fullSetData);
		FileUtility.SaveDataIntoFile(left, C_ROOT_DIRECTORY+ALL_TRANSACTION_LEFT_ARFF);
		System.out.println("history Data left File saved "  );
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
		Instances fullData = DBAccess.LoadDataFromDB();

		
		predict(clModel, pathName, fullData);
		System.out.println("Database updated and mission completed.");
	}
	
	//用模型预测数据
	private static Instances predict(MyClassifier clModel, String pathName, Instances inData) throws Exception {
		Instances newData = null;
		Instances result = null;

		//TODO 调整nominal属性
		Instances fullData=calibrateAttributesForDailyData(pathName, inData);
	
		//把计算字段加上
		fullData=ArffFormat.addCalculateAttribute(fullData);		
		
		FileUtility.SaveDataIntoFile(fullData, pathName+FormatUtility.getDateStringFor(1)+"dailyInput-new116.arff");
		
		//获得”均线策略"的位置属性
		int maIndex=FilterData.findATTPosition(fullData,ArffFormat.SELECTED_MA);

		//FileUtility.SaveDataIntoFile(fullData, pathName+"data-new116.csv");
		if (clModel instanceof NominalClassifier ){
			fullData=((NominalClassifier)clModel).processDataForNominalClassifier(fullData);
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
			System.out.println(" new data size , row : "
					+ newData.numInstances() + " column: "
					+ newData.numAttributes());
			
			String modelFileName;
			String evalFileName;
			if (clModel instanceof NominalClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName
						+ "\\交易分析2005-2016 by month-new-mlp-2016 MA " + clModel.m_policySubGroup[j]	;				
				evalFileName = pathName+"\\"+clModel.classifierName
						+ "\\交易分析2005-2016 by month-new-mlp-201605 MA " + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}else{
				modelFileName = pathName+"\\"+clModel.classifierName
						+ "\\交易分析2005-2016 by month-new-m5p-201605 MA " + clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName
						+ "\\交易分析2005-2016 by month-new-m5p-201605 MA " + clModel.m_policySubGroup[j]+MyClassifier.THRESHOLD_EXTENSION	;				
			}

			clModel.setModelFileName(modelFileName);
			clModel.setEvaluationFilename(evalFileName);
	

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
 
			result = clModel.predictData(newData, result);
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
		System.out.println("test backward using classifier : "+clModel.classifierName);
		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) { // if i starts from 1, the
														// first one is to use
														// as validateData
			
			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   "+splitMark);
			// 加载原始arff文件
			if (fullSetData == null) {

				System.out.println("start to load File for fullset from File: "+ clModel.ARFF_FILE  );
				fullSetData = FileUtility.loadDataFromFile( C_ROOT_DIRECTORY+clModel.ARFF_FILE);
				System.out.println("finish loading fullset Data. row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
				if (clModel instanceof NominalClassifier ){
				//	fullSetData=FilterData.getInstancesSubset(fullSetData, "ATT2>201601");
					
					fullSetData=((NominalClassifier)clModel).processDataForNominalClassifier(fullSetData);
					//TODO 因为历史原因，201605018之前build的二分类器模型 “均线策略" 都叫"policy"，所以调用模型前先改一下名，调用模型后再改回来
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
				result = doOneModel(clModel, fullSetData, result, splitMark,
						policy, lower_limit, upper_limit,tp_fp_ratio, splitTrainClause,
						splitTestClause);
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
					result = doOneModel(clModel, fullSetData, result,
							splitMark, policy, lower_limit, upper_limit,tp_fp_ratio,
							splitTrainClause, splitTestClause);
				}
			}
			
			System.out.println("********************complete **************************** " + splitMark);
			System.out.println(" ");
		}
		
		clModel.outputClassifySummary();
		if (clModel instanceof NominalClassifier ){
			//TODO 因为历史原因，201605018之前build的二分类器模型 “均线策略" 都叫"policy"，所以调用模型前先改一下名，调用模型后再改回来
			int maIndex=FilterData.findATTPosition(result,"policy");
			result.renameAttribute(maIndex-1, ArffFormat.SELECTED_MA);
		}		
		clModel.saveResultFile(result);
		
		System.out.println(clModel.classifierName+" test result file saved.");
		return result;
	}

	protected static Instances doOneModel(MyClassifier clModel,
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
			System.out.println(" training data size , row : "
					+ trainingData.numInstances() + " column: "
					+ trainingData.numAttributes());
			if (clModel.m_saveArffInBacktest) {
				clModel.saveArffFile(trainingData,"train", yearSplit, policySplit);
			}
		}
		
		if (clModel.m_skipTrainInBacktest == false) { 

			System.out.println("start to build model");
			model = clModel.trainData(trainingData);
		} else {
			model = clModel.loadModel(yearSplit,policySplit);
		}
		if (clModel.m_skipEvalInBacktest == false) {
			// System.out.println("start to split validate data set");
			// validateData = FilterData.splitTrainAndTest(fullSetData,
			// "(ATT2 >= " + splitYear[i-3]+ ") and (ATT2 <= " +splitYear[i-1] +
			// ") and (ATT3 is '" + splitPolicy[j] + "')");
			// validateData = FilterData.removeAttribs(validateData,
			// yearPosition);
			// validateData = FilterData.removeAttribs(validateData,
			// Integer.toString(ID_POSITION));
			// System.out.println(" validateData data size , row : " +
			// validateData.numInstances() + " column: "
			// + validateData.numAttributes());

			clModel.evaluateModel(trainingData, model, lower_limit,
					upper_limit,tp_fp_ratio);
		}
		trainingData=null;

		// apply model part
		System.out.println("start to split testing set");
		testingData = FilterData
				.getInstancesSubset(fullSetData, splitTestClause);
		testingData = FilterData.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);
		System.out.println("testing data size, row: "
				+ testingData.numInstances() + " column: "
				+ testingData.numAttributes());

		if (clModel.m_saveArffInBacktest) {
			clModel.saveArffFile(testingData,"test", yearSplit, policySplit);
		}

		result = clModel.predictData(testingData, result);
		testingData=null;
		System.out.println("accumulated predicted rows: "
				+ result.numInstances());
		System.out.println("complete for " + yearSplit + "均线策略: " + policySplit);
		return result;
	}

	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private static Instances calibrateAttributesForDailyData(String pathName,Instances incomingData) throws Exception {
		
		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		Instances format=FileUtility.loadDataFromFile(pathName+"AllTransaction20052016-format.arff");
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
	
//	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
//	private static void calibrateNominalAttributes(String pathName, Instances incomingData) throws Exception {
//
//		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
//		Instances format=FileUtility.loadDataFromFile(pathName+"AllTransaction20052016-format.arff");
//		format=FilterData.removeAttribs(format, "2");
//		System.out.println("!!!!!verifying input data format , you should read this .... "+ format.equalHeadersMsg(incomingData));
//		for (int n = 0; n < incomingData.numAttributes() - 1; n++) { 
//			Attribute incomingAtt = incomingData.attribute(n);
//			Attribute formatAtt=format.attribute(n);
//			String formatAttName=formatAtt.name();
//			String incomingAttName=incomingAtt.name();
//			if (formatAtt.isNominal()) {
//				if (formatAttName.equals(incomingAttName)){
//					System.out.println("processing incoming attribute: "+incomingAttName+ " using format attribute: "+formatAttName);
//					Enumeration enu=formatAtt.enumerateValues();
//					List<String> list = Collections.list(enu);
//					
//
//					for (String label : list) {
//						int index=formatAtt.indexOfValue(label);
//						String value=String.valueOf(index);
//						System.out.println("index:label= " + value+" : "+label);
////						incomingData.renameAttributeValue(incomingAtt, value, label);
//					}
//				}else {
//					//throw new Exception("Attribute order error! "+ incomingAttName + " vs. "+ formatAttName);
//					System.out.println("Attribute order error! "+ incomingAttName + " vs. "+ formatAttName);
//				}
//			}
//
//		}
//	}
	
	
	protected static Instances mergeResultWithData(Instances resultData,Instances referenceData,String dataToAdd) throws Exception{
		//读取磁盘上预先保存的左侧数据
		Instances left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ALL_TRANSACTION_LEFT_ARFF);


	    // 创建输出结果
	    Instances mergedResult = new Instances(left, 0);
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_PROFIT, mergedResult.numAttributes());
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_WIN_RATE, mergedResult.numAttributes());
	    mergedResult=FilterData.AddAttribute(mergedResult,ArffFormat.RESULT_SELECTED, mergedResult.numAttributes());

		
		int processed=0;
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


		for (int i=0;i<left.numInstances();i++){	
			leftCurr=left.instance(i);
			resultCurr=resultData.instance(processed);
			referenceCurr=referenceData.instance(processed);
			if (leftCurr.value(0)==resultCurr.value(0)){//找到相同ID的记录了，接下来做冗余字段的数据校验
				if ( checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA,leftBias5, resultBias5)) {
					newData=new DenseInstance(mergedResult.numAttributes());
					newData.setDataset(mergedResult);
					for (int n = 0; n < leftCurr.numAttributes(); n++) { 
						Attribute att = leftCurr.attribute(n);
						if (att != null) {
							if (att.isNominal()) {
								String label = leftCurr.stringValue(att);
								int index = att.indexOfValue(label);
								if (index != -1) {
									newData.setValue(n, index);
								} //这里如果left里面的数据没有值，也就不必设值了
							} else if (att.isNumeric()) {
								newData.setValue(n, leftCurr.value(att));
							} else if (att.isString()) {
								String label = leftCurr.stringValue(att);
								newData.setValue(n, label);
							} else {
								throw new IllegalStateException("Unhandled attribute type!");
							}
						}
					}

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
					processed++;
					if (processed % 100000 ==0){
						System.out.println("number of results processed:"+ processed);
					}
					if (processed>=resultData.numInstances()){
						break;
					}
				}else {
					throw new Exception("data value in header data and result data does not equal "+leftCurr.value(leftMA)+" = "+resultCurr.value(resultMA)+ " / "+leftCurr.value(leftBias5) + " = "+resultCurr.value(resultBias5));
				}
			}
		}// end left processed
		if (processed!=resultData.numInstances()){
			throw new Exception("not all data in result have been processed , processed= "+processed+" ,while total result="+resultData.numInstances());
		}else {
			System.out.println("number of results merged and processed: "+ processed);
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
}