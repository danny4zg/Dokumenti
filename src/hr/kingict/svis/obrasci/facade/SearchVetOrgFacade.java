package hr.kingict.svis.obrasci.facade;

import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Org4Doc;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="dialogSearchVetOrgBean")
public class SearchVetOrgFacade {
	private static final long serialVersionUID = 1L;
	private LazyDataModel<Org4Doc> lazyModel;
	
	public SearchVetOrgFacade(){
		lazyModel = new LazyOrg4DocDataModel(Const.TYPE_VET_ORG);
	}

	public LazyDataModel<Org4Doc> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Org4Doc> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
}