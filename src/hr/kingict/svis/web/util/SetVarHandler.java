package hr.kingict.svis.web.util;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.primefaces.component.datatable.DataTable;

public class SetVarHandler extends TagHandler{

	 private final TagAttribute var;

	    public SetVarHandler(TagConfig config) {
	        super(config);
	        this.var = this.getAttribute("var");
	    }
	    public void apply(FaceletContext ctx, UIComponent parent)
	            throws IOException, FacesException, FaceletException, ELException {

	        if (parent instanceof DataTable) {
	        	DataTable table = (DataTable) parent;
	            table.setVar(this.var.getValue(ctx));
	        }
	        

	        this.nextHandler.apply(ctx, parent);
	    }
}
