package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.Dokument;
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
import javax.faces.model.SelectItem;

@ManagedBean(name="addDocOrg")
@ViewScoped
public class AddDocOrganizationFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private List<Dokument> docList = new ArrayList<Dokument>();
	private Integer selected;
	
	public AddDocOrganizationFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
    		
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "SelectDokumenti", parm);
				
			while (rs.next()){
				docList.add(new Dokument(rs.getInt(1), rs.getString(2)));
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
	
	public void add(ActionEvent event) {  
        	
		Integer param = (Integer) event.getComponent().getAttributes().get("selected");
		
		if (check(param)){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Nije moguće dodati isti dokument!"));  
		}
		else {
			List parm = new ArrayList();
			ITransaction trx = null;
			ResultSet rs = null;
			
			try{
				Integer idDocOrg = null;
				
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs = trx.executePreparedQueryById("test", "GetSequenceDocOrg", parm);
				
				if (rs.next()){
					idDocOrg = rs.getInt(1);
				}
				
				FacesContext context = FacesContext.getCurrentInstance();
				OrganizationBean o = (OrganizationBean) context.getExternalContext().getSessionMap().get("organization");
				
				parm.add(idDocOrg);
				parm.add(o.getIdOrganization());
				parm.add(param);
				
				trx.executePreparedUpdateById("test", "InsertDocOrg", parm);
				trx.commit();
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Dokument je uspješno dodan."));
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		 	
	}
	
	public boolean check(Integer value){
		boolean exists = false;
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			List<DocumentBean> lista = (List<DocumentBean>) context.getExternalContext().getSessionMap().get("docListOrg");

			for (DocumentBean item: lista){
				System.out.println(item.getIdDocument().intValue());
				if (item.getIdDocument().intValue() == value.intValue()){ 
					exists = true;
					break;
				}
			}			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		return exists;
	}
	
	public void cancel(){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_organizations?faces-redirect=true");	 	
	}

	public List<Dokument> getDocList() {
		return docList;
	}

	public void setDocList(List<Dokument> docList) {
		this.docList = docList;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}
	
}
