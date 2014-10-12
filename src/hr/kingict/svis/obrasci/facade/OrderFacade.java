package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.OrderBean;
import hr.kingict.svis.obrasci.web.Org4Doc;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.DataSource;

@ManagedBean(name="order")
@ViewScoped
public class OrderFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private ArrayList<OrderBean> orderList = new ArrayList<OrderBean>();
	private Integer selected;
	private boolean confirm;
	private int step;
	
	//search params
	private Integer kolicina;
	private Integer id;
	private OrganizationBean dobavljac;
	private DocumentBean selDoc;
	private Org4Doc selDobavljac;
	private BigDecimal cijena;
	private Date datumNarudzbe;
	private String napomena;
	private int rola;
	
	private UserBean ub;
	private OrganizationBean narucitelj;
	
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	private ArrayList<Org4Doc> orgList = new ArrayList<Org4Doc>();
	
	public OrderFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();

			if (rola != 5 && rola != 2 && rola != 12 && rola != 1226 && rola != 1228){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				setStep(1);
				
				datumNarudzbe = new Date();
				selDoc = new DocumentBean();
				selDobavljac = new Org4Doc();
				narucitelj = new OrganizationBean();
	    		
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				
		    	ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
				
		    	if (rola == Const.ROLA_OBJEKT){
		    		parm.add(ub.getIdOrganizacije());//od logiranog usera
					rs =  trx.executePreparedQueryById("test", "DokumentiNarudzbaObjektHistory", parm);
		    	} 
		    	else {
					parm.add(ub.getIdOrganizacije());//od logiranog usera
					rs =  trx.executePreparedQueryById("test", "DokumentiNarudzbaHistory", parm);
		    	}
				while (rs.next()){
					DocumentBean doc = new DocumentBean();
					doc.setIdDocument(rs.getInt(1));
					doc.setCode(rs.getString(2));
					doc.setDesc(rs.getString(3));
					doc.setIdUnit(rs.getInt(4));
					doc.setUnit(rs.getString(5));
					doc.setPrice(rs.getBigDecimal(6));
					doc.setMultiply(rs.getInt(7));
					doc.setPrefix(rs.getString(8));
					doc.setSerialNo(rs.getString(9));
					doc.setCurrentSerialNo(rs.getInt(10));
					doc.setPakiranjeKom(rs.getInt(11));
					doc.setMaxNumber(rs.getInt(12));
					docList.add(doc);
				}
				
				rs.close();
				
				if (rola == Const.ROLA_OBJEKT.intValue()){
					rs =  trx.executePreparedQueryById("test", "SelectNaruciteljObjekt", parm);
					
					if (rs.next()){
						narucitelj.setIdOrganization(rs.getInt(1));
						narucitelj.setName(rs.getString(2));
						narucitelj.setTel(rs.getString(5));
						narucitelj.setFax(rs.getString(6));
						narucitelj.setEmail(rs.getString(7));
						narucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
						narucitelj.setOib(rs.getString(9));
						narucitelj.setType(Const.CODE_OBJEKT);
					}
				}
				else {
					rs =  trx.executePreparedQueryById("test", "SelectNaruciteljOrg", parm);
					
					if (rs.next()){
						narucitelj.setIdOrganization(rs.getInt(1));
						narucitelj.setName(rs.getString(2));
						narucitelj.setTel(rs.getString(5));
						narucitelj.setFax(rs.getString(6));
						narucitelj.setEmail(rs.getString(7));
						narucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
						narucitelj.setOib(rs.getString(9));
						narucitelj.setType(Const.CODE_ORG);
					}
				}
				
				rs.close();
				//provjeri ima li nepotvrđenih narudžbi
				rs =  trx.executePreparedQueryById("test", "CheckOrderReceptionExists", parm);
				
				if (rs.next()){
					
					int brojNepot = rs.getInt(1);
					
					if (brojNepot > 0){
						context.addMessage(null, new FacesMessage("Molimo vas potvrdite prijem svih pristiglih narudžbi.", "")); 
					}
				}
				rs.close();
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
	
	private int getNextId(int length){	
		int maxId = 0;
		
		for (int i=0; i < length; i++){
			if (orderList.get(i).getId() >= maxId){
				maxId = orderList.get(i).getId();
            }
		}
		
		return maxId+1;
	}
	
	private String generateSerialNumber(Integer counter, String serijskiBroj){
		String genSerialNum = null;
		
		try {
			 System.out.println("SERIJSKI BROJ: " + serijskiBroj);
			 System.out.println("Brojac: " + counter);
			 String reverse = new StringBuffer(serijskiBroj).reverse().toString();
			 String brojacRev = new StringBuffer(counter.toString()).reverse().toString();
			 System.out.println("Rev serijski broj: " + reverse);
			 System.out.println("Rev brojac: " + brojacRev);
			 genSerialNum = brojacRev + reverse.substring(brojacRev.length(), reverse.length());
			 genSerialNum = new StringBuffer(genSerialNum).reverse().toString();
			 System.out.println("GENERIRANI SERIJSKI BROJ: " + genSerialNum);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return genSerialNum;
	}
	
	public void add(ActionEvent event){
		DocumentBean doc = getSelDoc();
		
		if (doc == null || kolicina == null || cijena == null){
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Niste unijeli sve potrebne podatke", ""));
		
	        return;
		}
		
		if (getSelDobavljac() == null){
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Niste odabrali dobavljača.", ""));
		
	        return;
		}
		
		if (doc.getMaxNumber().intValue() != -1 && this.kolicina.intValue() > doc.getMaxNumber().intValue())
	    {
	      FacesContext context = FacesContext.getCurrentInstance();
	      context.addMessage(null, new FacesMessage("Maksimalna dozvoljena količina na zahtjevu za obrazac " + doc.getCode() + " je " + doc.getMaxNumber().intValue() + ".", ""));
	      
	      return;
	    }
		
		if (doc.getMultiply() != null){
			
			int visekratnik = doc.getMultiply().intValue();
			
			System.out.println("VIŠEKRATNIK : " + visekratnik);
			if (visekratnik == 0) visekratnik = 1;
			
			if ((kolicina % visekratnik) == 0){
				OrderBean ob = new OrderBean();
				ob.setId(new Integer(getNextId(orderList.size())));
				ob.setDoc(doc);
				ob.setCijena(getCijena());
				ob.setKolicina(getKolicina());
				ob.setDobavljac(getSelDobavljac());
				orderList.add(ob);
				
				//TODO clear polja 17092014
				setKolicina(null);
				setCijena(null);
				
				FacesContext context = FacesContext.getCurrentInstance();  
		        context.addMessage(null, new FacesMessage("U košaricu ste dodali obrazac " + doc.getCode(), "")); 
			}
			else {
				FacesContext context = FacesContext.getCurrentInstance();  
		        context.addMessage(null, new FacesMessage("Količina treba biti višekratnik broja " + visekratnik, "")); 
			}
		}
		
	}
	
	
	public void handleClose() {	
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			setSelDobavljac(null);
			orgList.clear();
			setKolicina(null);
			setCijena(null);
			
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			
			parm.add(getSelDoc().getIdDocument());
			rs =  trx.executePreparedQueryById("test", "Org4Doc", parm);
			
			int brojac = 0;
			while (rs.next()){
				Org4Doc item = new Org4Doc();
				item.setIdOrganizacija(rs.getInt(1));
				item.setNaziv(rs.getString(2));
				item.setCijena(rs.getBigDecimal(3));
				System.out.println("ITEM: " + item.getNaziv() + " * " + item.getIdOrganizacija());
				orgList.add(item);
				brojac++;
			}
			
			if (brojac == 1) setSelDobavljac(orgList.get(0));
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
		setKolicina(null);
		setCijena(null);
		setSelDobavljac(null);
		setSelDoc(new DocumentBean());		
	}
	
	public void next(ActionEvent event){
		if (orderList.size() > 0) setStep(2);
		else{
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Vaša narudžba je prazna!", ""));  
		}
	}
	
	public void cancel(ActionEvent event){
		//TODO: update - ponisti barkodove koji su pridruženi organizaciji
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order?faces-redirect=true");
	}
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSelected());
	}
	
	public void delete(){
		try {
			for (int i=0; i<docList.size(); i++){
				System.out.println(orderList.get(i).getId());
				if (orderList.get(i).getId().intValue() == getSelected().intValue()){
					orderList.remove(i);
					break;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}	
	}
	
	private boolean orderDB(){
		boolean error = false;
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			
			rs =  trx.executePreparedQueryById("test", "GetSequenceOrder", parm);
			Integer idOrder = null;
			
			if (rs.next()){
				idOrder = rs.getInt(1);
			}
			
			rs.close();
			
			java.util.Calendar cal1 = Calendar.getInstance();
			java.util.Calendar cal2 = Calendar.getInstance();
			java.util.Date utilDate = new java.util.Date();
			cal1.setTime(utilDate);
			
			cal2.setTime(getDatumNarudzbe());
			java.sql.Date sqlDate = new java.sql.Date(cal1.getTime().getTime()); // your sql date
			
			parm.add(idOrder);
			parm.add(new Integer(orderList.size()));
			parm.add(new java.sql.Date(cal2.getTime().getTime()));
			parm.add(Const.UNESENO);
			parm.add(new java.sql.Date(cal1.getTime().getTime()));
			parm.add(ub.getIdKorisnik());
			parm.add(new Integer(1));//activity
			

			parm.add(new Integer(0));//session
			
			BigDecimal ukupno = BigDecimal.ZERO;
			
			for (OrderBean ob : orderList){
				ukupno = ukupno.add(ob.getCijena());
			}
			System.out.println("Ukupno: " + ukupno.toString());
			parm.add(ukupno);//ukupno
			parm.add(ub.getIdKorisnik());//narucio
			parm.add(ub.getIdOrganizacije());
			parm.add(rola == Const.ROLA_OBJEKT.intValue() ? Const.CODE_OBJEKT : Const.CODE_ORG);
			parm.add(napomena);
			
			trx.executePreparedUpdateById("test", "InsertOrder", parm);
			trx.commit();
			
			//stavke
			//ID_GN_ITEM_ORDERS_TYPE, ID_GN_ITEM_ORDER, ID_GN_ORG_DOCS, KOLICINA, IZNOS, CIJENA, SERIJSKI_BROJ_OD, SERIJSKI_BROJ_DO, ISPORUCENO, VRACENO, RAZLOG_POVRATKA, VRACENO_POTVRDJENO)
			
			for (OrderBean item : orderList){
				parm.clear();
				
				Integer idItem = null;

				rs =  trx.executePreparedQueryById("test", "GetSequenceOrderItem", parm);
			
				if (rs.next()){
					idItem = rs.getInt(1);
				}
				
				rs.close();
				
				int param3 = 0;
				String param4 = Const.BLANK;
				String param5 = Const.BLANK;
				String param2 = Const.BLANK;
				int barcode1 = 0;
				int barcode2 = 0;
				boolean reusedBarcode = false;
				
				//TODO - ako je barkod onda zovi proceduru assignbarcode
				//priprema inserta stavki - generiranje serijskog broja
				try {
					
					javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
		        	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
		        	
		        	Connection conn = ds.getConnection(); 
		        	String proc3StoredProcedure = "";
		        	String procReusedBarcode = "";
		        	
		        	if (item.getDoc().getCode().equals(Const.BARKOD)){
		        	    proc3StoredProcedure = "{ call vis_ex.assign_barcodes(?, ?, ?, ?, ?, ?) }";
			            // Step-3: prepare the callable statement
			            CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
			        	
			            // Step-4: set input parameters ...
			            // first input argument
			            
			            System.out.println("KOLIČINA: " + item.getKolicina().intValue());
			            System.out.println("ID ORDER: " + idOrder);
			            System.out.println("ID DOK: " + item.getDoc().getIdDocument().intValue());
			            
			            int tempKom = item.getDoc().getPakiranjeKom().intValue();
			            System.out.println("TEMP PAKIRANJE: " + tempKom);
			            
			            if (tempKom == 0) tempKom = item.getKolicina().intValue();
			            else tempKom = item.getKolicina().intValue()*tempKom;
			            
			            System.out.println("TEMP KOLIČINE FINAL: " + tempKom);
			            
			            
			            cs.setInt(1, tempKom);
			            cs.setInt(2, idItem.intValue());//stavka
			            cs.setInt(3, ub.getIdKorisnik().intValue());
			            cs.setInt(4, narucitelj.getIdOrganization().intValue());
			            
			            // Step-5: register output parameters ...
			            cs.registerOutParameter(5, java.sql.Types.INTEGER);
			            cs.registerOutParameter(6, java.sql.Types.INTEGER);
			            // Step-6: execute the stored procedures: proc3
			            cs.execute();
			            
			            barcode1 = cs.getInt(5);
			            System.out.println("barkod od: " + barcode1);
			            
			            barcode2 = cs.getInt(6);
			            System.out.println("barkod d0: " + barcode2);
		        	}
		        	else {
		        		procReusedBarcode = "{ call vis_ex.doc_Order_Serial_2(?, ?, ?, ?, ?) }";
			        	proc3StoredProcedure = "{ call vis_ex.doc_Order_Serial(?, ?, ?) }";
			            // Step-3: prepare the callable statement
			        	CallableStatement cs1 = conn.prepareCall(procReusedBarcode);
			            CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
			        	
			            // Step-4: set input parameters ...
			            // first input argument
			            
			            int tempKom = item.getDoc().getPakiranjeKom().intValue();
			            System.out.println("TEMP PAKIRANJE: " + tempKom);
			            
			            System.out.println("KOLIČINA: " + item.getKolicina().intValue());
			            System.out.println("ID DOK: " + item.getDoc().getIdDocument().intValue());
			            
			            if (tempKom == 0) tempKom = item.getKolicina().intValue();
			            else tempKom = item.getKolicina().intValue()*tempKom;
			            
			            System.out.println("TEMP KOLIČINE FINAL: " + tempKom);
			            
			            /*Provjeri može li se iskoristiti raspon storniranih serijskih brojeva*/
			            cs1.setInt(1, tempKom);
			            cs1.setInt(2, item.getDoc().getIdDocument().intValue());
			            cs1.registerOutParameter(3, java.sql.Types.VARCHAR);
			            cs1.registerOutParameter(4, java.sql.Types.VARCHAR);
			            cs1.registerOutParameter(5, java.sql.Types.VARCHAR);
			            
			            System.out.println("{ call vis_ex.doc_Order_Serial_2(" + tempKom + ", " + item.getDoc().getIdDocument().intValue() + ", " + param2 + ", " + param4 + ", " + param5 + ") }");
			            
			            cs1.execute();
			            
			            param2 = cs1.getString(3);	//ser broj od
			            param4 = cs1.getString(4);	//ser broj do
			            param5 = cs1.getString(5);	//zadnji ser broj u rasponu
			            
			            System.out.println("reuse ser broj od: " + param2);
			            System.out.println("reuse ser broj do: " + param4);
			            System.out.println("reuse ser broj last: " + param5);
			            
			            if ("0".equals(param2)) {
			            	reusedBarcode = false;
			            }
			            else {
			            	reusedBarcode = true;
			            }
			            
			            if (reusedBarcode == false){
				            /*Ako nema raspona među storniranim serijskim brojevima generiraj nove*/
				            cs.setInt(1, tempKom);
				            cs.setInt(2, item.getDoc().getIdDocument().intValue());
				            
				            // Step-5: register output parameters ...
				            cs.registerOutParameter(3, java.sql.Types.INTEGER);
				            // Step-6: execute the stored procedures: proc3
				            cs.execute();
				            
				            param3 = cs.getInt(3);
				            System.out.println("rezultat: " + param3);
			            }
			           
		        	}
		        	
		        	conn.close();
				
				} catch (Exception e){
					e.printStackTrace();
					System.out.println("error");
				}
				
				//odrediti koji se serijski brojevi pohranjuju - uzeti counter i generirati serijskiBrojDo, umanjiti counter za kolicinu i generirati serijskibrojod
				
				parm.clear();
				parm.add(idItem);
				parm.add(idOrder);
				parm.add(item.getDoc().getIdDocument());
				parm.add(item.getKolicina());
				parm.add(item.getCijena());
				parm.add(item.getDoc().getPrice());
				parm.add(item.getDobavljac().getIdOrganizacija());
				
				if (item.getDoc().getCode().equals(Const.BARKOD)){
					parm.add(String.valueOf(barcode1));
					parm.add(String.valueOf(barcode2));
				}
				else {
					
					if ("0".equals(item.getDoc().getSerialNo())){
						parm.add(Const.BLANK);//serijski_broj_od
						parm.add(Const.BLANK);//serijski_broj_do
					} else if (reusedBarcode == true){
						parm.add(param2);
						parm.add(param4);
					} else if (item.getDoc().getPakiranjeKom().intValue() > 0){
						parm.add(generateSerialNumber(new Integer(param3 - item.getDoc().getPakiranjeKom().intValue()*item.getKolicina().intValue() + 1),item.getDoc().getSerialNo()));//serijski_broj_od
						parm.add(generateSerialNumber(new Integer(param3),item.getDoc().getSerialNo()));//serijski_broj_do
					} else {
						parm.add(generateSerialNumber(new Integer(param3 - item.getKolicina().intValue() + 1),item.getDoc().getSerialNo()));//serijski_broj_od
						parm.add(generateSerialNumber(new Integer(param3),item.getDoc().getSerialNo()));//serijski_broj_do
					}
					
				}
				trx.executePreparedUpdateById("test", "InsertOrderItem", parm);
				trx.commit();
			}
			
			
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			error = true;
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
		return error;
	}
	
	public void potvrdi(){
		if (!confirm){
			FacesContext context = FacesContext.getCurrentInstance();  
	        context.addMessage(null, new FacesMessage("Niste potvrdili narudžbu!", "")); 
		}
		else if (getSelDobavljac()!=null){
			
			boolean error = orderDB();
			
			if (!error){
				setStep(3);
				FacesContext context = FacesContext.getCurrentInstance();  
		        context.addMessage(null, new FacesMessage("Vaša narudžba je zaprimljena!", ""));
			}
			else {
				FacesContext context = FacesContext.getCurrentInstance();  
				context.addMessage(null, new FacesMessage("Dogodila se pogreška prilikom spremanja narudžbe!", "")); 
			}
		}
	}
	
	public void handleClose2() {
		System.out.println("test row: ");
		System.out.println(getSelDobavljac().getIdOrganizacija() + " * " + getSelDobavljac().getNaziv());
	}
	
	public void calc() {
		if (getKolicina() != null) setCijena(new BigDecimal(getKolicina()).multiply(getSelDoc().getPrice()));
	}

	public ArrayList<OrderBean> getOrderList() {
		return orderList;
	}

	public void setOrderList(ArrayList<OrderBean> orderList) {
		this.orderList = orderList;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public Integer getKolicina() {
		return kolicina;
	}

	public void setKolicina(Integer kolicina) {
		this.kolicina = kolicina;
	}

	public OrganizationBean getDobavljac() {
		return dobavljac;
	}

	public void setDobavljac(OrganizationBean dobavljac) {
		this.dobavljac = dobavljac;
	}

	public DocumentBean getSelDoc() {
		return selDoc;
	}

	public void setSelDoc(DocumentBean selDoc) {
		this.selDoc = selDoc;
	}

	public Org4Doc getSelDobavljac() {
		return selDobavljac;
	}

	public void setSelDobavljac(Org4Doc selDobavljac) {
		this.selDobavljac = selDobavljac;
	}

	public ArrayList<DocumentBean> getDocList() {
		return docList;
	}

	public void setDocList(ArrayList<DocumentBean> docList) {
		this.docList = docList;
	}

	public ArrayList<Org4Doc> getOrgList() {
		return orgList;
	}

	public void setOrgList(ArrayList<Org4Doc> orgList) {
		this.orgList = orgList;
	}

	public BigDecimal getCijena() {
		return cijena;
	}

	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isConfirm() {
		return confirm;
	}

	public void setConfirm(boolean confirm) {
		this.confirm = confirm;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public OrganizationBean getNarucitelj() {
		return narucitelj;
	}

	public void setNarucitelj(OrganizationBean narucitelj) {
		this.narucitelj = narucitelj;
	}

	public Date getDatumNarudzbe() {
		return datumNarudzbe;
	}

	public void setDatumNarudzbe(Date datumNarudzbe) {
		this.datumNarudzbe = datumNarudzbe;
	}

	public String getNapomena() {
		return napomena;
	}

	public void setNapomena(String napomena) {
		this.napomena = napomena;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}
	
}
