package yueyueGo;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ArffFormat {
	public static final int LEGACY_FORMAT=-1;
	public static final int EXT_FORMAT=2;
	
	public static final String LONG_ARFF_FILE = "AllTransaction20052016-ext-new.arff";//"AllTransaction20052016-new.arff"; // 包含计算字段的ARFF格式，这是提供给各输入属性独立的分类器使用的，如分类树
	public static final String SHORT_ARFF_FILE = "AllTransaction20052016-ext-short.arff";//"AllTransaction20052016-short.arff";// 不包含计算字段的ARFF格式，这是提供给各输入属性独立的分类器使用的，如神经网络

	
	public static final String SELECTED_MA = "均线策略";
	public static final String SELECTED_MA_IN_OTHER_SYSTEM = "selected_avgline"; // 输入输出文件中的“均线策略”名称
	public static final String IS_HS300 = "属沪深300指数";
	public static final String IS_ZZ500 = "属中证500指数";
	public static final String IS_SZ50 = "属上证50指数";
	public static final String SHOUYILV = "shouyilv";
	public static final String IS_POSITIVE = "positive";
	public static final String VALUE_YES = "1";
	public static final String VALUE_NO = "0";

		
	public static final String RESULT_PREDICTED_PROFIT = "PredictedProfit";
	public static final String RESULT_SELECTED = "selected";
	public static final String RESULT_PREDICTED_WIN_RATE="PredictedWinRate";

	public static final String TRADE_DATE = "tradeDate"; // 之所以定义这个字段，是因为所有的数据都要以它排序
	public static final String SELL_DATE = "mc_date";
	public static final String DATA_DATE = "dataDate";

	public static final String ID = "id";
	public static final int ID_POSITION = 1; // ID的位置
	public static final String YEAR_MONTH_INDEX = "2"; // yearmonth所处位置，理论上说可以不用这个定义，用findAttPosition查找，暂时保留吧


	// 用于training的数据顺序（最短格式，无计算字段的）
	public static final String[] TRAINING_ARFF_SHORT_FORMAT = { SELECTED_MA, "bias5",
			"bias10", "bias20", "bias30", "bias60", "bias5前日差", "bias10前日差",
			"bias20前日差", "bias30前日差", "bias60前日差", "bias5二日差", "bias10二日差",
			"bias20二日差", "bias30二日差", "bias60二日差", "ma5前日比", "ma10前日比",
			"ma20前日比", "ma30前日比", "ma60前日比", "ma5二日比", "ma10二日比", "ma20二日比",
			"ma30二日比", "ma60二日比", "ma5三日比", "ma10三日比", "ma20三日比", "ma30三日比",
			"ma60三日比", "ma5四日比", "ma10四日比", "ma20四日比", "ma30四日比", "ma60四日比",
			"ma5五日比", "ma10五日比", "ma20五日比", "ma30五日比", "ma60五日比", "涨跌幅", "换手率",
			"换手前日比", "换手二日比", "换手三日比", "指数code", "申万行业指数code", IS_SZ50,
			IS_HS300, "属中证100指数", IS_ZZ500, "属深证100指数", "属沪股通标的", "属融资标的",
			"sw行业bias5", "sw行业bias10", "sw行业bias20", "sw行业bias30",
			"sw行业bias60", "swbias5前日差", "swbias10前日差", "swbias20前日差",
			"swbias30前日差", "swbias60前日差", "swbias5二日差", "swbias10二日差",
			"swbias20二日差", "swbias30二日差", "swbias60二日差", "指数bias5", "指数bias10",
			"指数bias20", "指数bias30", "指数bias60", "指数bias5前日差", "指数bias10前日差",
			"指数bias20前日差", "指数bias30前日差", "指数bias60前日差", "指数bias5二日差",
			"指数bias10二日差", "指数bias20二日差", "指数bias30二日差", "指数bias60二日差"
	// "shouyilv" 最后一个可能是shouyilv也有可能是positive
	};

	//单次交易收益率的扩展ARFF格式之校验位
	public static final String[] INCREMENTAL_EXT_ARFF_LEFT= {
		ID,TRADE_DATE,"code",SELL_DATE,DATA_DATE,SELECTED_MA,"bias5前日差","指数code",
	};
	//单次交易收益率的扩展ARFF格式之第二批数据
	public static final String[] INCREMENTAL_EXT_ARFF_RIGHT= {
		"zhishu_quantity_preday_perc","zhishu_quantity_pre2day_perc","zhishu_quantity_pre3day_perc","zhishu_ma5_indicator","zhishu_ma10_indicator","zhishu_ma20_indicator","zhishu_ma30_indicator","zhishu_ma60_indicator","sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator","ma5_signal_scale","ma10_signal_scale","ma20_signal_scale","ma30_signal_scale","ma60_signal_scale"
	};
	//单次交易收益率的扩展ARFF格式之第三批数据
	public static final String[] INCREMENTAL_EXT_ARFF_RIGHT2= {
		"zhangdieting","shangying","xiaying","index_shangying","index_xiaying","yearhighbias","yearlowbias","monthhighbias","monthlowbias","index_yearhighbias","index_yearlowbias","index_monthhighbias","index_monthlowbias"
	};
	
	//单次交易收益率的扩展ARFF格式之第四批数据
	public static final String[] INCREMENTAL_EXT_ARFF_RIGHT3= {
		"circulation_marketVal_gears","PE_TTM","PE_TTM_gears","PE_LYR","PE_LYR_gears","listed_days_gears"	
	};
	
	
	
	// 交易ARFF数据全集数据的格式 （从ID到均线策略，后面都和trainingarff的相同了）， 总共10个字段
	public static final String[] ORIGINAL_TRANSACTION_ARFF_FORMAT = { ID,
			"yearmonth", TRADE_DATE, "code", SELL_DATE, "股票名称", "year",
			DATA_DATE, IS_POSITIVE, SELECTED_MA };

	// 单次收益率增量数据的格式 （从ID到均线策略，后面都和dailyArff的相同了）
	public static final String[] INCREMENTAL_ARFF_FORMAT = { ID, TRADE_DATE,
			"code", SELL_DATE, DATA_DATE, IS_POSITIVE,
			SELECTED_MA_IN_OTHER_SYSTEM };

	// 每日数据（数据库和数据文件都是如此)的格式
	public static final String[] DAILY_DATA_TO_PREDICT_FORMAT = { 
		ID,	SELECTED_MA_IN_OTHER_SYSTEM, "bias5", "bias10", "bias20", "bias30",
			"bias60", "bias5_preday_dif", "bias10_preday_dif",
			"bias20_preday_dif", "bias30_preday_dif", "bias60_preday_dif",
			"bias5_pre2day_dif", "bias10_pre2day_dif", "bias20_pre2day_dif",
			"bias30_pre2day_dif", "bias60_pre2day_dif", "ma5_preday_perc",
			"ma10_preday_perc", "ma20_preday_perc", "ma30_preday_perc",
			"ma60_preday_perc", "ma5_pre2day_perc", "ma10_pre2day_perc",
			"ma20_pre2day_perc", "ma30_pre2day_perc", "ma60_pre2day_perc",
			"ma5_pre3day_perc", "ma10_pre3day_perc", "ma20_pre3day_perc",
			"ma30_pre3day_perc", "ma60_pre3day_perc", "ma5_pre4day_perc",
			"ma10_pre4day_perc", "ma20_pre4day_perc", "ma30_pre4day_perc",
			"ma60_pre4day_perc", "ma5_pre5day_perc", "ma10_pre5day_perc",
			"ma20_pre5day_perc", "ma30_pre5day_perc", "ma60_pre5day_perc",
			"zhangdiefu", "huanshoulv", "huanshoulv_preday_perc",
			"huanshoulv_pre2day_perc", "huanshoulv_pre3day_perc",
			"zhishu_code", "sw_zhishu_code", "issz50", "ishs300", "iszz100",
			"iszz500", "issz100", "ishgtb", "isrzbd", "sw_bias5", "sw_bias10",
			"sw_bias20", "sw_bias30", "sw_bias60", "sw_bias5_preday_dif",
			"sw_bias10_preday_dif", "sw_bias20_preday_dif",
			"sw_bias30_preday_dif", "sw_bias60_preday_dif",
			"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif",
			"sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif",
			"sw_bias60_pre2day_dif", "zhishu_bias5", "zhishu_bias10",
			"zhishu_bias20", "zhishu_bias30", "zhishu_bias60",
			"zhishu_bias5_preday_dif", "zhishu_bias10_preday_dif",
			"zhishu_bias20_preday_dif", "zhishu_bias30_preday_dif",
			"zhishu_bias60_preday_dif", "zhishu_bias5_pre2day_dif",
			"zhishu_bias10_pre2day_dif", "zhishu_bias20_pre2day_dif",
			"zhishu_bias30_pre2day_dif", "zhishu_bias60_pre2day_dif"
			//"shouyilv"  //收益率这个字段在每日预测的输入数据中并没有，是loadfromDB或CSV方法中加的空字段。
	};
	
	// 每日扩展数据（数据库和数据文件都是如此)的格式
	public static String[] EXT_DAILY_DATA_TO_PREDICT_FORMAT = create_ext_daily_data_to_predict();
	

	// 读取的数据源（每日预测数据和单次收益率数据）中的日期格式
	public static final String INPUT_DATE_FORMAT = "yyyy/M/d";
	// ARFF文件中的日期格式
	public static final String ARFF_DATE_FORMAT = "M/d/yyyy";

	protected static final String[] create_ext_daily_data_to_predict(){
		String[] ext_formatString=new String[DAILY_DATA_TO_PREDICT_FORMAT.length+INCREMENTAL_EXT_ARFF_RIGHT.length+INCREMENTAL_EXT_ARFF_RIGHT2.length+INCREMENTAL_EXT_ARFF_RIGHT3.length];
		System.arraycopy(DAILY_DATA_TO_PREDICT_FORMAT, 0, ext_formatString, 0, DAILY_DATA_TO_PREDICT_FORMAT.length);  
		System.arraycopy(INCREMENTAL_EXT_ARFF_RIGHT, 0, ext_formatString, DAILY_DATA_TO_PREDICT_FORMAT.length, INCREMENTAL_EXT_ARFF_RIGHT.length);
		System.arraycopy(INCREMENTAL_EXT_ARFF_RIGHT2, 0, ext_formatString, DAILY_DATA_TO_PREDICT_FORMAT.length+INCREMENTAL_EXT_ARFF_RIGHT.length,INCREMENTAL_EXT_ARFF_RIGHT2.length); 
		System.arraycopy(INCREMENTAL_EXT_ARFF_RIGHT3, 0, ext_formatString, DAILY_DATA_TO_PREDICT_FORMAT.length+INCREMENTAL_EXT_ARFF_RIGHT.length+INCREMENTAL_EXT_ARFF_RIGHT2.length,INCREMENTAL_EXT_ARFF_RIGHT3.length);
		System.out.println("ext daily data to predict format: "+ext_formatString);
		return ext_formatString;
		
	}
	
	// 从All Transaction Data中删除无关字段remove attribute: 3-9 (tradeDate到均线策略）
	public static Instances processAllTransaction(Instances allData)
			throws Exception {
		Instances result = InstanceUtility.removeAttribs(allData, "3-9");
		return result;
	}

	// 此方法与上面方法正好相反，从All Transaction Data中保留计算收益率的相关字段保留1-9，以及最后的收益率，删除其他计算字段
	public static Instances getTransLeftPartFromAllTransaction(Instances allData)
			throws Exception {
		// 用查找的方式保留下来中间的属性字段
		int left_column_number = ORIGINAL_TRANSACTION_ARFF_FORMAT.length;
		int bias10Index = left_column_number + 2;
		int codeBegin = 0;
		int codeEnd = 0;
		for (int i = 0; i < TRAINING_ARFF_SHORT_FORMAT.length; i++) {
			if (IS_SZ50.equals(TRAINING_ARFF_SHORT_FORMAT[i])) {
				codeBegin = left_column_number + i - 1;
			} else if (IS_ZZ500.equals(TRAINING_ARFF_SHORT_FORMAT[i])) {
				codeEnd = left_column_number + i + 1;
			}
		}
		Instances result = InstanceUtility.removeAttribs(allData, bias10Index + "-"
				+ codeBegin + "," + codeEnd + "-"
				+ (allData.numAttributes() - 1)); // 第10-11个字段（均线策略，bias5）保留下来做校验用
		return result;
	}

	// 为原始的Arff文件加上计算属性
	public static Instances addCalculateAttribute(Instances data) {
		Instances result = new Instances(data, 0);

		int row = data.numInstances();
		double[][] bias5to60 = { { 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 } };
		String[][] biasAttName = {
				{ "bias5", "bias10", "bias20", "bias30", "bias60" },
				{ "sw行业bias5", "sw行业bias10", "sw行业bias20", "sw行业bias30",
						"sw行业bias60" },
				{ "指数bias5", "指数bias10", "指数bias20", "指数bias30", "指数bias60" } };
		// String[][] biasAttName = {{ "bias5", "bias10", "bias20",
		// "bias30","bias60"},{"sw_bias5","sw_bias10","sw_bias20","sw_bias30","sw_bias60"},{"zhishu_bias5","zhishu_bias10","zhishu_bias20","zhishu_bias30","zhishu_bias60"}
		// };
		for (int x = 0; x < bias5to60.length; x++) {
			for (int m = 0; m < bias5to60[x].length; m++) {
				for (int k = m + 1; k < bias5to60[x].length; k++) {
					// insert before class value
					result.insertAttributeAt(new Attribute(biasAttName[x][m]
							+ "-" + biasAttName[x][k]),
							result.numAttributes() - 1);
				}
			}
		}

		// 为每一行数据处理
		for (int i = 0; i < row; i++) {
			Instance oneRow = data.instance(i);
			for (int x = 0; x < biasAttName.length; x++) {
				for (int j = 0; j < biasAttName[x].length; j++) {
					Attribute attribute = data.attribute(biasAttName[x][j]);
					bias5to60[x][j] = oneRow.value(attribute);
				}
			}
			Instance newRow = new DenseInstance(result.numAttributes());
			newRow.setDataset(result);

			// copy same values
			for (int n = 0; n < data.numAttributes() - 1; n++) {
				Attribute att = data.attribute(newRow.attribute(n).name());
				if (att != null) {
					if (att.isNominal()) {
						String label = oneRow.stringValue(att);
						int index = att.indexOfValue(label);
						if (index != -1) {
							newRow.setValue(n, index);
						}
					} else if (att.isNumeric()) {
						newRow.setValue(n, oneRow.value(att));
					} else {
						throw new IllegalStateException(
								"Unhandled attribute type!");
					}
				}
			}
			// 添加bias相减的部分
			int addColumn = 0;
			int insertPosition = data.numAttributes() - 1;
			for (int x = 0; x < bias5to60.length; x++) {
				for (int m = 0; m < bias5to60[x].length; m++) {
					for (int k = m + 1; k < bias5to60[x].length; k++) {
						newRow.setValue(insertPosition + addColumn,
								bias5to60[x][m] - bias5to60[x][k]);
						addColumn++;
					}
				}
			}
			// 添加最后的classvalue
			newRow.setValue(result.numAttributes() - 1,
					oneRow.value(data.numAttributes() - 1));
			result.add(newRow);

		}
		return result;
	}

	// 将输入文件和training数据顺序对比 ，bypassColumnCount是指需要忽略掉的colum数（比如每日增量里多了一列ID需要忽略）
	//因为201607以后有扩展格式存在， 这个改名仅限于原始的training改名（扩展格式中的名字与原始训练模型中是相同的）
	public static Instances trainingAttribMapper(Instances data,String[] validInputColumns,
			int bypassColumnCount) throws Exception {
		// 把读入的数据改名 以适应内部训练的arff格式
		String incomingColumnName=null;
		for (int i = 0; i < TRAINING_ARFF_SHORT_FORMAT.length; i++) {
			incomingColumnName=data.attribute(i + bypassColumnCount).name();
			if (incomingColumnName.equals(validInputColumns[i+1])){
				System.out.println("rename input db column name ["
						+ data.attribute(i + bypassColumnCount).name()
						+ "] to training attribuate name ["
						+ ArffFormat.TRAINING_ARFF_SHORT_FORMAT[i] + "]");
				data.renameAttribute(i + bypassColumnCount,
						TRAINING_ARFF_SHORT_FORMAT[i]);

			}else {
				throw new Exception("input data column name is invalid! input column="+incomingColumnName+ " valid column should be:"+validInputColumns[i+1]);
			}
		}
		return data;
	}


	// 判断是否为沪深300、中证500、上证50
	protected static boolean belongToIndex(Instance curr, String indexName) {
		Attribute hsAtt = curr.dataset().attribute(indexName);
		String label = curr.stringValue(hsAtt);
		if (label.equals(VALUE_YES)) { // 故意用这个顺序，如果label是null就扔exception出去
			return true;
		} else {
			return false;
		}
	}

	// 判断是否为沪深300
	public static boolean isHS300(Instance curr) {
		return belongToIndex(curr, IS_HS300);
	}

	// 判断是否为中证500
	public static boolean isZZ500(Instance curr) {
		return belongToIndex(curr, IS_ZZ500);
	}

	// 判断是否为上证50
	public static boolean isSZ50(Instance curr) {
		return belongToIndex(curr, IS_SZ50);
	}


}
