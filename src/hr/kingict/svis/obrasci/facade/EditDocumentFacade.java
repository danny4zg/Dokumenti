package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.web.DocumentBean;

import java.io.Serializable;
import java.math.BigDecimal;
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

@ManagedBean(name="editDocSif")
@ViewScoped
public class EditDocumentFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<SelectItem> mjeraList = new ArrayList<SelectItem>();
	private Integer selected;
	
	//search params
	private String kod;
	private String naziv;
	private BigDecimal cijena;
	
	private boolean edit;
	private boolean status; 
	DocumentBean db;
	
	public EditDocumentFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init

			FacesContext context = FacesContext.getCurrentInstance();
			db = (DocumentBean) context.getExternalContext().getSessionMap().get("docSif");
			
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "SelectMjera", parm);
			
			while (rs.next()){
				mjeraList.add(new SelectItem(rs.getInt(1), rs.getString(2)));
			}
			
			if (db == null) {
				edit = false;
			}
			else {
				edit = true;
				setSelected(db.getIdUnit());
				setNaziv(db.getDesc());
				setKod(db.getCode());
				setCijena(db.getPrice());
				
				if (db.getStatus().equals("1")){
					status = true;
				}
				else status = false;
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
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "documents?faces-redirect=true");
	}
	
	public void add(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			if (getKod() == null || getNaziv() == null || getCijena() == null){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Niste unijeli sve potrebne podatke.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
				
				return;
			}
			
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		
    		if (!edit){
				rs =  trx.executePreparedQueryById("test", "GetSequenceDocSif", parm);
				Integer idDocumentSif = null;
	
				if (rs.next()){
					idDocumentSif = rs.getInt(1);
				}
				
				parm.add(idDocumentSif);
				parm.add(getKod());
				parm.add(getNaziv());
				parm.add(getSelected());
				parm.add(getCijena());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				
				trx.executePreparedUpdateById("test", "InsertDocSif", parm);
			}
    		else {
    			
				parm.add(getKod());
				parm.add(getNaziv());
				parm.add(getSelected());
				parm.add(getCijena());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				parm.add(db.getIdDocument());
				
				trx.executePreparedUpdateById("test", "EditDocSif", parm);
    		}
			trx.commit();
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspješno spremljen dokument.",  "");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
				
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


	public ArrayList<SelectItem> getMjeraList() {
		return mjeraList;
	}

	public void setMjeraList(ArrayList<SelectItem> mjeraList) {
		this.mjeraList = mjeraList;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public String getKod() {
		return kod;
	}

	public void setKod(String kod) {
		this.kod = kod;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public BigDecimal getCijena() {
		return cijena;
	}

	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public DocumentBean getDb() {
		return db;
	}

	public void setDb(DocumentBean db) {
		this.db = db;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
}
