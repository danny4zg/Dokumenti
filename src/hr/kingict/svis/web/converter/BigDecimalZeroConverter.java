/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.kingict.svis.web.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;



/**
 *
 * @author hr1uz0f5
 */
public class BigDecimalZeroConverter implements Converter {

	@Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {

    	if (value == null || (value != null && value.isEmpty())) return null;

    	String trimmedValue = value.trim();

    	BigDecimal bd=null;
        try {
        	trimmedValue = trimmedValue.replace(".", "");
        	trimmedValue = trimmedValue.replace(",", ".");
            bd= new BigDecimal(trimmedValue);
        }
        catch (NumberFormatException ex) {}
        
        if (bd != null) {
        	return bd;
        }
        

        FacesMessage msg = new FacesMessage("fieldMsgs_"+arg1.getClientId(arg0),"Pogrešan unos broja: " + (String)value);
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);			
		throw new ConverterException(msg);

    }

	@Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {

        if (arg2 == null) {
            return "";
        }
        try {
            Locale locale = new Locale("hr", "HR");
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
            DecimalFormat df = (DecimalFormat) nf;
            BigDecimal number = (BigDecimal) arg2;

            StringBuilder pattern = new StringBuilder();
            pattern.append("#,##0.00");
            int scale = number.scale();
            for (int cnt = 2; cnt < scale; cnt++) {
                pattern.append('0');
            }
            df.applyPattern(pattern.toString());
            return df.format(arg2);
        } catch (Exception ex) {
        	Logger.getLogger(getClass().getName()).log(Level.FINEST, "exception caught", ex);
            return "";
        }
    }


}
