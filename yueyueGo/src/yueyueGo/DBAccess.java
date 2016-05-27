package yueyueGo;

import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class DBAccess  {
	public final static String URL = "jdbc:mysql://uts.simu800.com/develop?characterEncoding=utf8&autoReconnect=true";
	public final static String USER = "root";
	public final static String PASSWORD = "data@2014";
	protected final static String QUERY_DATA="SELECT  `id`, `selected_avgline`, `bias5`,  `bias10`,  `bias20`,  `bias30`,  `bias60`, `bias5_preday_dif`,  `bias10_preday_dif`,  `bias20_preday_dif`,  `bias30_preday_dif`,  `bias60_preday_dif`,  `bias5_pre2day_dif`,  `bias10_pre2day_dif`,  `bias20_pre2day_dif`,  `bias30_pre2day_dif`,  `bias60_pre2day_dif`, `ma5_preday_perc`,  `ma5_pre2day_perc`,  `ma5_pre3day_perc`,  `ma5_pre4day_perc`,  `ma5_pre5day_perc`,  `ma10_preday_perc`,  `ma10_pre2day_perc`,  `ma10_pre3day_perc`,  `ma10_pre4day_perc`,  `ma10_pre5day_perc`, `ma20_preday_perc`,  `ma20_pre2day_perc`,  `ma20_pre3day_perc`,  `ma20_pre4day_perc`,  `ma20_pre5day_perc`, `ma30_preday_perc`,  `ma30_pre2day_perc`,  `ma30_pre3day_perc`,  `ma30_pre4day_perc`,  `ma30_pre5day_perc`, `ma60_preday_perc`,  `ma60_pre2day_perc`,  `ma60_pre3day_perc`,  `ma60_pre4day_perc`,  `ma60_pre5day_perc`, `zhangdiefu`,  `huanshoulv`, `huanshoulv_preday_perc`,  `huanshoulv_pre2day_perc`,  `huanshoulv_pre3day_perc`, `zhishu_code`,   `sw_zhishu_code`, `issz50`,  `ishs300`, `iszz100`, `iszz500`, `issz100`,  `ishgtb`,   `isrzbd`, `sw_bias5`, `sw_bias10`, `sw_bias20`, `sw_bias30`, `sw_bias60`, `sw_bias5_preday_dif`, `sw_bias10_preday_dif`, `sw_bias20_preday_dif`, `sw_bias30_preday_dif`,  `sw_bias60_preday_dif`, `sw_bias5_pre2day_dif`, `sw_bias10_pre2day_dif`,   `sw_bias20_pre2day_dif`,   `sw_bias30_pre2day_dif`,   `sw_bias60_pre2day_dif`, `zhishu_bias5`, `zhishu_bias10`, `zhishu_bias20`,  `zhishu_bias30`,   `zhishu_bias60`,  `zhishu_bias5_preday_dif`, `zhishu_bias10_preday_dif`,  `zhishu_bias20_preday_dif`, `zhishu_bias30_preday_dif`,  `zhishu_bias60_preday_dif`,  `zhishu_bias5_pre2day_dif`,   `zhishu_bias10_pre2day_dif`,  `zhishu_bias20_pre2day_dif`, `zhishu_bias30_pre2day_dif`, `zhishu_bias60_pre2day_dif`,  `shouyilv` FROM t_stock_avgline_increment_zuixin_v ";
			
	
	
	public DBAccess() {
		// TODO Auto-generated constructor stub
	}
	
	public static Instances LoadDataFromDB() throws Exception{
//		MyDatabaseLoader loader = new MyDatabaseLoader();
//		loader.setUrl(URL);
//		loader.setUser(USER);
//		loader.setPassword(PASSWORD);
//		loader.setQuery(QUERY_DATA); 
//		Instances data=loader.getDataSet();
//
//		
//		// Create nominal attribute "position" 
//		Attribute position = new Attribute("position", my_nominal_values);
//		data=FilterData.nomToString(data, "2,48-56");
		
		// load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(URL);
		query.setUsername(USER);
		query.setPassword(PASSWORD);
		query.setQuery(QUERY_DATA); 
		Instances data = query.retrieveInstances();
//		//全部读进来之后再转nominal，否则直接加载， nominal的值的顺序会和文件顺序有关，造成数据不对！！！
		data=FilterData.numToNominal(data, "2,48-56");

		//把读入的数据改名 以适应内部训练的arff格式,读入的数据里多了第一列的ID
		data=ArffFormat.trainingAttribMapper(data,1);
		data.setClassIndex(data.numAttributes()-1);
		System.out.println("records loaded from database: "+data.numInstances());
		return data;
	}


	
	public static int SavaDataIntoDB(Instances data){
		int processed=0;
		return processed;
	}
}
