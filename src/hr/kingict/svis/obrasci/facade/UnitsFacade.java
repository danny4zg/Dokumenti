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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.component.datatable.DataTable;

@ManagedBean(name="unitSif")
@ViewScoped
public class UnitsFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<UnitBean> unitList = new ArrayList<UnitBean>();
	private Integer selected;
	private int rola;
	//search params
	private String naziv;

	
	public UnitsFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
		    rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 1226 && rola != 1012){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else{
			
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				
	    		trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "SelectMjera", parm);
					
				while (rs.next()){
					UnitBean unit = new UnitBean();
					unit.setIdUnit(rs.getInt(1));
					unit.setDesc(rs.getString(2));
					unitList.add(unit);
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

	public void clear(ActionEvent event){
		setNaziv("");
	}
	
	public void add(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().remove("unitSif");
		context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_unit?faces-redirect=true");
	}
	
	public void search(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			String naziv = (String) event.getComponent().getAttributes().get("naziv");
			
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			parm.add(checkInput(naziv));

			unitList.clear();
			rs =  trx.executePreparedQueryById("test", "SearchMjera", parm);
			
			while (rs.next()){
				UnitBean unit = new UnitBean();
				unit.setIdUnit(rs.getInt(1));
				unit.setDesc(rs.getString(2));
				unitList.add(unit);
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


	public Integer getSelected() {
		return selected;
	}


	public void setSelected(Integer selected) {
		this.selected = selected;
	}
	
	public void handleClose() {
		System.out.println("test row:");
	}

	public String getDetail(){  
	 	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (UnitBean o : unitList){
				if (o.getIdUnit().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("unitSif", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_unit?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
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
			for (int i=0; i<unitList.size(); i++){
				System.out.println(unitList.get(i).getIdUnit());
				if (unitList.get(i).getIdUnit().intValue() == getSelected().intValue()){
					unitList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteUnit", parm);
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

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public ArrayList<UnitBean> getUnitList() {
		return unitList;
	}

	public void setUnitList(ArrayList<UnitBean> unitList) {
		this.unitList = unitList;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

}
