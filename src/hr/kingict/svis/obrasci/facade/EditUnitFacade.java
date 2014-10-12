package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.web.UnitBean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name="editUnitSif")
@ViewScoped
public class EditUnitFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;
	
	//search params
	private String naziv;
	
	private boolean edit;
	UnitBean db;
	
	public EditUnitFacade(){
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			db = (UnitBean) context.getExternalContext().getSessionMap().get("unitSif");
			
			if (db == null) {
				edit = false;
			}
			else {
				edit = true;
				setSelected(db.getIdUnit());
				setNaziv(db.getDesc());
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public String checkInput(String parm){
		String pattern = "(%?)(\\wČčĆćŽžŠšĐđ)*[\\s(\\wČčĆćŽžŠšĐđ)]*";
		
		try {
			if (parm.matches(pattern))
				return parm;
			else 
				return "";
		} 
		catch (Exception e){
			e.printStackTrace();
		} 
		
		return "";
	}

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "units?faces-redirect=true");
	}
	
	public void add(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init	
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		
    		if (!edit){
				rs =  trx.executePreparedQueryById("test", "GetSequenceUnitSif", parm);
				Integer idUnitSif = null;
	
				if (rs.next()){
					idUnitSif = rs.getInt(1);
				}
				
				parm.add(idUnitSif);
				parm.add(getNaziv());

				trx.executePreparedUpdateById("test", "InsertUnitSif", parm);
			}
    		else {
				parm.add(getNaziv());
				parm.add(getSelected());
				
				trx.executePreparedUpdateById("test", "EditUnitSif", parm);
    		}
			trx.commit();
				
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


	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public UnitBean getDb() {
		return db;
	}

	public void setDb(UnitBean db) {
		this.db = db;
	}
	
}
