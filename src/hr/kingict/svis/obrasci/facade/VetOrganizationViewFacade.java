package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.OrganizationBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.LazyDataModel;

@ManagedBean(name="vetOrgView")
@ViewScoped
public class VetOrganizationViewFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	private LazyDataModel<Certifikat> lazyModel;
	private OrganizationBean org;
	private boolean edit = false;
	private Integer selected;
	private Integer selectedCert;
	
	private int docListSize;
	private int certListSize;
	
	public VetOrganizationViewFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {		
			FacesContext context = FacesContext.getCurrentInstance();
			this.org = (OrganizationBean) context.getExternalContext().getSessionMap().get("organization");	
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
    		parm.add(getOrg().getIdOrganization());
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "SelectDokumentiVetOrg", parm);
				
			while (rs.next()){
				DocumentBean doc = new DocumentBean();
				doc.setIdDocument(rs.getInt(13));
				doc.setIdOrgDocument(rs.getInt(1));
				doc.setCode(rs.getString(2));
				doc.setDesc(rs.getString(3));
				doc.setIdState(rs.getInt(4));
				doc.setStateCode(rs.getString(5));
				doc.setState(rs.getString(6));
				doc.setIdUnit(rs.getInt(7));
				doc.setUnit(rs.getString(8));
				doc.setIdLanguage(rs.getInt(9));
				doc.setLanguage(rs.getString(10));
				doc.setJavaLangCode(rs.getString(11));
				doc.setPrice(rs.getBigDecimal(12));
				docList.add(doc);
			}

			lazyModel = new LazyCertificatesDataModel(1);
				
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}


	public void doEdit(ActionEvent event){	
		Boolean param = Boolean.parseBoolean((String) event.getComponent().getAttributes().get("edit"));
		setEdit(param);
		
		setDocListSize(docList.size());
		setCertListSize(((List<Certifikat>)lazyModel.getWrappedData()).size());	
	}
	
	public void cancel(ActionEvent event){
		Boolean param = Boolean.parseBoolean((String) event.getComponent().getAttributes().get("edit"));
		setEdit(param);	
	}
	
	public void cancel2(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();		
		context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "vet_organizations?faces-redirect=true");
	}
	
	public void setParam(){
		System.out.println("set param: " + getSelected());
		System.out.println("set param: " + getSelectedCert());
	}
	
	public void doAdd(ActionEvent event){	
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().put("docListOrg", docList);
		
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_doc_vet_organization?faces-redirect=true");		 	
	}
	
	public void doAddCert(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().put("certListOrg", docList);
		
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_cert_organization?faces-redirect=true");	
	}
	
	public void deleteActionDoc(ActionEvent event){
		List parm = new ArrayList();
		ITransaction trx = null;
		
		try {
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			
			parm.add(org.getIdOrganization());
			trx.executePreparedUpdateById("test", "RemoveAllDocOrg", parm);
			trx.commit();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public void deleteActionCert(ActionEvent event){
		List parm = new ArrayList();
		ITransaction trx = null;
		
		try {
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			
			parm.add(org.getIdOrganization());
			trx.executePreparedUpdateById("test", "RemoveAllCertOrg", parm);
			trx.commit();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public void delete(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		try {
			for (int i=0; i<docList.size(); i++){

				if (docList.get(i).getIdOrgDocument().intValue() == getSelected().intValue()){
					docList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteDocOrg", parm);
					trx.commit();
					
					break;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	public void deleteCert(){
		List parm = new ArrayList();
		ITransaction trx = null;
		
		List<Certifikat> certList = (List<Certifikat>) lazyModel.getWrappedData();
		
		try {
			for (int i=0; i<certList.size(); i++){
				if (certList.get(i).getIdCertifikat().intValue() == getSelectedCert().intValue()){
					certList.remove(i);
					
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelectedCert());
					parm.add(org.getIdOrganization());
					trx.executePreparedUpdateById("test", "DeleteOrgCertificate", parm);
					trx.commit();
					
					break;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}

	public LazyDataModel<Certifikat> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Certifikat> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	public OrganizationBean getOrg() {
		return org;
	}

	public void setOrg(OrganizationBean org) {
		this.org = org;
	}

	public ArrayList<DocumentBean> getDocList() {
		return docList;
	}

	public void setDocList(ArrayList<DocumentBean> docList) {
		this.docList = docList;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	
	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public Integer getSelectedCert() {
		return selectedCert;
	}

	public void setSelectedCert(Integer selectedCert) {
		this.selectedCert = selectedCert;
	}

	public int getDocListSize() {
		return docListSize;
	}

	public void setDocListSize(int docListSize) {
		this.docListSize = docListSize;
	}

	public int getCertListSize() {
		return certListSize;
	}

	public void setCertListSize(int certListSize) {
		this.certListSize = certListSize;
	}
	
}
