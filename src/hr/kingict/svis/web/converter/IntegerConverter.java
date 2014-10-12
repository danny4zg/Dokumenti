/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.kingict.svis.web.converter;

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
public class IntegerConverter implements Converter {

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {

    	if (value == null || (value != null && value.isEmpty())) return null;
    	String trimmedValue = value.trim();

        try {
            return Integer.parseInt(trimmedValue);
        }
        catch (NumberFormatException ex) {
        	
        	FacesMessage msg = new FacesMessage("fieldMsgs_"+arg1.getClientId(arg0),"Pogrešan unos broja: " + (String)value);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);			
			throw new ConverterException(msg);
        		
        }

  
        //postMessage(arg0, arg1);
        //return null;

    }

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        if (arg2 == null) {
            return "";
        }
        try {
            Integer number = (Integer) arg2;
            return number.toString();
        } catch (Exception ex) {
        	Logger.getLogger(getClass().getName()).log(Level.FINEST, "exception caught", ex);
            return "";
        }
    }

    private void postMessage(FacesContext fc, UIComponent comp) {
        FacesContext.getCurrentInstance().addMessage(comp.getClientId(fc),
                new FacesMessage(FacesMessage.SEVERITY_ERROR,null,"Uneseni broj nije ispravan!"));
    }
}
