package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.LazyDataModel;

@ManagedBean(name="org")
@ViewScoped
public class OrganizationFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<OrganizationBean> lazyModel;
	private Integer selected;
	private OrganizationBean selOrg;
	
	//search params
	private String naziv;
	private String adresa;
	
	private int rola;
	private UserBean ub;
	private Boolean search;
	
	public OrganizationFacade(){
		FacesContext context = FacesContext.getCurrentInstance();
		
		rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();

		if (rola != 1226 && rola != 1012){
			context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
		}
		else {
			this.ctxA = ApplicationContextFactory.getCurrentContext();
			
	    	UserBean ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
			this.ub = ub;
			
			lazyModel = new LazyPrintOrganizationDataModel();
		}
	}

	public void clear(ActionEvent event){
		setNaziv("");
		setAdresa("");
	}
	
	
	public void search(ActionEvent event){
		try {
			setSearch(true);
		
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("naziv", SearchUtil.testIfContainsMode(getNaziv()));
			map.put("adresa", SearchUtil.testIfContainsMode(getAdresa()));
			
			lazyModel = new LazyPrintOrganizationDataModel(map);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}
	
	public void handleClose() {
		System.out.println("test row: " + selOrg.getIdOrganization() + " - " + selOrg.getName());
	}

	public String getDetail(){  
	 	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (OrganizationBean o : (List<OrganizationBean>) lazyModel.getWrappedData()){
				if (o.getIdOrganization().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("organization", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_organizations?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getAdresa() {
		return adresa;
	}

	public void setAdresa(String adresa) {
		this.adresa = adresa;
	}

	public OrganizationBean getSelOrg() {
		return selOrg;
	}

	public void setSelOrg(OrganizationBean selOrg) {
		this.selOrg = selOrg;
	}

	public LazyDataModel<OrganizationBean> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<OrganizationBean> lazyModel) {
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

	public Boolean getSearch() {
		return search;
	}

	public void setSearch(Boolean search) {
		this.search = search;
	}
	
}
