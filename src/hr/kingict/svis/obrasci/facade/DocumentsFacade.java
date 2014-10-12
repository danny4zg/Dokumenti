package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.DocumentBean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name="docSif")
@ViewScoped
public class DocumentsFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	private Integer selected;
	
	//search params
	private String kod;
	private String naziv;
	private String mjera;
	private boolean status;
	private int rola;
	
	public DocumentsFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 1226 && rola != 1012){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				this.status = true;
				
				//FacesContext context = FacesContext.getCurrentInstance();
	    		
	    		trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "DokumentiSifrarnik", parm);
					
				while (rs.next()){
					DocumentBean doc = new DocumentBean();
					doc.setIdDocument(rs.getInt(1));
					doc.setCode(rs.getString(2));
					doc.setDesc(rs.getString(3));
					doc.setIdUnit(rs.getInt(4));
					doc.setUnit(rs.getString(5));
					doc.setPrice(rs.getBigDecimal(6));
					doc.setStatus(rs.getString(7));
					docList.add(doc);
				}
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

	public void clear(ActionEvent event){
		setNaziv("");
		setKod("");
		setMjera("");
		setStatus(true);
	}
	
	public void add(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().remove("docSif");
		
		context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_document?faces-redirect=true");
	}
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSelected());
	}
	
	public void search(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			String naziv = (String) event.getComponent().getAttributes().get("naziv");
			String kod = (String) event.getComponent().getAttributes().get("kod");
			String mjera = (String) event.getComponent().getAttributes().get("mjera");
			String activity = this.status == true ? "1" : "0";

			trx = ctxA.getDatabaseManager().createTransaction("sys");
			parm.add(activity);
			parm.add(SearchUtil.testIfContainsMode(kod.toUpperCase()));
			parm.add(SearchUtil.testIfContainsMode(naziv.toUpperCase()));
			parm.add(SearchUtil.testIfContainsMode(mjera.toUpperCase()));
			
			rs =  trx.executePreparedQueryById("test", "SearchDokumentiSifrarnik", parm);
			
			docList.clear();
			
			while (rs.next()){
				DocumentBean doc = new DocumentBean();
				doc.setIdDocument(rs.getInt(1));
				doc.setCode(rs.getString(2));
				doc.setDesc(rs.getString(3));
				doc.setIdUnit(rs.getInt(4));
				doc.setUnit(rs.getString(5));
				doc.setPrice(rs.getBigDecimal(6));
				doc.setStatus(rs.getString(7));
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

	public String getDetail(){  
	 	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (DocumentBean o : docList){
				if (o.getIdDocument().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("docSif", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_document?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void delete(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		try {
			for (int i=0; i<docList.size(); i++){
				//System.out.println(docList.get(i).getIdDocument());
				if (docList.get(i).getIdDocument().intValue() == getSelected().intValue()){
					docList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteDoc", parm);
					trx.commit();
					
					break;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		if (trx != null)
			try {
				trx.close();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}
	
	public void handleClose() {
		System.out.println("test row: ");
	}
	
	public ArrayList<DocumentBean> getDocList() {
		return docList;
	}

	public void setDocList(ArrayList<DocumentBean> docList) {
		this.docList = docList;
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

	public String getMjera() {
		return mjera;
	}

	public void setMjera(String mjera) {
		this.mjera = mjera;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

}
