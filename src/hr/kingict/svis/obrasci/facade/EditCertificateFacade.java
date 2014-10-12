package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.CertifikatJezici;
import hr.kingict.svis.obrasci.web.Drzava;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.sql.DataSource;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.BLOB;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.ibm.ws.rsadapter.jdbc.WSJdbcConnection;
import com.ibm.ws.rsadapter.jdbc.WSJdbcUtil;

@ManagedBean(name="editCertSif")
@ViewScoped
public class EditCertificateFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;
	
	private UploadedFile file;  
	private StreamedContent fileDownload;
	private StreamedContent modelDownload;
	
	//search params
	private String kod;
	private String naziv;
	private BigDecimal cijena;
	private String komentar;
	private boolean disablePregled = false;
	
	private String filePath;
	
	private ArrayList<SelectItem> mjeraList = new ArrayList<SelectItem>();
	private Integer selectedMjera;
	
	private List<Drzava> drzavaList = new ArrayList<Drzava>();
	private Drzava[] selectedDrzava = new Drzava[]{};
	
	private String selectedDrzavaText;
	
	private List<String> selectedJezici = new ArrayList<String>();
	private Map jezici = new HashMap<String, String>();
	
	private Date datumOd = null;
	private Date datumDo = null;
	
	private boolean status; 
	
	private boolean edit;
	private Certifikat db;
	
	private String serijskiBroj;
	private String oznakaSerije;
	
	private Integer selectedCertTip;
	private ArrayList<SelectItem> certTipovi = new ArrayList<SelectItem>();
	
	private boolean certGenerated = false;
	
	private String destination="C:\\temp\\";
	
	private Integer idNewCert;
	
	private boolean showUpload = false;
	
	public EditCertificateFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			trx = ctxA.getDatabaseManager().createTransaction("sys");
			
			FacesContext context = FacesContext.getCurrentInstance();
			db = (Certifikat) context.getExternalContext().getSessionMap().get("certSif");			
			
			InputStream stream = null;

			if (db == null) {
	
				status = true;
				setDatumOd(new Date());
				setOznakaSerije(Const.CERT_OZNAKA_SERIJE);
				setSerijskiBroj(Const.CERT_SERIJA);
			}
			else {

				parm.add(db.getIdCertifikat());
				rs =  trx.executePreparedQueryById("test", "GetCertifikatModel2", parm);
			
				if (rs.next()){	
					serijskiBroj = rs.getString(1);
					oznakaSerije = rs.getString(2);
					
					selectedCertTip = rs.getInt(3);
					
					if (rs.getString(5).equals("1")){
						status = true;
					}
					else status = false;
				}
				
				showUpload = true;
				
				rs.close();
				
				List<CertifikatJezici> tmpJezici = new ArrayList<CertifikatJezici>();
				
				parm.clear();
				parm.add(db.getIdCertifikat());
				rs =  trx.executePreparedQueryById("test", "GetCertifikatJezici", parm);
				
				CertifikatJezici cj = null;
				
				while(rs.next()){
					cj = new CertifikatJezici();
					cj.setIdJezik(rs.getInt(1));
					cj.setNazivJezik(rs.getString(2));
					tmpJezici.add(cj);
				}
				
				db.setJezici(tmpJezici);
				
				for (CertifikatJezici j: tmpJezici){
					selectedJezici.add(j.getIdJezik().toString());
				}
				
				rs.close();
				
				//drzave
				List<Drzava> tmpDrzave = new ArrayList<Drzava>();
				Drzava state = null;
				String tmpDrzaveText = "";
				List<Drzava> list  = new ArrayList<Drzava>();
				
				parm.clear();
				parm.add(db.getIdCertifikat());
				rs =  trx.executePreparedQueryById("test", "GetCertifikatDrzave", parm);
				
				while(rs.next()){
					state = new Drzava();
					state.setIdDrzava(rs.getInt(1));
					state.setNaziv(rs.getString(2));
					tmpDrzave.add(state);
					
					tmpDrzaveText += state.getNaziv() + ", ";
					
					list.add(state);
				}
				
				selectedDrzava = list.toArray(new Drzava[list.size()]);
				db.setDrzave(tmpDrzave);
				
				tmpDrzaveText = tmpDrzaveText.substring(0, tmpDrzaveText.lastIndexOf(","));
				setSelectedDrzavaText(tmpDrzaveText);		
				
				rs.close();
				//end drzave
				
				Date d = new Date();
				SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddkkmmssSSS");  
				String fileName = null;
				filePath = dt.format(d) + ".pdf";
				
				byte[] buf = null;
				
				try {
					
					parm.clear();
					parm.add(db.getIdCertifikat());
					rs =  trx.executePreparedQueryById("test", "GetCertifikatModel", parm);
				
					if (rs.next()){
						buf = rs.getBytes(1);
						fileName = rs.getString(2);
					}	
			    	if (buf == null || buf.length == 0) {
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Model certifikata je prazan.",  "Učitajte model");  
			            FacesContext.getCurrentInstance().addMessage(null, message);
					
			            setDisablePregled(true);
			    	}
									
					if (buf != null){
						
						if ((buf.length < FacesContext.getCurrentInstance().getExternalContext().getResponseBufferSize()) && (buf.length > 0)) {
					        FacesContext.getCurrentInstance().getExternalContext().setResponseBufferSize(buf.length);
						}
						
						FileOutputStream fileOuputStream = new FileOutputStream(destination + filePath); 
						fileOuputStream.write(buf);
						fileOuputStream.close();
						
						//fileName = destination + dt.format(d) + "-" +  fileName;
					    stream = new FileInputStream(new File(destination + filePath));
						fileDownload = new DefaultStreamedContent(stream, "application/pdf", filePath);
					
					} 
					
					rs.close();
					
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			
			
			//FacesContext context = FacesContext.getCurrentInstance();
			//context.getExternalContext().getSessionMap().get("user");
			parm.clear();
    		rs =  trx.executePreparedQueryById("test", "SelectCertifikatMjera", parm);
			
			while (rs.next()){
				mjeraList.add(new SelectItem(rs.getInt(1), rs.getString(2)));
			}
			
			rs.close();
			
			rs =  trx.executePreparedQueryById("test", "SelectLanguages", parm);
			
			while (rs.next()){
				jezici.put(rs.getString(2), rs.getString(1));
			}
			
			rs.close();
			
			rs =  trx.executePreparedQueryById("test", "SelectStates", parm);
			
			while (rs.next()){
				Drzava d = new Drzava();
				d.setIdDrzava(rs.getInt(1));
				d.setKod(rs.getString(2));
				d.setNaziv(rs.getString(3));
				
				drzavaList.add(d);
			}
			
			rs.close();
			
			rs =  trx.executePreparedQueryById("test", "GetCertifikatTip", parm);
			
			while (rs.next()){
				certTipovi.add(new SelectItem(rs.getInt(1), rs.getString(2)));
			}
			
			rs.close();
			
			if (db == null) {
				edit = false;
			}
			else {
				edit = true;
				setSelected(db.getIdCertifikat());
				setNaziv(db.getNazivCertifikat());
				setKod(db.getKodCertifikat());
				setCijena(db.getCijena());
				setKomentar(db.getKomentar());
				setDatumOd(db.getDatumVrijediOd());
				setDatumDo(db.getDatumVrijediDo());
				setSerijskiBroj(db.getSerijskiBroj());
				setOznakaSerije(db.getOznakaSerije());
				setSelectedMjera(db.getIdMjera());
				setSelectedCertTip(db.getTip());
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
	
	public void selectDrzava(ActionEvent event){
		String tmp = "";
		
		for (Drzava p : selectedDrzava){
			System.out.println(p.getIdDrzava() + " - " + p.getNaziv());
			
			tmp += p.getNaziv() + ", ";
		}
		tmp = tmp.substring(0, tmp.lastIndexOf(","));
		setSelectedDrzavaText(tmp);
	}
	
	public void handleClose() {
		System.out.println("test row: ");
	}

	public void handleClose2() {
		System.out.println("test row: ");
	}

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificates?faces-redirect=true");
	}
		
	public void delete(ActionEvent event){
		try {
			copyFile(null, new ByteArrayInputStream(new byte[0]));
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Model certifikata je obrisan.",  "Nije moguće izdati certifikat u izradi");  
            FacesContext.getCurrentInstance().addMessage(null, message);
            
            setDisablePregled(true);
		}
		catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public void add(){
		ITransaction trx = null;
		ITransaction trx2 = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			//FacesContext context = FacesContext.getCurrentInstance();
			//context.getExternalContext().getSessionMap().get("user");
			
			if (getSelectedDrzava() == null || getSelectedMjera() == null || getKod() == null ||
				getNaziv() == null	|| getCijena() == null || getSerijskiBroj() == null || getOznakaSerije() == null){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Niste unijeli sve potrebne podatke.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
		        
		        return;
			}
			
			if (getSelectedJezici().size() == 0){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Morate odabrati barem 1 jezik.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
		        
		        return;
			}
			
			Integer idDocumentSif = null;
			Integer idKorisnik = ((UserBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user")).getIdKorisnik();
			
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
    		
    		if (!edit){
    			//insert
				rs =  trx.executePreparedQueryById("test", "GetSequenceCertSif", parm);
				
				if (rs.next()){
					idDocumentSif = rs.getInt(1);
					setIdNewCert(idDocumentSif);
				}
				
				rs.close();
				
				parm.add(idDocumentSif);
				parm.add(getNaziv());
				parm.add(null); //getSelectedDrzava().getIdDrzava()
				parm.add(getKod().toUpperCase());
				parm.add(getSelectedMjera());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				parm.add(getSelectedCertTip());
				
				if (datumOd != null){
					//datum od
					java.util.Calendar cal1 = Calendar.getInstance();
					cal1.setTime(datumOd);
					parm.add(new java.sql.Date(cal1.getTime().getTime()));
				}
				else parm.add(null);
				
				if (datumDo != null){
				//datum do
					java.util.Calendar cal2 = Calendar.getInstance();
					cal2.setTime(datumDo);
					parm.add(new java.sql.Date(cal2.getTime().getTime()));
				}
				else parm.add(null);
				
				parm.add(getKomentar());
				parm.add(getSerijskiBroj());
				parm.add(getOznakaSerije());
				parm.add(getCijena());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				parm.add(idKorisnik);
				
				trx.executePreparedUpdateById("test", "InsertCertifikat", parm);
			}
    		else {
    			
    			idDocumentSif = db.getIdCertifikat();
    			//update
				parm.add(getNaziv());
				parm.add(null); //getSelectedDrzava().getIdDrzava()
				parm.add(getKod());
				parm.add(getSelectedMjera());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				parm.add(getSelectedCertTip());
				
				//datum promjene
				java.util.Calendar chgDate = Calendar.getInstance();
				chgDate.setTime(new Date());
				parm.add(new java.sql.Date(chgDate.getTime().getTime()));
				
				if (datumOd != null){
					//datum od
					java.util.Calendar cal1 = Calendar.getInstance();
					cal1.setTime(datumOd);
					parm.add(new java.sql.Date(cal1.getTime().getTime()));
				}
				else parm.add(null);
				
				if (datumDo != null){
				//datum do
					java.util.Calendar cal2 = Calendar.getInstance();
					cal2.setTime(datumDo);
					parm.add(new java.sql.Date(cal2.getTime().getTime()));
				}
				else parm.add(null);
				
				parm.add(getKomentar());
				parm.add(getSerijskiBroj());
				parm.add(getOznakaSerije());
				parm.add(getCijena());
				parm.add(isStatus() == true ? new Integer(1): new Integer(0));
				parm.add(idKorisnik);
				parm.add(db.getIdCertifikat());
				
				trx.executePreparedUpdateById("test", "UpdateCertifikat", parm);
    		}
			trx.commit();
			
			//update jezici
			
			if (edit){
				//update
				
				trx2 = ctxA.getDatabaseManager().createTransaction("sys");
				
				List<Integer> l1 = new ArrayList<Integer>();
				parm.clear();
				parm.add(db.getIdCertifikat());
				rs =  trx.executePreparedQueryById("test", "SelectCertifikatJezici", parm);
				
				while(rs.next()){
					l1.add(rs.getInt(2));
				}
				
				rs.close();
				
				List<String> ljezici = getSelectedJezici();
				List<Integer> postoji = new ArrayList<Integer>();
				Integer noviJezik = null;
				
				for (String item : ljezici){
					boolean exists = false;
					
					int brojac = 0;
					for (Integer a1 : l1){
						if (Integer.parseInt(item) == a1.intValue()){
							//
							exists = true;
							System.out.println("POSTOJI ZAPIS ZA JEZIK: " + a1);
							postoji.add(new Integer(brojac));
							
							break;
						}
						brojac++;
					}
					if (!exists){
						//insert
						noviJezik = Integer.parseInt(item);
						
						ResultSet rs1 = null;
						Integer idCertLang = null;
						
						parm.clear();
						rs1 =  trx2.executePreparedQueryById("test", "GetSequenceCertLangSif", parm);
						
						if (rs1.next()){
							idCertLang = rs1.getInt(1);
						}
						
						parm.add(idCertLang);
						parm.add(noviJezik);
						parm.add(db.getIdCertifikat());
						
						System.out.println("Dodajem novi jezik " + noviJezik);
						trx2.executePreparedUpdateById("test", "InsertCertifikatJezici", parm);
						trx2.commit();
						
						rs1.close();
					}
				}
			
				Collections.sort(postoji);
				Collections.reverse(postoji);
				
				for (Integer i : postoji){
					System.out.println("MiČEM: " + i);
					l1.remove(i.intValue());
				}
				
				//deaktivacija jezika
				for (Integer item : l1){
					
					System.out.println("Deaktiviram jezik " + item);
					parm.clear();
					
					parm.add(db.getIdCertifikat());
					parm.add(item);
					
					trx2.executePreparedUpdateById("test", "UpdateCertifikatJezici", parm);
					trx2.commit();
					
				}	
				
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspješno ažurirani podaci.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
			}
			else {
				//insert
				trx2 = ctxA.getDatabaseManager().createTransaction("sys");
				
				for (String s: getSelectedJezici()){
					
					ResultSet rs1 = null;
					Integer idCertLang = null;
					
					parm.clear();
					rs1 =  trx2.executePreparedQueryById("test", "GetSequenceCertLangSif", parm);
					
					if (rs1.next()){
						idCertLang = rs1.getInt(1);
					}
					
					parm.add(idCertLang);
					parm.add((Integer) Integer.parseInt(s));
					parm.add(getIdNewCert());
					
					System.out.println("Dodajem novi jezik " + s);
					trx2.executePreparedUpdateById("test", "InsertCertifikatJezici", parm);
					trx2.commit();
					
					rs1.close();
				}
			}
			
			//insert drzave
			
			if (edit){
				//update
				
				trx2 = ctxA.getDatabaseManager().createTransaction("sys");
				
				List<Integer> l1 = new ArrayList<Integer>();
				parm.clear();
				parm.add(db.getIdCertifikat());
				rs =  trx.executePreparedQueryById("test", "SelectCertifikatDrzave", parm);
				
				while(rs.next()){
					l1.add(rs.getInt(2));//drzave
				}
				
				rs.close();
				
				Drzava[] ldrzave = getSelectedDrzava();
				List<Integer> postoji = new ArrayList<Integer>();
				Integer novaDrzava = null;
				
				for (Drzava item : ldrzave){
					boolean exists = false;
					
					int brojac = 0;
					for (Integer a1 : l1){
						if (item.getIdDrzava().intValue() == a1.intValue()){
							//
							exists = true;
							System.out.println("POSTOJI ZAPIS ZA DRZAVU: " + a1);
							postoji.add(new Integer(brojac));
							
							break;
						}
						brojac++;
					}
					if (!exists){
						//insert
						novaDrzava = item.getIdDrzava();
						
						ResultSet rs1 = null;
						Integer idCertState = null;
						
						parm.clear();
						rs1 =  trx2.executePreparedQueryById("test", "GetSequenceCertDrzaveSif", parm);
						
						if (rs1.next()){
							idCertState = rs1.getInt(1);
						}
						
						parm.add(idCertState);
						parm.add(novaDrzava);
						parm.add(db.getIdCertifikat());
						
						System.out.println("Dodajem novu drzavu " + novaDrzava);
						trx2.executePreparedUpdateById("test", "InsertCertifikatDrzave", parm);
						trx2.commit();
						
						rs1.close();
					}
				}
			
				Collections.sort(postoji);
				Collections.reverse(postoji);
				
				for (Integer i : postoji){
					System.out.println("MiČEM: " + i);
					l1.remove(i.intValue());
				}
				
				//deaktivacija drzave
				for (Integer item : l1){
					
					System.out.println("Deaktiviram drzavu " + item);
					parm.clear();
					
					parm.add(db.getIdCertifikat());
					parm.add(item);
					
					trx2.executePreparedUpdateById("test", "UpdateCertifikatDrzave", parm);
					trx2.commit();
					
				}	
				
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspješno ažurirani podaci.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
			}
			else {
				//insert
				trx2 = ctxA.getDatabaseManager().createTransaction("sys");
				
				for (Drzava s: getSelectedDrzava()){
					
					ResultSet rs1 = null;
					Integer idCertState = null;
					
					parm.clear();
					rs1 =  trx2.executePreparedQueryById("test", "GetSequenceCertDrzaveSif", parm);
					
					if (rs1.next()){
						idCertState = rs1.getInt(1);
					}
					
					parm.add(idCertState);
					parm.add(s.getIdDrzava());
					parm.add(getIdNewCert());
					
					System.out.println("Dodajem novi jezik " + s);
					trx2.executePreparedUpdateById("test", "InsertCertifikatDrzave", parm);
					trx2.commit();
					
					rs1.close();
				}
				
				showUpload = true;
				
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspješno spremljeni podaci.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
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
			if (trx2 != null)
				try {
					trx2.close();
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

	public void handleFileUpload(FileUploadEvent event) {       
        try {
            copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
            setDisablePregled(false);
            
            FacesMessage msg = new FacesMessage("Uspješno završeno! ", "datoteka " + event.getFile().getFileName() + " je učitana.");  
            FacesContext.getCurrentInstance().addMessage(null, msg);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }  

    public void copyFile(String fileName, InputStream in) {
       try {
    	   	Date d = new Date();
			SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddkkmmssSSS");  
			
			byte[] dokument = IOUtils.toByteArray(in); 
			
			FileOutputStream fileOuputStream = new FileOutputStream(destination + filePath); 
			fileOuputStream.write(dokument);
			fileOuputStream.close();
            
			InputStream stream = new FileInputStream(new File(destination + filePath));
			
            //osvježi link
			fileDownload = new DefaultStreamedContent(stream, "application/pdf", filePath);
            
    		try {
    			javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
	        	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
	        	
	        	Connection conn1 = ds.getConnection(); 
	        	OracleConnection conn = (OracleConnection) WSJdbcUtil.getNativeConnection((WSJdbcConnection) conn1);  
	        	conn.setAutoCommit(false);
	        		        	
	        	String queryString = null;
	        	PreparedStatement pstmt1 = null;
	        	
	        	queryString = "update vis_ex.cert_certificates " +
					  		  "set document = EMPTY_BLOB(), document_name = ? " +
					  		  "where id=?";
		
	        	pstmt1 = conn.prepareStatement(queryString);
	        	pstmt1.setString(1, fileName);
	        	pstmt1.setInt(2,db.getIdCertifikat());
	        	
	        	pstmt1.executeUpdate();
	        	pstmt1.close();
	        	conn.commit();
	        						 
	        	queryString = "SELECT document FROM vis_ex.cert_certificates WHERE id = ? FOR UPDATE";

	        	BLOB blob;
	        	
	        	pstmt1 = conn.prepareStatement(queryString);
	        	if (edit){
	        		pstmt1.setInt(1, db.getIdCertifikat());
	        	}
	        	else {
	        		pstmt1.setInt(1, getIdNewCert());
	        	}
	        	ResultSet rset = pstmt1.executeQuery();
	        	rset.next();
	        	blob = ((OracleResultSet)rset).getBLOB(1);
	        	
	        	OutputStream outstream = blob.setBinaryStream(1L);
	        	
	        	int size = blob.getBufferSize();
	        	byte[] buffer = new byte[size];
	        	int length = -1;
	        	
	        	while ((length = in.read(buffer)) != -1){
	        		outstream.write(buffer, 0, length);
	        	}
	        	
	        	in.close();
	        	outstream.close();
	        	conn.commit();
	        	//conn.close();
	        	conn1.close();
    		}
    		catch (Exception e){
    			e.printStackTrace();
    		}
            
       } 
       catch (IOException e) {
            System.out.println(e.getMessage());
       }
    } 
    

    public void prepareCertificateToDownload(ActionEvent event){
		File file = new File(destination + filePath);
        InputStream stream = null; 
	     
        try {     	
        	System.out.println("PREPARING MODEL 4 DOWNLOAD...");
        	System.out.println("PATH: " + destination + filePath);
            
            stream = new FileInputStream(file);
            setFileDownload(new DefaultStreamedContent(stream, "application/pdf", "model.pdf"));
        }
        catch (Exception e){
			e.printStackTrace();
		}
        
	}
    
	public ArrayList<SelectItem> getMjeraList() {
		return mjeraList;
	}

	public void setMjeraList(ArrayList<SelectItem> mjeraList) {
		this.mjeraList = mjeraList;
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

	public Certifikat getDb() {
		return db;
	}

	public void setDb(Certifikat db) {
		this.db = db;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public StreamedContent getFileDownload() {
		return fileDownload;
	}

	public void setFileDownload(StreamedContent fileDownload) {
		this.fileDownload = fileDownload;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public StreamedContent getModelDownload() {
		return modelDownload;
	}

	public void setModelDownload(StreamedContent modelDownload) {
		this.modelDownload = modelDownload;
	}
	
	public boolean isCertGenerated() {
		return certGenerated;
	}

	public void setCertGenerated(boolean certGenerated) {
		this.certGenerated = certGenerated;
	}

	public String getKomentar() {
		return komentar;
	}

	public void setKomentar(String komentar) {
		this.komentar = komentar;
	}

	public List<Drzava> getDrzavaList() {
		return drzavaList;
	}

	public void setDrzavaList(List<Drzava> drzavaList) {
		this.drzavaList = drzavaList;
	}

	public Drzava[] getSelectedDrzava() {
		return selectedDrzava;
	}

	public void setSelectedDrzava(Drzava[] selectedDrzava) {
		this.selectedDrzava = selectedDrzava;
	}

	public List<String> getSelectedJezici() {
		return selectedJezici;
	}

	public void setSelectedJezici(List<String> selectedJezici) {
		this.selectedJezici = selectedJezici;
	}

	public Map getJezici() {
		return jezici;
	}

	public void setJezici(Map jezici) {
		this.jezici = jezici;
	}

	public Date getDatumOd() {
		return datumOd;
	}

	public void setDatumOd(Date datumOd) {
		this.datumOd = datumOd;
	}

	public Date getDatumDo() {
		return datumDo;
	}

	public void setDatumDo(Date datumDo) {
		this.datumDo = datumDo;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getSerijskiBroj() {
		return serijskiBroj;
	}

	public void setSerijskiBroj(String serijskiBroj) {
		this.serijskiBroj = serijskiBroj;
	}

	public String getOznakaSerije() {
		return oznakaSerije;
	}

	public void setOznakaSerije(String oznakaSerije) {
		this.oznakaSerije = oznakaSerije;
	}

	public Integer getSelectedMjera() {
		return selectedMjera;
	}

	public void setSelectedMjera(Integer selectedMjera) {
		this.selectedMjera = selectedMjera;
	}

	public Integer getSelectedCertTip() {
		return selectedCertTip;
	}

	public void setSelectedCertTip(Integer selectedCertTip) {
		this.selectedCertTip = selectedCertTip;
	}

	public ArrayList<SelectItem> getCertTipovi() {
		return certTipovi;
	}

	public void setCertTipovi(ArrayList<SelectItem> certTipovi) {
		this.certTipovi = certTipovi;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getIdNewCert() {
		return idNewCert;
	}

	public void setIdNewCert(Integer idNewCert) {
		this.idNewCert = idNewCert;
	}

	public boolean isShowUpload() {
		return showUpload;
	}

	public void setShowUpload(boolean showUpload) {
		this.showUpload = showUpload;
	}

	public String getSelectedDrzavaText() {
		return selectedDrzavaText;
	}

	public void setSelectedDrzavaText(String selectedDrzavaText) {
		this.selectedDrzavaText = selectedDrzavaText;
	}

	public boolean isDisablePregled() {
		return disablePregled;
	}

	public void setDisablePregled(boolean disablePregled) {
		this.disablePregled = disablePregled;
	}
	
}
