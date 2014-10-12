package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.svis.obrasci.web.Farma;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="farma")
public class FarmaFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Farma> lazyModel;
	
	public FarmaFacade(){
		lazyModel = new LazyFarmaDataModel();
	}

	public LazyDataModel<Farma> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Farma> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
}
