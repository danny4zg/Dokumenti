package hr.kingict.svis.obrasci.util;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.validator.Validator;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.primefaces.component.inputtext.InputText;

public class SetCustomValidatorHandler extends TagHandler{
	 
		private final TagAttribute var;

	    public SetCustomValidatorHandler(TagConfig config) {
	        super(config);
	        this.var = this.getAttribute("validator");
	    }
	    
	    public void apply(FaceletContext ctx, UIComponent parent)
	            throws IOException, FacesException, FaceletException, ELException {

	        if (parent instanceof InputText) {
	        	InputText input = (InputText) parent;
	        	
	        	String validatorName=this.var.getValue(ctx);
				try {
					 Object o = Class.forName("hr.kingict.svis.web.validator."+validatorName).newInstance();
					 input.addValidator((Validator) o);
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	    
	        }	        

	        this.nextHandler.apply(ctx, parent);
	    }
}
