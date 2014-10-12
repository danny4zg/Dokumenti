package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.OrganizationBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.LazyDataModel;

@ManagedBean(name="addCertVetOrg")
@ViewScoped
public class AddCertVetOrganizationFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Certifikat> lazyModel;
	private Certifikat[] selectedCertifikati;
	private Integer idOrganization;
	private String selectedCertifikatTxt = Const.BLANK;
	private boolean disableAdd = false;
	
	public AddCertVetOrganizationFacade(){
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();
			
			FacesContext context = FacesContext.getCurrentInstance();
			this.idOrganization = ((OrganizationBean) context.getExternalContext().getSessionMap().get("organization")).getIdOrganization();
		
			lazyModel = new LazyCertificatesDataModel(2);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(ActionEvent event) {         	
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		if (this.disableAdd){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Spremanje podataka je u tijeku..."));
			
			return;
		}
		
		
		if (selectedCertifikati.length > 0) {
			
			this.disableAdd = true;
			
			try{
				Integer idCertOrg = null;
				
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				
				for (Certifikat c : selectedCertifikati){
					parm.clear();
					rs = trx.executePreparedQueryById("test", "GetSequenceCertOrg", parm);
					
					if (rs.next()){
						idCertOrg = rs.getInt(1);
					}
					
					parm.clear();
					parm.add(idCertOrg);
					parm.add(c.getIdCertifikat());
					parm.add(getIdOrganization());
					
					trx.executePreparedUpdateById("test", "InsertCertOrg", parm);
					trx.commit();
				}
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Certifikati su uspješno dodani organizaciji."));
				
				this.disableAdd = false;
				
				setSelectedCertifikati(new Certifikat[]{});
				setSelectedCertifikatTxt(Const.BLANK);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			finally {
				if (rs != null){
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (trx != null)
					try {
						trx.close();
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		 	
	}
	
	public void selectCode(ActionEvent event){
		String tmp = "";
		
		for (Certifikat p : selectedCertifikati){
			
			tmp += p.getKodCertifikat() + ", ";
		}
		tmp = tmp.substring(0, tmp.lastIndexOf(","));
		setSelectedCertifikatTxt(tmp);
	}
	
	public void cancel(){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_vet_organizations?faces-redirect=true");	 	
	}

	public LazyDataModel<Certifikat> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Certifikat> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public Certifikat[] getSelectedCertifikati() {
		return selectedCertifikati;
	}

	public void setSelectedCertifikati(Certifikat[] selectedCertifikati) {
		this.selectedCertifikati = selectedCertifikati;
	}

	public Integer getIdOrganization() {
		return idOrganization;
	}

	public void setIdOrganization(Integer idOrganization) {
		this.idOrganization = idOrganization;
	}

	public String getSelectedCertifikatTxt() {
		return selectedCertifikatTxt;
	}

	public void setSelectedCertifikatTxt(String selectedCertifikatTxt) {
		this.selectedCertifikatTxt = selectedCertifikatTxt;
	}

	public boolean isDisableAdd() {
		return disableAdd;
	}

	public void setDisableAdd(boolean disableAdd) {
		this.disableAdd = disableAdd;
	}

}
