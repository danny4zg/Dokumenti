package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Contingent;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.DataSource;

import org.primefaces.model.LazyDataModel;

@ViewScoped
@ManagedBean(name="contingent")
public class ContingentFacade {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer tip;
	private Integer kolicina;
	private Integer multiplikator;
	private LazyDataModel<Contingent> lazyModel;
	private int rola;
	private Integer selected;
	private UserBean ub;
	
	public ContingentFacade() {
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		try {
			
			FacesContext context = FacesContext.getCurrentInstance();
		
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 3){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();
				
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "GetMultiplikator", parm);
					
				if (rs.next()){
					setMultiplikator(rs.getInt("multiply"));
				}
				
				this.ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
				
				setTip(Const.NALJEPNICE);
				lazyModel = new LazyContingentDataModel();
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
	
	public void save(ActionEvent event){
		try {
			
			if (kolicina == null){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Niste unijeli broj barkodova.",  "Niste unijeli broj barkodova.");  
	            FacesContext.getCurrentInstance().addMessage(null, message);
				
				return;
			}
			else if (kolicina % multiplikator != 0){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Količina mora biti višekratnik broja " + getMultiplikator(),  "Količina mora biti višekratnik broja " + getMultiplikator());  
	            FacesContext.getCurrentInstance().addMessage(null, message);
				
				return;
			}
			
			javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
        	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
        	
        	Connection conn = ds.getConnection(); 

        	//pVrsta IN NUMBER,
        	//pKolicina IN NUMBER,
        	//pUserID IN NUMBER,
        	//pBarkodOd OUT NUMBER,
        	//pBarkodDo OUT NUMBER
        	
        	String proc3StoredProcedure = "{ call vis_ex.insert_barcodes(?, ?, ?, ?, ?) }";
 	       
	        CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
	    	
	        cs.setInt(1, this.getTip().intValue());
	        cs.setInt(2, this.getKolicina().intValue());
	        cs.setInt(3, this.ub.getIdKorisnik().intValue());
	        cs.registerOutParameter(4, java.sql.Types.INTEGER);
	        cs.registerOutParameter(5, java.sql.Types.INTEGER);
	        cs.execute();
	        
	        Integer parm1 = cs.getInt(4);
	        Integer parm2 = cs.getInt(5);
	        
	        System.out.println("Kreiran je kontingent barkodova od: " + parm1 + " do: " + parm2);
	       
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Operacija je uspješno završena",  "Kreiran je kontingent barkodova od: " + parm1 + " do: " + parm2);  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	        conn.close();
		}
		catch (Exception e){
			e.printStackTrace();
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Došlo je do pogreške prilikom kreiranja narudžbe. REASON: " + e.getMessage());  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
		}
	}
	
	public void clear(ActionEvent event){
		setKolicina(null);
	}
	
	public String getDetail(){  
	 	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (Contingent o : (List<Contingent>) lazyModel.getWrappedData()){
				if (o.getIdContingent().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("contingent", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "contingent_detail?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public Integer getTip() {
		return tip;
	}
	
	public void setTip(Integer tip) {
		this.tip = tip;
	}
	
	public Integer getKolicina() {
		return kolicina;
	}
	
	public void setKolicina(Integer kolicina) {
		this.kolicina = kolicina;
	}

	public LazyDataModel<Contingent> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Contingent> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public Integer getMultiplikator() {
		return multiplikator;
	}

	public void setMultiplikator(Integer multiplikator) {
		this.multiplikator = multiplikator;
	}

}
