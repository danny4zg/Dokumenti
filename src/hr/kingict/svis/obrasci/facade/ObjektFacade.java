package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.svis.obrasci.web.Objekt;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="objekt")
public class ObjektFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Objekt> lazyModel;
	
	public ObjektFacade(){
		lazyModel = new LazyObjektDataModel();
	}

	public LazyDataModel<Objekt> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Objekt> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
}
