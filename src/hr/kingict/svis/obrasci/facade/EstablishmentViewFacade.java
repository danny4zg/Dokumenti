package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.ObjektExt;
import hr.kingict.svis.obrasci.web.OrganizationBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name="estView")
@ViewScoped
public class EstablishmentViewFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	private ObjektExt est;
	private boolean edit = false;
	private Integer selected;
	
	public EstablishmentViewFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			FacesContext context = FacesContext.getCurrentInstance();
			this.est = (ObjektExt) context.getExternalContext().getSessionMap().get("est");
			
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
    		parm.add(getEst().getIdObjekt());
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "SelectDokumentiObjekt", parm);
				
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
	}
	
	public void cancel(ActionEvent event){
		Boolean param = Boolean.parseBoolean((String) event.getComponent().getAttributes().get("edit"));
		setEdit(param);
	}
	
	public void cancel2(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();		
		context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "establishments?faces-redirect=true");
	}
	
	public void doAdd(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().put("docListEst", docList);
		
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_doc_establishment?faces-redirect=true"); 	
	}
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSelected());
	}
	
	public void delete(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		try {
			for (int i=0; i<docList.size(); i++){
				System.out.println(docList.get(i).getIdOrgDocument());
				if (docList.get(i).getIdOrgDocument().intValue() == getSelected().intValue()){
					docList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteDocEst", parm);
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

	public ObjektExt getEst() {
		return est;
	}

	public void setEst(ObjektExt est) {
		this.est = est;
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
}
