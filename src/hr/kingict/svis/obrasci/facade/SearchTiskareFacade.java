package hr.kingict.svis.obrasci.facade;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Org4Doc;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="dialogSearchTiskareBean")
public class SearchTiskareFacade {
	private static final long serialVersionUID = 1L;
	private LazyDataModel<Org4Doc> lazyModel;
	
	public SearchTiskareFacade(){
		lazyModel = new LazyOrg4DocDataModel(Const.TYPE_TISKARA);
	}

	public LazyDataModel<Org4Doc> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Org4Doc> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
}

