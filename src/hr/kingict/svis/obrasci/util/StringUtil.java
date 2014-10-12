/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.kingict.svis.obrasci.util;


/**
 *
 * @author sasa.hrubec
 */
public class StringUtil {
    
    public static String capitalize(String fieldName) {
         String prvoSlovo=String.valueOf(fieldName.charAt(0)); 
         prvoSlovo=prvoSlovo.toUpperCase();
         fieldName=fieldName.substring(1,fieldName.length());
         fieldName=prvoSlovo+fieldName;
         return fieldName;
    }
    
    
    public static String decapitalize(String fieldName) {
        String prvoSlovo=String.valueOf(fieldName.charAt(0)); 
        prvoSlovo=prvoSlovo.toLowerCase();
        fieldName=fieldName.substring(1,fieldName.length());
        fieldName=prvoSlovo+fieldName;
        return fieldName;
   }
    
    public static String vodeceNule(int i,int length) {
    	if(i<0) i*=-1;
    	    String tmp=String.valueOf(i);
    	    if(tmp.length()>length)
    		return tmp.substring(0,length);
    	    String tmp1="";
    	    for(int j=0;j<(length-tmp.length());j++)
    		tmp1+="0";
    	    return tmp1+tmp;
    	}
}
