package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Assign2Bean;
import hr.kingict.svis.obrasci.web.Assign4Bean;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

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

@ManagedBean(name="editOrdAssign")
@ViewScoped
public class EditOrderAssignmentFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<SelectItem> pdocList = new ArrayList<SelectItem>();
	private Integer selected;
	
	private ArrayList<Assign2Bean> currList = new ArrayList<Assign2Bean>();
	private ArrayList<Assign4Bean> assignList = new ArrayList<Assign4Bean>();
	
	//search params
	private String kod;
	private String naziv;
	private BigDecimal cijena;
	
	private boolean edit;
	private Assign2Bean db;
	private Assign2Bean selDoc;
	
	private OrganizationBean selNarucitelj;
	private OrganizationBean selDobavljac;
	
	private String vetKorisnik;
	
	private int step;
	
	private Integer delItem;
	
	public EditOrderAssignmentFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			selNarucitelj = new OrganizationBean();
			selDobavljac = new OrganizationBean();
			
			FacesContext context = FacesContext.getCurrentInstance();
			//context.getExternalContext().getSessionMap().get("user");
    		
	    	UserBean ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
	    			
			db = (Assign2Bean) context.getExternalContext().getSessionMap().get("assiord");
			
			//System.out.println("ASSIGNING ORDER");
			//System.out.println(db.getIdNarudzbe() + " * " + db.getIdStavkaNarudzbe());
			
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
			
			//podaci za tiskaru
			parm.clear();
			parm.add(ub.getIdOrganizacije());//ovo čupam od prijavljenog korisnika
			rs =  trx.executePreparedQueryById("test", "SelectNaruciteljOrg", parm);
			
			if (rs.next()){
				System.out.println("idorg d " + rs.getInt(1));
				selDobavljac.setIdOrganization(rs.getInt(1));
				selDobavljac.setName(rs.getString(2));
				selDobavljac.setTel(rs.getString(5));
				selDobavljac.setFax(rs.getString(6));
				selDobavljac.setEmail(rs.getString(7));
				selDobavljac.setAddress(rs.getString(3) + ", " + rs.getString(4));
				selDobavljac.setOib(rs.getString(9));
			}
			
			parm.clear();
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "GetPersonData", parm);
			
			if (rs.next()){
				vetKorisnik = rs.getString(2);
			}
			
			parm.clear();
			parm.add(ub.getIdOrganizacije());
			parm.add(db.getIdNarudzbe());
			rs =  trx.executePreparedQueryById("test", "EditAssignOrder", parm);
			
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
				doc.setPrice(rs.getBigDecimal(17));
				ab.setDoc(doc);
				
				pdocList.add(new SelectItem(rs.getInt(1), rs.getString(5)));
				
				ab.setKolicina(rs.getInt(7));
				ab.setCijena(rs.getBigDecimal(8));
				ab.setIdStatus(rs.getInt(9));
				ab.setStatus(rs.getString(10));
				ab.setIdNarucitelj(rs.getInt(11));
				ab.setNarucitelj(rs.getString(14));
				ab.setSerijskiBrojOd(rs.getString(15));
				ab.setSerijskiBrojDo(rs.getString(16));
				currList.add(ab);
			}
			Integer initDoc = null;
			
			if (pdocList.size() == 0) {
				step = 1;
			}
			else {
				
				initDoc = (Integer) pdocList.get(0).getValue();
			
				for (Assign2Bean doc : currList){
					if (doc.getIdStavkaNarudzbe().intValue() == initDoc.intValue()){
						this.selected = doc.getIdStavkaNarudzbe();
						this.selDoc = doc;
						break;
					}
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
	
	public void docValueChange(){
		
		for (Assign2Bean as : currList){
			if (as.getIdStavkaNarudzbe().intValue() == this.selected.intValue()){
				this.selDoc = as;
				break;
			}
		}
	}

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_assignment?faces-redirect=true");
	}
		
	
	public void add(ActionEvent event){

		try {
			Assign4Bean a1 = new Assign4Bean();
			int b=0;
			for (Assign2Bean as : currList){
				if (as.getIdStavkaNarudzbe().intValue() == this.selected.intValue()){
					a1.setStavka(as);
					currList.remove(b);
					break;
				}
				b++;
			}
			System.out.println("UPDATING STAVKA " + a1.getStavka().getIdStavkaNarudzbe());
			
			//a1.setStavka(selDoc);
			a1.setSerijaOd(a1.getStavka().getSerijskiBrojOd());
			a1.setSerijaDo(a1.getStavka().getSerijskiBrojDo());
			assignList.add(a1);
			
			for (int i=0; i<pdocList.size();i++){
				if (((Integer) pdocList.get(i).getValue()).intValue() == this.selected.intValue()){
					pdocList.remove(i);
					
					if (pdocList.size() == 0) {
						pdocList.clear();
						step = 1;
					}
					else {
						this.selDoc = currList.get(0);
						this.selected = currList.get(0).getIdStavkaNarudzbe();
					}
					break;
				}
			}

			System.out.println("ALL ASSIGNED: " + step);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void confirm(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		
    		if (assignList.size() == 0){
    			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Nema dokumenata za potvrdu",  "");    
    			FacesContext.getCurrentInstance().addMessage(null, message);  
    			return;
    		}
    		
    		for (Assign4Bean a : assignList){
    			parm.clear();
    			//parm.add(a.getSerijaOd());
    			//parm.add(a.getSerijaDo());
    			parm.add(a.getStavka().getIdStavkaNarudzbe());
    			trx.executePreparedUpdateById("test", "AssignOrderItem", parm);
    			trx.commit();
    		}
    		
			parm.clear();
			parm.add(Const.DODIJELJENO);
			parm.add(db.getIdNarudzbe());
			trx.executePreparedUpdateById("test", "UpdateOrderStatus", parm);
			trx.commit();
			
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Narudžba je dodijeljena!", ""));  
	        
	        step = 2;
			
		} catch (DatabaseException e) {
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

	public void delete(){

		try {
			for (int i=0; i<assignList.size(); i++){
				
				if (assignList.get(i).getStavka().getIdStavkaNarudzbe().intValue() == getDelItem().intValue()){
					Assign4Bean b = assignList.get(i); 
					assignList.remove(i);
					currList.add(b.getStavka());
					pdocList.add(new SelectItem(b.getStavka().getIdStavkaNarudzbe(), b.getStavka().getDoc().getCode()));
					this.selDoc = currList.get(0);
					this.selected = currList.get(0).getIdStavkaNarudzbe();
					break;
				}
			}
			if (assignList.size() > 0) step = 0;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void cancel(){

		try {
			Assign4Bean b = null;
			boolean isFirst = true;
			
			for (int i=0; i<assignList.size(); i++){
				b = assignList.get(i); 
				currList.add(b.getStavka());
			}
			
			assignList.clear();
			pdocList.clear();
			
			for (Assign2Bean doc : currList){
				pdocList.add(new SelectItem(doc.getIdStavkaNarudzbe(), doc.getDoc().getCode()));
				if (isFirst){
					this.selected = doc.getIdStavkaNarudzbe();
					this.selDoc = doc;
					isFirst = false;
				}
			}
			
			setStep(0);
			
			FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_assignment?faces-redirect=true");
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getDelItem());
	}

	public ArrayList<SelectItem> getPdocList() {
		return pdocList;
	}

	public void setPdocList(ArrayList<SelectItem> pdocList) {
		this.pdocList = pdocList;
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

	public ArrayList<Assign4Bean> getAssignList() {
		return assignList;
	}

	public void setAssignList(ArrayList<Assign4Bean> assignList) {
		this.assignList = assignList;
	}

	public Assign2Bean getSelDoc() {
		return selDoc;
	}

	public void setSelDoc(Assign2Bean selDoc) {
		this.selDoc = selDoc;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public Integer getDelItem() {
		return delItem;
	}

	public void setDelItem(Integer delItem) {
		this.delItem = delItem;
	}

	
}
