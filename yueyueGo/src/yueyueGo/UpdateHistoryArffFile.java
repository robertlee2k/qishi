package yueyueGo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class UpdateHistoryArffFile {

	/**
	 * @throws Exception
	 */
	protected static void callRefreshInstances() throws Exception {
		int startYear=2016;
		int endYear=2016;
		UpdateHistoryArffFile.refreshArffFile(startYear,endYear);
		UpdateHistoryArffFile.compareRefreshedInstancesForYear(2010,100);
		UpdateHistoryArffFile.compareRefreshedInstancesForYear(2016,5);
	}

	// replaced by compareRefreshedInstances which is more effecient 
	//此方法用于比较原始文件和refreshed文件之间的差异
	// 根据原始文件的格式ORIGINAL_TRANSACTION_ARFF_FORMAT
	//TRADE_DATE （2）,"code"（3） 和 SELECTED_MA（9） 是唯一性键值
	// checkSample 是指抽样率，如果是1表示每行都要比较，如果是100，则为每100类记录中抽取一条比较 
	protected static void compareRefreshedInstancesForYear(int year,int checkSample) throws Exception {
		
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + year + "01) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ year + "12) ";
		String filePrefix=ProcessData.C_ROOT_DIRECTORY+ProcessData.TRANSACTION_ARFF_PREFIX;
		Instances originData=FileUtility.loadDataFromFile(filePrefix+"-origin.arff");
		originData=InstanceUtility.getInstancesSubset(originData, splitSampleClause);
		
		int originDataSize=originData.numInstances();
		System.out.println("loaded original file into memory, number= "+originDataSize);
		Instances refreshedData=FileUtility.loadDataFromFile(filePrefix+".arff");
		refreshedData=InstanceUtility.getInstancesSubset(refreshedData, splitSampleClause);
		
		int refreshedDataSize=refreshedData.numInstances();
		System.out.println("loaded refreshed file into memory, number= "+refreshedDataSize);
		System.out.println("compare originData and newData header,result is :"+originData.equalHeadersMsg(refreshedData));
		
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=InstanceUtility.findATTPosition(originData, ArffFormat.TRADE_DATE);
		originData.sort(tradeDateIndex-1);
	
		System.out.println("data sorted on tradeDate");
		
		int codeIndex=InstanceUtility.findATTPosition(originData, "code");
		int maIndex=InstanceUtility.findATTPosition(originData, ArffFormat.SELECTED_MA);
		
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
	
			originDailyData=InstanceUtility.getInstancesSubset(originData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
			refreshedDailyData=InstanceUtility.getInstancesSubset(refreshedData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
			
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
						if (UpdateHistoryArffFile.compareRefreshedRow(maIndex, originRow,refreshedRow, tradeDate, code)==false){
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
		String originFileName=ProcessData.C_ROOT_DIRECTORY+ProcessData.TRANSACTION_ARFF_PREFIX;
		Instances fullData = FileUtility.loadDataFromFile(originFileName+"-origin.arff");
		
		//做这个处理是因为不知为何有时id之前会出现全角空格
		if (ArffFormat.ID.equals(fullData.attribute(ArffFormat.ID_POSITION-1).name())==false){
			fullData.renameAttribute(ArffFormat.ID_POSITION-1, ArffFormat.ID);
		}
		//将股票代码，交易日期之类的字段变换为String格式
		fullData=InstanceUtility.NominalToString(fullData, "3-6,8");
	
	
		System.out.println("finish  loading original File row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		
	
		for (int i=startYear;i<=endYear;i++){
			fullData = UpdateHistoryArffFile.refreshArffForOneYear(i,ProcessData.C_ROOT_DIRECTORY+"sourceData\\onceyield"+i+".txt",fullData);
		}
	
		//保险起见把新数据按日期重新排序，虽然这样比较花时间，但可以确保日后处理时按tradeDate升序。
		fullData.sort(2);
		System.out.println("refreshed arff file sorted, start to save.... number of rows="+fullData.numInstances());
		FileUtility.SaveDataIntoFile(fullData, originFileName+".arff");
		System.out.println("refreshed arff file saved. ");
	
		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + (endYear-1) + "06) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ endYear + "12) ";
		Instances sampleData=InstanceUtility.getInstancesSubset(fullData, splitSampleClause);
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
		fullData=InstanceUtility.getInstancesSubset(fullData, splitCurrentYearClause);
		
		int filteredNumber=fullData.numInstances() ;
		System.out.println("number of rows removed = "+ (originInstancesNum-filteredNumber));
	
		
		Instances newData = FileUtility.loadDataFromIncrementalCSVFile(newDataFile);
		
				
		
		
		//将单次收益率增量数据修正成为原始文件数据格式（yearmonth重新计算，插入一列空的股票代码和year（这两列暂时用不上），改名“均线策略”）
		int yearMonthIndex=InstanceUtility.findATTPosition(newData, ArffFormat.ID); //在ID之后插入
		int stockNameIndex=InstanceUtility.findATTPosition(newData, ArffFormat.SELL_DATE); //在MC_DATA之后插入
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
	
		InstanceUtility.calibrateAttributes(newData,fullData);
	
		System.out.println("number of refreshed dataset = "+fullData.numInstances());
		return fullData;
	}

	//这是处理历史全量数据，重新切割生成各种长、短以及格式文件的方法
		protected static void processHistoryFile() throws Exception {
			System.out.println("loading history file into memory "  );
			String originFileName=ProcessData.C_ROOT_DIRECTORY+ProcessData.TRANSACTION_ARFF_PREFIX;
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
			FileUtility.SaveDataIntoFile(left, ProcessData.C_ROOT_DIRECTORY+ProcessData.TRANSACTION_ARFF_PREFIX+"-left.arff");
			System.out.println("history Data left File saved: "+ProcessData.TRANSACTION_ARFF_PREFIX+"-left.arff"  );
		}

		//这个函数是将201606之前的数据（只有第一组）合并上新的第二组，第三组数据
		protected static void mergeExtData() throws Exception{
			String file1=null;
			String file2=null;
			Instances extData=null;
			
			file1=ProcessData.C_ROOT_DIRECTORY+"sourceData\\单次收益率第二组数据2005_2010.txt";
			file2=ProcessData.C_ROOT_DIRECTORY+"sourceData\\单次收益率第二组数据2011_20160531.txt";
			extData = UpdateHistoryArffFile.mergeInstancesFromTwoFiles(file1, file2);
			System.out.println("Group 2 full ext data loaded. number="+extData.numInstances());
			
			//加载原始arff文件
			String originFileName=ProcessData.C_ROOT_DIRECTORY+"AllTransaction20052016";
			Instances fullData = FileUtility.loadDataFromFile(originFileName+".arff");
			System.out.println("full trans data loaded. number="+fullData.numInstances());
			
			//将两边数据以ID排序
			fullData.sort(ArffFormat.ID_POSITION-1);
			extData.sort(ArffFormat.ID_POSITION-1);
			System.out.println("all data sorted by id");
			
		
			Instances result=UpdateHistoryArffFile.mergeTransactionWithExtension(fullData,extData,ArffFormat.INCREMENTAL_EXT_ARFF_RIGHT);
			System.out.println("group 2 ext data processed. number="+result.numInstances()+" columns="+result.numAttributes());
			extData=null;
			fullData=null;
			
			
			//处理第三组数据
			file1=ProcessData.C_ROOT_DIRECTORY+"sourceData\\单次收益率第三组数据2005_2010.txt";
			file2=ProcessData.C_ROOT_DIRECTORY+"sourceData\\单次收益率第三组数据2011_20160531.txt";
			extData = UpdateHistoryArffFile.mergeInstancesFromTwoFiles(file1, file2);
			System.out.println("Group 3 full ext data loaded. number="+extData.numInstances());
		
			extData.sort(ArffFormat.ID_POSITION-1);
			
		
			result=UpdateHistoryArffFile.mergeTransactionWithExtension(result,extData,ArffFormat.INCREMENTAL_EXT_ARFF_RIGHT2);
			System.out.println("group 2 ext data processed. number="+result.numInstances()+" columns="+result.numAttributes());
		
			//返回结果之前需要按TradeDate重新排序
			int tradeDateIndex=InstanceUtility.findATTPosition(result, ArffFormat.TRADE_DATE);
			result.sort(tradeDateIndex-1);
			
			//保留原始的ext文件
			FileUtility.SaveDataIntoFile(result, originFileName+"-ext.arff");
			System.out.println("history Data File saved: "+originFileName+"-ext.arff");
			
			// 存下用于计算收益率的数据
			Instances left=ArffFormat.getTransLeftPartFromAllTransaction(result);
			FileUtility.SaveDataIntoFile(left, ProcessData.C_ROOT_DIRECTORY+originFileName+"-ext-left.arff");
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
				InstanceUtility.copyToNewInstance(oldRow,newRow,0,colSize,0);
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
		    Instances mergedResult=UpdateHistoryArffFile.prepareMergedFormat(new Instances(transData,0), new Instances(extData,0));
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
					InstanceUtility.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
		
					//再拷贝extData中除校验数据之外的数据				
					srcStartIndex=ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length;
					srcEndIndex=ArffFormat.INCREMENTAL_EXT_ARFF_LEFT.length+extDataFormat.length-1;
					targetStartIndex=leftCurr.numAttributes()-1; //接着拷贝
					InstanceUtility.copyToNewInstance(rightCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
		
					//再设置classValue
					srcStartIndex=leftCurr.numAttributes()-1;
					srcEndIndex=leftCurr.numAttributes()-1;
					targetStartIndex=newData.numAttributes()-1;
					InstanceUtility.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
					
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
