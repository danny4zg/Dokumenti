/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.kingict.svis.web.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author sasa.hrubec
 */
public class DateConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        
        
        SimpleDateFormat sdf= new SimpleDateFormat("dd.MM.yyyy");
        Date date=null;
        try {
            date=sdf.parse(value);
        } catch (ParseException ex) {
            Logger.getLogger(DateConverter.class.getName()).log(Level.SEVERE, null, ex);
  
            FacesMessage msg = new FacesMessage("fieldMsgs_"+component.getClientId(context),"Uneseni datum nije ispravan");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);			
			throw new ConverterException(msg);
        }
    
    	return date;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) return "";
        Date d = (Date)value;
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        return df.format(d);
    }
    

    
}
