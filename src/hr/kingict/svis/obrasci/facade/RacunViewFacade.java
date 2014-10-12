package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.Racun;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ViewScoped
@ManagedBean(name="racunViewBean")
public class RacunViewFacade {
	protected IApplicationContext ctxA = null;
	private Racun racun;
	private Integer selected;
	//search
	private String subjekt;
	private String objekt;

	private BigDecimal iznos;
	private BigDecimal uplaceniIznos;
	private Integer idStatusPlacanja;
	private String statusPlacanja;
	private Date datumPlacanja;
	private String ira;
	
	private int rola;

	public RacunViewFacade(){
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
		    rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 1227){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else{
				this.ctxA = ApplicationContextFactory.getCurrentContext();
				
				racun = (Racun) context.getExternalContext().getSessionMap().get("rac");
				
				setIznos(racun.getIznos());
				setUplaceniIznos(racun.getUplaceniIznos());
				setIra(racun.getIra());
				setDatumPlacanja(racun.getDatumPlacanja());
				setIdStatusPlacanja(racun.getIdStatusPlacanja());
				setStatusPlacanja(racun.getNazivStatusPlacanja());	
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void update(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			if (uplaceniIznos.compareTo(iznos) == 0){
				setIdStatusPlacanja(new Integer(3));
				setStatusPlacanja("Plaćeno");
			}
			else if (uplaceniIznos.compareTo(iznos) > 0){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Plaćeni iznos ne može biti veći od iznosa računa.",  "User error");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
		        
		        return;
			}
			else if (uplaceniIznos == null || (uplaceniIznos.compareTo(BigDecimal.ZERO) == 0)){
				setIdStatusPlacanja(new Integer(2));
				setStatusPlacanja("Djelomično plaćeno");
			}
			
			parm.add(new java.sql.Date(datumPlacanja.getTime()));
			parm.add(uplaceniIznos);
			parm.add(ira);
			parm.add(idStatusPlacanja);
			parm.add(racun.getIdRacun());
			
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			trx.executePreparedUpdateById("test", "UpdateRacunCert", parm);
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
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	public void cancel(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		String returnPage = null;
		
		try {
			
			returnPage = ((String) context.getExternalContext().getSessionMap().get("return"));
			
			if (returnPage != null && !returnPage.equals("")){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, returnPage);
			}
			else {
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "obracun?faces-redirect=true");
			}
		} 
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public Racun getRacun() {
		return racun;
	}

	public void setRacun(Racun racun) {
		this.racun = racun;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public String getSubjekt() {
		return subjekt;
	}

	public void setSubjekt(String subjekt) {
		this.subjekt = subjekt;
	}

	public String getObjekt() {
		return objekt;
	}

	public void setObjekt(String objekt) {
		this.objekt = objekt;
	}

	public BigDecimal getUplaceniIznos() {
		return uplaceniIznos;
	}

	public void setUplaceniIznos(BigDecimal uplaceniIznos) {
		this.uplaceniIznos = uplaceniIznos;
	}

	public Integer getIdStatusPlacanja() {
		return idStatusPlacanja;
	}

	public void setIdStatusPlacanja(Integer idStatusPlacanja) {
		this.idStatusPlacanja = idStatusPlacanja;
	}

	public String getStatusPlacanja() {
		return statusPlacanja;
	}

	public void setStatusPlacanja(String statusPlacanja) {
		this.statusPlacanja = statusPlacanja;
	}

	public Date getDatumPlacanja() {
		return datumPlacanja;
	}

	public void setDatumPlacanja(Date datumPlacanja) {
		this.datumPlacanja = datumPlacanja;
	}

	public String getIra() {
		return ira;
	}

	public void setIra(String ira) {
		this.ira = ira;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public BigDecimal getIznos() {
		return iznos;
	}

	public void setIznos(BigDecimal iznos) {
		this.iznos = iznos;
	}
	
}
