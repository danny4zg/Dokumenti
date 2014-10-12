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
import hr.kingict.svis.obrasci.web.Dokument;
import hr.kingict.svis.obrasci.web.OrderBean;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

@ManagedBean(name="editOrdReuse")
@ViewScoped
public class EditOrderReuseFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;

	private Assign2Bean db;
	
	private String serijskiBrojOd;
	private String serijskiBrojDo;
	
	private String serijskiBrojOdControl;
	private String serijskiBrojDoControl;
	
	private DocumentBean doc;
	private UserBean ub;
	private boolean block = false;
	
	public EditOrderReuseFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			FacesContext context = FacesContext.getCurrentInstance();
			//context.getExternalContext().getSessionMap().get("user");
    		
	    	ub = (UserBean) context.getExternalContext().getSessionMap().get("user");   			
			db = (Assign2Bean) context.getExternalContext().getSessionMap().get("reuseord");

			this.selected = (Integer) context.getExternalContext().getSessionMap().get("residord");
			
			this.serijskiBrojOd = db.getSerijskiBrojOd();
			this.serijskiBrojDo = db.getSerijskiBrojDo();
			
			this.serijskiBrojOdControl = db.getSerijskiBrojOd();
			this.serijskiBrojDoControl = db.getSerijskiBrojDo();
			
			this.doc = db.getDoc();
			
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
	
	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_reuse_serial?faces-redirect=true");
	}
		
	public void free(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			System.out.println("OSLOBODI");
			System.out.println(serijskiBrojOd);
			System.out.println(serijskiBrojDo);
			
			/*  
			 *  kontrola, moguće unijeti samo interval iz slobodnog raspona
			 * 	
			 * • Početni slobodni broj mora biti >= Početni serijski broj narudžbe
			 * • Završni slobodni broj mora biti <= Završni serijski broj narudžbe
			 * 
			 * */
			
			Integer poc = Integer.parseInt(serijskiBrojOd.replace(Const.PATTERN, Const.BLANK));
			Integer zav = Integer.parseInt(serijskiBrojDo.replace(Const.PATTERN, Const.BLANK));
			
			Integer controlPoc = Integer.parseInt(serijskiBrojOdControl.replace(Const.PATTERN, Const.BLANK));
			Integer controlZav = Integer.parseInt(serijskiBrojDoControl.replace(Const.PATTERN, Const.BLANK));
			
			if (poc.intValue() < controlPoc.intValue() || zav.intValue() > controlZav.intValue()){
				FacesContext context = FacesContext.getCurrentInstance();  
		        context.addMessage(null, new FacesMessage("Raspon serijskih brojeva mora biti u intervalu " + serijskiBrojOdControl + " - " + serijskiBrojDoControl, ""));
			
		        return;
			}
			
			trx = ctxA.getDatabaseManager().createTransaction("sys");
	
			/***
			insert into vis_ex.gn_docs_reuse_serial(id_gn_docs_reuse_serial, id_gn_org_docs, id_gn_item_orders_type,serijski_broj_od,serijski_broj_do,kolicina, date_created, date_modified, modified_by, created_by, activity)
			values (VIS_EX.GN_DOCS_REUSE_SEQ.nextval,?,?,?,?,null, sysdate, sysdate, ?, ?, 1)" type="java.lang.String">
			 ***/
			
			if (db.getDoc().getCode().equals(Const.BARKOD)){
				System.out.println("oslobađam barkodove u rasponu: " + serijskiBrojOd + " - " + serijskiBrojDo);
			
				parm.add(serijskiBrojOd);
				parm.add(serijskiBrojDo);
				parm.add(db.getIdStavkaNarudzbe());
				
				trx.executePreparedUpdateById("test", "BarcodeReuseSerialNumbers", parm);
				trx.commit();
			
			}
			else {
				parm.add(db.getDoc().getIdDocument());
				parm.add(db.getIdStavkaNarudzbe());
				parm.add(serijskiBrojOd);
				parm.add(serijskiBrojDo);
				parm.add(ub.getUsername());
				parm.add(ub.getUsername());
				
				trx.executePreparedUpdateById("test", "InsertReuseSerialNumbers", parm);
				trx.commit();
			}
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Pohranjeni serijski brojevi mogu se ponovo naručiti", ""));
	        
	        block = true;
			
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
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSerijskiBrojOd() + " - " + getSerijskiBrojDo());
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

	public String getSerijskiBrojOd() {
		return serijskiBrojOd;
	}

	public void setSerijskiBrojOd(String serijskiBrojOd) {
		this.serijskiBrojOd = serijskiBrojOd;
	}

	public String getSerijskiBrojDo() {
		return serijskiBrojDo;
	}

	public void setSerijskiBrojDo(String serijskiBrojDo) {
		this.serijskiBrojDo = serijskiBrojDo;
	}

	public DocumentBean getDoc() {
		return doc;
	}

	public void setDoc(DocumentBean doc) {
		this.doc = doc;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public boolean isBlock() {
		return block;
	}

	public void setBlock(boolean block) {
		this.block = block;
	}

	public String getSerijskiBrojOdControl() {
		return serijskiBrojOdControl;
	}

	public void setSerijskiBrojOdControl(String serijskiBrojOdControl) {
		this.serijskiBrojOdControl = serijskiBrojOdControl;
	}

	public String getSerijskiBrojDoControl() {
		return serijskiBrojDoControl;
	}

	public void setSerijskiBrojDoControl(String serijskiBrojDoControl) {
		this.serijskiBrojDoControl = serijskiBrojDoControl;
	}
	
}
