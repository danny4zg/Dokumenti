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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

@ManagedBean(name="editOrdReturn")
@ViewScoped
public class EditOrderReturnFacade implements Serializable {
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
	
	private String serijaOd;
	private String serijaDo;
	private Date datum;
	private String notes;
	
	private String min;
	private String max;
	
	private int step;
	private UserBean ub;
	
	private Integer delItem;
	
	private Integer status;
	private int rola;
	
	public EditOrderReturnFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 5 && rola != 2 && rola != 12 && rola != 2 && rola != 1226 && rola != 1228){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				this.datum = new Date();
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				Calendar prije = Calendar.getInstance();
				Calendar poslije = Calendar.getInstance();
				
				int day = prije.get(Calendar.DATE);
		        int month = prije.get(Calendar.MONTH);
		        int year = prije.get(Calendar.YEAR);
		        
		        prije.set(year, month-1, day);
		        //poslije.set(year, month+1, day);
		        
		        System.out.println(sdf.format(prije.getTime()));
		        System.out.println(sdf.format(poslije.getTime()));
		        
				this.min=sdf.format(prije.getTime());
				this.max=sdf.format(poslije.getTime());
				
				selNarucitelj = new OrganizationBean();
				
		    	ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
	    		
	    		trx = ctxA.getDatabaseManager().createTransaction("sys");
		    	parm.add(ub.getIdOrganizacije());
		    	
		    	if (rola == Const.ROLA_OBJEKT.intValue()){
	    			rs =  trx.executePreparedQueryById("test", "SelectNaruciteljObjekt", parm);
		    	}
		    	else { 
	    			rs =  trx.executePreparedQueryById("test", "SelectNaruciteljOrg", parm);
		    	}
		    	
				while (rs.next()){
					selNarucitelj.setIdOrganization(rs.getInt(1));
					selNarucitelj.setName(rs.getString(2));
					selNarucitelj.setTel(rs.getString(5));
					selNarucitelj.setFax(rs.getString(6));
					selNarucitelj.setEmail(rs.getString(7));
					selNarucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
					selNarucitelj.setOib(rs.getString(9));
				}

				rs.close();
				
				if (rola == Const.ROLA_OBJEKT.intValue()){
	    			rs =  trx.executePreparedQueryById("test", "OrderReturnObjekt", parm);
		    	}
		    	else { 
	    			rs =  trx.executePreparedQueryById("test", "OrderReturnOrg", parm);
		    	}
				
				while (rs.next()){
					Assign2Bean ab = new Assign2Bean();
					System.out.println("stavka: " + rs.getInt(1));
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
					System.out.println(rs.getInt(1) + " - " + rs.getString(5));
					
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
			a1.setDatum(datum);
			a1.setNotes(notes);
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
		
		Integer idNarudzba = -1;
		
		try {
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		Integer seq = null;
    		
    		if (assignList.size() == 0){
    			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Nema dokumenata za potvrdu",  "");    
    			FacesContext.getCurrentInstance().addMessage(null, message);  
    			return;
    		}
    		
    		for (Assign4Bean a : assignList){
    			
    			parm.clear();
    			rs =  trx.executePreparedQueryById("test", "GetSequenceReturnItem", parm);
    			if (rs.next()) seq = rs.getInt(1);
    			
    			parm.clear();
    			parm.add(seq);
    			parm.add(a.getStavka().getIdNarudzbe());
    			parm.add(a.getStavka().getIdStavkaNarudzbe());
    			parm.add(a.getStavka().getDoc().getIdDocument());
    			parm.add(a.getStavka().getKolicina());
    			parm.add(a.getSerijaOd());
    			parm.add(a.getSerijaDo());
    			
    			java.util.Calendar cal1 = Calendar.getInstance();
    			cal1.setTime(a.getDatum());
    			java.sql.Date d1 = new java.sql.Date(cal1.getTime().getTime());
    			parm.add(d1);
    			parm.add(a.getNotes());
    			
    			trx.executePreparedUpdateById("test", "InsertReturnItem", parm);
    			trx.commit();
    		
    			parm.clear();
    			parm.add(Const.VRACENO);
    			parm.add(a.getStavka().getIdStavkaNarudzbe());
    			trx.executePreparedUpdateById("test", "ChangeStatusItem", parm);
    			trx.commit();
    			
    			if (idNarudzba.intValue() != a.getStavka().getIdNarudzbe().intValue()){	
    				idNarudzba = a.getStavka().getIdNarudzbe();
	    			parm.clear();
					parm.add(Const.VRACENO);
					parm.add(idNarudzba);
					trx.executePreparedUpdateById("test", "UpdateOrderStatus", parm);
					trx.commit();
				}
				
    		}
    		
	        step = 2;
			
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
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void checkConfirmStatus(ActionEvent actionEvent){  
		
		try {
			setStatus(1);
			
			confirm();
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Potvrdili ste povrat dokumenata",  "");    
			FacesContext.getCurrentInstance().addMessage(null, message);  
		}
		catch (Exception e){
			e.printStackTrace();
		}
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

	public String getSerijaOd() {
		return serijaOd;
	}

	public void setSerijaOd(String serijaOd) {
		this.serijaOd = serijaOd;
	}

	public String getSerijaDo() {
		return serijaDo;
	}

	public void setSerijaDo(String serijaDo) {
		this.serijaDo = serijaDo;
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

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}
	
}
