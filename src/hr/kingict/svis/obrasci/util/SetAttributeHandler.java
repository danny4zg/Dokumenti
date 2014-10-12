package hr.kingict.svis.obrasci.util;

import java.io.IOException;

import javax.ccpp.Attribute;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

public class SetAttributeHandler extends TagHandler{

	private final TagAttribute var;

    public SetAttributeHandler(TagConfig config) {
        super(config);
        this.var = this.getAttribute("klasa");
    }
    
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {

        if (parent instanceof Attribute) {
        	Attribute  input = (Attribute) parent;
        	
        	String validatorName=this.var.getValue(ctx);
			try {
				 Object o = Class.forName("hr.kingict.svis.web.validator."+validatorName).newInstance();
				// input.getComponent()
			} catch (Exception e) {
				e.printStackTrace();
			}
        	    
        }	        

        this.nextHandler.apply(ctx, parent);
    }
}
