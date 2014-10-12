package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Assign2Bean;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

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

@ManagedBean(name="editOrdDeliv")
@ViewScoped
public class EditOrderDeliveryFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;
	
	private ArrayList<Assign2Bean> currList = new ArrayList<Assign2Bean>();
	private Assign2Bean[] selectedDocs;
	
	private Assign2Bean db;

	private OrganizationBean selNarucitelj;
	private OrganizationBean selDobavljac;
	
	private String vetKorisnik;
	
	private int step;
	
	public EditOrderDeliveryFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			selNarucitelj = new OrganizationBean();
			selDobavljac = new OrganizationBean();

			FacesContext context = FacesContext.getCurrentInstance();
    		
	    	UserBean ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
	    	
			db = (Assign2Bean) context.getExternalContext().getSessionMap().get("deliord");
			
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		parm.add(db.getIdNarucitelj());
			rs =  trx.executePreparedQueryById("test", "SelectNarucitelj", parm);
			
			while (rs.next()){
				selNarucitelj.setIdOrganization(rs.getInt(1));
				selNarucitelj.setName(rs.getString(3));
				selNarucitelj.setTel(rs.getString(6));
				selNarucitelj.setFax(rs.getString(7));
				selNarucitelj.setEmail(rs.getString(8));
				selNarucitelj.setAddress(rs.getString(4) + ", " + rs.getString(5));
				selNarucitelj.setOib(rs.getString(12));
			}
			
			parm.clear();
			parm.add(ub.getIdOrganizacije());
			rs =  trx.executePreparedQueryById("test", "SelectNarucitelj", parm);
			
			if (rs.next()){
				System.out.println("idorg d " + rs.getInt(1));
				selDobavljac.setIdOrganization(rs.getInt(1));
				selDobavljac.setName(rs.getString(3));
				selDobavljac.setTel(rs.getString(6));
				selDobavljac.setFax(rs.getString(7));
				selDobavljac.setEmail(rs.getString(8));
				selDobavljac.setAddress(rs.getString(4) + ", " + rs.getString(5));
				selDobavljac.setOib(rs.getString(12));
			}
			
			parm.clear();
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "GetPersonData", parm);
			
			if (rs.next()){
				vetKorisnik = rs.getString(2);
			}
			
			parm.clear();
			parm.add(ub.getIdOrganizacije());//ovo čupam od prijavljenog korisnika
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "EditDeliveryOrder", parm);
			
			while (rs.next()){
				Assign2Bean ab = new Assign2Bean();
				ab.setIdStavkaNarudzbe(rs.getInt(1));
				ab.setDatumNarudzbe(rs.getDate(2));
				ab.setIdNarudzbe(rs.getInt(3));
				
				DocumentBean doc = new DocumentBean();
				doc.setIdDocument(rs.getInt(4));
				doc.setCode(rs.getString(5));
				doc.setDesc(rs.getString(6));
				doc.setIdUnit(rs.getInt(12));
				doc.setUnit(rs.getString(13));
				doc.setPrice(rs.getBigDecimal(15));
				ab.setDoc(doc);
				
				ab.setKolicina(rs.getInt(7));
				ab.setCijena(rs.getBigDecimal(8));
				ab.setIdStatus(rs.getInt(9));
				ab.setStatus(rs.getString(10));
				ab.setIdNarucitelj(rs.getInt(11));
				ab.setNarucitelj(rs.getString(14));
				currList.add(ab);
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
	

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_delivery?faces-redirect=true");
	}
	
	public void confirm(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			if (getSelectedDocs().length == 0){
				FacesContext context = FacesContext.getCurrentInstance();  
		        context.addMessage(null, new FacesMessage("Morate odabrati narudžbe spremne za slanje", ""));  
		      
				return;
			}
			
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		List<Integer> rem = new ArrayList<Integer>();
    		for (Assign2Bean a : selectedDocs){
    			parm.clear();
    			parm.add(Const.ISPORUČENO);
    			parm.add(a.getIdStavkaNarudzbe());
    			trx.executePreparedUpdateById("test", "ChangeStatusItem", parm);
    			trx.commit();
    			
    			int i = 0;
    			for (Assign2Bean b: currList){
    				
    				if (b.getIdStavkaNarudzbe().intValue() == a.getIdStavkaNarudzbe().intValue()){
    					rem.add(new Integer(i));
    					System.out.println("adding to remove list " + i);
    				}
    				
    				i++;
    			}
    		}
    		
    		for (int i=0; i<rem.size(); i++){
    			currList.remove(rem.get(i));
			}
    		
			parm.clear();
			parm.add(Const.ISPORUČENO);
			parm.add(db.getIdNarudzbe());
			trx.executePreparedUpdateById("test", "UpdateOrderStatus", parm);
			trx.commit();
			setStep(2);
			
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Narudžba je spremna za slanje!", ""));  
	        
			
		} catch (Exception e) {
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

	public Assign2Bean getDb() {
		return db;
	}

	public void setDb(Assign2Bean db) {
		this.db = db;
	}

	public OrganizationBean getSelNarucitelj() {
		return selNarucitelj;
	}

	public void setSelNarucitelj(OrganizationBean selNarucitelj) {
		this.selNarucitelj = selNarucitelj;
	}

	public OrganizationBean getSelDobavljac() {
		return selDobavljac;
	}

	public void setSelDobavljac(OrganizationBean selDobavljac) {
		this.selDobavljac = selDobavljac;
	}

	public String getVetKorisnik() {
		return vetKorisnik;
	}

	public void setVetKorisnik(String vetKorisnik) {
		this.vetKorisnik = vetKorisnik;
	}

	public ArrayList<Assign2Bean> getCurrList() {
		return currList;
	}

	public void setCurrList(ArrayList<Assign2Bean> currList) {
		this.currList = currList;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public Assign2Bean[] getSelectedDocs() {
		return selectedDocs;
	}

	public void setSelectedDocs(Assign2Bean[] selectedDocs) {
		this.selectedDocs = selectedDocs;
	}
	
	
	
}
