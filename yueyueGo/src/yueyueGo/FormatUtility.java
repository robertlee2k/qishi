package yueyueGo;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FormatUtility {
	// 获取指定偏移量的日期String
	public static String getDateStringFor(int offset) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, offset);
		String dateString = new SimpleDateFormat("yyyyMMdd ").format(cal
				.getTime());
		return dateString;
	}

	/**
	 * 将double类型数据转换为百分比格式，并保留小数点前IntegerDigits位和小数点后FractionDigits位
	 * 
	 * @param d
	 * @param IntegerDigits
	 * @param FractionDigits
	 * @return
	 */
	public static String formatPercent(double d, int IntegerDigits,
			int FractionDigits) {
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumIntegerDigits(IntegerDigits);// 小数点前保留几位
		nf.setMinimumFractionDigits(FractionDigits);// 小数点后保留几位
		String str = nf.format(d);
		return str;
	}

	// 缺省保留百分号小数点前两位，后一位
	public static String formatPercent(double d) {
		return formatPercent(d, 2, 1);
	}

	// 缺省保留小数点后二位
	public static String formatDouble(double d) {
		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumFractionDigits(2);
		return df.format(d);
	}

	public static String formatDouble(double d, int integerDigits,
			int fractionDigits) {
		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumIntegerDigits(integerDigits);// 小数点前保留几位
		df.setMinimumFractionDigits(fractionDigits);// 小数点后保留几位
		return df.format(d);
	}

	/**
	 * @param tradeDate
	 * @throws ParseException
	 */
	public static double parseYearMonth(String tradeDate) throws ParseException {
		SimpleDateFormat ft = new SimpleDateFormat("yyyy/M/d");
		Date tDate = ft.parse(tradeDate);

		Calendar cal = Calendar.getInstance();
		cal.setTime(tDate);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1;
		double ym = y * 100 + m;
		return ym;
	}
}
