package yueyueGo;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;

public class FilterData {
	public static final String WEKA_ATT_PREFIX = "ATT";

	// 转换numeric为nominal
	public static Instances numToNominal(Instances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // attribute position
		NumericToNominal convert = new NumericToNominal();
		convert.setOptions(options);
		convert.setInputFormat(data);
		Instances newData = Filter.useFilter(data, convert); // apply filter
		return newData;
	}
	
	// 转换numeric为String
	public static Instances NominalToString(Instances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-C"; // "range"
		options[1] = attribPos; // attribute position
		NominalToString convert = new NominalToString();
		convert.setOptions(options);
		convert.setInputFormat(data);
		Instances newData = Filter.useFilter(data, convert); // apply filter
		return newData;
	}
	
	
	
	//删除指定的列（此处的index是从1开始）
	public static Instances removeAttribs(Instances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // first attribute
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInputFormat(data); // inform filter about dataset **AFTER**
										// setting options
		Instances newData = Filter.useFilter(data, remove); // apply filter
		return newData;
	}
	
	// 根据给定公式，获取数据集的子集
	public static Instances getInstancesSubset(Instances data, String expression)
			throws Exception {
		//System.out.println(" get Instances subset using expression: "+expression);
		SubsetByExpression subset = new SubsetByExpression();
		String[] options = new String[2];
		options[0] = "-E"; // "range"
		options[1] = expression; // attribute position
		subset.setOptions(options);
		subset.setInputFormat(data);
		Instances output = Filter.useFilter(data, subset);
		return output;
	}

	//在position的位置插入新的属性 （position从0开始） ，这个方法会创建新的instances后再插入，所以似乎可以直接调用原有instances中的insertAttributeAt方法
	public static Instances AddAttribute(Instances data, String attributeName,
			int position) {
		Instances newData = new Instances(data);
		newData.insertAttributeAt(new Attribute(attributeName), position);
		return newData;
	}
	



	
	// 将给定记录集中属于特定指数的数据筛选出来
	public static Instances filterDataForIndex(Instances origin, String indexName) throws Exception{
		//找出所属指数的位置
		int pos = findATTPosition(origin,indexName);
		return getInstancesSubset(origin,WEKA_ATT_PREFIX+pos+" is '"+ ArffFormat.VALUE_YES+"'");
	}
	
	// 找到指定数据集中属性所处位置（从1开始）
	public static int findATTPosition(Instances origin,String attName) {
		int pos=-1;
		for (int i=0;i<origin.numAttributes();i++){
			if (origin.attribute(i).name().equals(attName)){
				pos=i+1; // 找到指数所属第几个参数（从1开始），用于过滤时用
				break;
			}
		}
		return pos;
	}


}
