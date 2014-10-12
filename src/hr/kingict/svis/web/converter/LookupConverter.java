package hr.kingict.svis.web.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class LookupConverter implements Converter{

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {

		System.out.println("Lookup converter value: " + value);
    	if (value == null || (value != null && value.isEmpty())) {
    		
    		FacesMessage msg = new FacesMessage("fieldMsgs_"+arg1.getClientId(arg0),"Obavezan unos");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);			
			throw new ConverterException(msg);
    	}
    	
    	return value;

    }

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
    	
    	System.out.println("ARG 2: " + arg2);
    	
        if (arg2 == null) {
            return "";
        }
        return arg2.toString();
    }

}
