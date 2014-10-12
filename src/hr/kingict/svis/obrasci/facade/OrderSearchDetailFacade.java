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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name="ordSearchDetail")
@ViewScoped
public class OrderSearchDetailFacade implements Serializable {
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
	
	public OrderSearchDetailFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			selNarucitelj = new OrganizationBean();
			selDobavljac = new OrganizationBean();
    		
			FacesContext context = FacesContext.getCurrentInstance();
			
			db = (Assign2Bean) context.getExternalContext().getSessionMap().get("searchord");
			
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			
    		parm.add(db.getIdNarucitelj());
			if (db.getOrgType().intValue() == Const.CODE_OBJEKT.intValue())
    			rs =  trx.executePreparedQueryById("test", "SelectNaruciteljObjekt", parm);
    		else 
    			rs =  trx.executePreparedQueryById("test", "SelectNaruciteljOrg", parm);
    		
			while (rs.next()){
				selNarucitelj.setIdOrganization(rs.getInt(1));
				selNarucitelj.setName(rs.getString(2));
				selNarucitelj.setTel(rs.getString(5));
				selNarucitelj.setFax(rs.getString(6));
				selNarucitelj.setEmail(rs.getString(7));
				selNarucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
				selNarucitelj.setOib(rs.getString(9));
			}
			
			parm.clear();
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "GetPersonData", parm);
			
			if (rs.next()){
				vetKorisnik = rs.getString(2);
			}
			
			parm.clear();
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "SelectAllFromOrder", parm);
			
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
				doc.setPrice(rs.getBigDecimal(18));
				ab.setDoc(doc);
				
				ab.setKolicina(rs.getInt(7));
				ab.setCijena(rs.getBigDecimal(8));
				ab.setIdStatus(rs.getInt(9));
				ab.setStatus(rs.getString(10));
				ab.setIdNarucitelj(rs.getInt(11));
				ab.setNarucitelj(rs.getString(14));
				
				//dobavljac
				ab.setIdDobavljac(rs.getInt(16));
				ab.setDobavljac(rs.getString(17));
				
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
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_search?faces-redirect=true");
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
