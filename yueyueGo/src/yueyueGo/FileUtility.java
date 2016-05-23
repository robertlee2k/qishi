package yueyueGo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class FileUtility {
	// load full set of data
	protected static Instances loadDataFromFile(String fileName)
			throws Exception {
		DataSource source = new DataSource(fileName); 
		Instances data = source.getDataSet();

		// setting class attribute if the data format does not provide this
		// information
		if (data.classIndex() == -1)
		  data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

	// load full set of data from CSV File
	protected static Instances loadDataFromCSVFile(String fileName)
			throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(fileName));

		
//		// 在这里才能设置读取的哪个字段是nominal， 永远别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
//		loader.setNominalAttributes("2,48-56"); 不要这么直接设nominal！
		
		loader.setNumericAttributes("87");//把收益率设为numeric
		Instances datasrc = loader.getDataSet();
		
		//全部读进来之后再转nominal，否则直接加载， nominal的值的顺序会和文件顺序有关，造成数据不对！！！
		datasrc=FilterData.numToNominal(datasrc, "2,48-56");
		
		
		// 把读入的数据改名 以适应内部训练的arff格式，注意读入的数据里多了第一列的ID
		datasrc=ArffFormat.trainingAttribMapper(datasrc);
		if (datasrc.classIndex() == -1)
			  datasrc.setClassIndex(datasrc.numAttributes() - 1);
		return datasrc;
	}
	protected static void SaveDataIntoFile(Instances dataSet, String fileName) throws IOException {

		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(fileName));
		saver.writeBatch();

	}
	protected static void saveCSVFile(Instances data, String fileName) throws IOException {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(data);
		saver.setFile(new File(fileName));
		saver.writeBatch();
	}
	
	public static void write(String path, String content, String encoding) throws IOException {
		File file = new File(path);
		file.delete();
		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(content);
		writer.close();
	}

	public static String read(String path, String encoding) throws IOException {
		String content = "";
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}
	
	public static void convertMyModelToWekaModel(String path,String modelFileName)
			throws Exception {
		
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(path+modelFileName);
		Classifier model = (Classifier) v.get(0);
		SerializationHelper.write(path+"WEKA-"+modelFileName, model);
		
	}
}
