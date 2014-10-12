package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.svis.obrasci.web.Barcode;
import hr.kingict.svis.obrasci.web.Contingent;
import hr.kingict.svis.obrasci.web.UserBean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="editContingent")
public class EditContingentFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;

	private UserBean ub;
	private Contingent con;
	
	private LazyDataModel<Barcode> lazyModel;
	private int rola;
	private int type;
	
	public EditContingentFacade() {
		
		try {
			
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 3){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();
				
				ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
		    	con = (Contingent) context.getExternalContext().getSessionMap().get("contingent");
				lazyModel = new LazyBarcodeDataModel(con.getIdContingent());
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int currentDataPageSize(){
		int size = ((List<Barcode>) lazyModel.getWrappedData()).size();
		
		if (size > 2){
			type = 1;
		}
		else {
			type = 2;
		}
		
		return type;
	}
	
	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "contingents?faces-redirect=true");
	}

	public LazyDataModel<Barcode> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Barcode> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public Contingent getCon() {
		return con;
	}

	public void setCon(Contingent con) {
		this.con = con;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
