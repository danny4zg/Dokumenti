package hr.kingict.svis.obrasci.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import hr.kingict.svis.obrasci.param.Const;

public final class SearchUtil {
	
	public static String testIfEmpty(String test){
		String s = Const.SEARCH_MORE;
		test = checkInput(test);
		
		if (test != null && !Const.BLANK.equals(test)) {
			s = test;
			
			System.out.println("INPUT: " + test);
			System.out.println("CORRECTION: " + s);
		}
		
		return s;
	}
	
	public static String testIfContainsMode(String test){
		String s = Const.SEARCH_MORE;
		test = checkInput(test);
		
		if (test != null && !Const.BLANK.equals(test) && !Const.SEARCH_MORE.equals(test)) {
			
			s = test + Const.SEARCH_MORE;
			
			System.out.println("INPUT: " + test);
			System.out.println("CORRECTION: " + s);
		}
		
		return s;
	}
	
	private static String checkInput(String parm){
		String pattern = "(%?)(\\wČčĆćŽžŠšĐđ)*[\\s-(\\wČčĆćŽžŠšĐđ)]*";
		
		try {
			if (parm.matches(pattern))
				return parm;
			else 
				return Const.BLANK;
		} 
		catch (Exception e){
			e.printStackTrace();
		} 
		
		return Const.BLANK;
	}
	
	private static Integer checkNumber(Integer num){
		Integer res = null;
		
		if (num != null) res = num;
		
		return res;
	}
	
	public static String convertNumber(Integer num){
		String res = Const.BLANK;
		
		if (num != null) res = num.toString();
		
		return res;
	}
	
	public static String convertDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
		String res;
		
		if (date != null){
			res = format.format(date);
		}
		else {
			res = format.format(new Date());
		}
		
		return res;
	}
}
