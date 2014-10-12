package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.CertifikatPrint;
import hr.kingict.svis.obrasci.web.CertifikatProizvodi;
import hr.kingict.svis.obrasci.web.Farma;
import hr.kingict.svis.obrasci.web.Objekt;
import hr.kingict.svis.obrasci.web.Prod4Cert;
import hr.kingict.svis.obrasci.web.SubjektBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name="approveCert")
@ViewScoped
public class ApproveCertificateFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;
	
	private UploadedFile file;  
	private StreamedContent fileDownload;
	private StreamedContent modelDownload;
	
	private BigDecimal cijena;
	private String komentar;
	private BigDecimal kolicina;
	
	private String filePath;
	private String genIzdaniPath;

	private String serijskiBroj;
	private String oznakaSerije;
	private byte[] model;
	private Integer idInsertDoc;
	
	private Integer km;
	private Integer odobrenaKm;
	private boolean odobreno = false;
	
	private Integer locType;
	
	private boolean isInspektor = false;
	private boolean viewMode = false;
	
	private Farma selFarma;
	private Objekt selObjekt;
	//autocomplete
	private List<SubjektBean> suggestResults = new ArrayList<SubjektBean>();
	
	private String destination="C:\\temp\\";
	
	private Date printDate;
	private CertifikatPrint cert;
	
	private UserBean ub;
	private String org;
	private String veterinar;
	
	private List<CertifikatProizvodi> proizvodiList = new ArrayList<CertifikatProizvodi>();
	private CertifikatProizvodi selectedProizvod;
	
	private ArrayList<SelectItem> mjeraList = new ArrayList<SelectItem>();
	private Integer selectedMjera;
	
	private List<Prod4Cert> listaSelCertProd = new ArrayList<Prod4Cert>();
	
	public ApproveCertificateFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			FacesContext context = FacesContext.getCurrentInstance();
			ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
			cert = (CertifikatPrint) context.getExternalContext().getSessionMap().get("approveCert");
			
			//db fetch
				
			InputStream stream = null;
			if (cert != null){ 
				
				if (trx == null) trx = ctxA.getDatabaseManager().createTransaction("sys");
				
				parm.clear();
				parm.add(cert.getIdCertifikatPrint());
				parm.add(cert.getIdCertifikatPrint());
				rs =  trx.executePreparedQueryById("test", "GetApproveCertificateData", parm);
					
				if (rs.next()){
					printDate = rs.getDate(5);
					org = rs.getString(3);
					veterinar = rs.getString(4);
					serijskiBroj = rs.getString(1);
					locType = rs.getInt(10);
					km = rs.getInt(6);
					odobrenaKm = rs.getInt(7);
					
					if (odobrenaKm != null && odobrenaKm.intValue() > -1){
						viewMode = false;
					}
					else {
						viewMode = true;
						odobrenaKm = 0;
					}
					
					System.out.println("VIEW MODE: " + viewMode);
					
					if (locType.intValue() == 2){
						selObjekt = new Objekt();
						selObjekt.setSubjekt(rs.getString(8));
						selObjekt.setOib(rs.getString(9));
						selObjekt.setVkb(rs.getString(11));
						selObjekt.setAdresa(rs.getString(12) + " " + rs.getString(13) + ", " + rs.getString(14) + " " + rs.getString(15));
					
					}
					else if (locType.intValue() == 1){
						selFarma = new Farma();
						
					}
				}
				
				rs.close();
				
				//proizvodi
				parm.clear();
				parm.add(cert.getIdCertifikatPrint());
				rs =  trx.executePreparedQueryById("test", "GetApproveCertificateProductsData", parm);
				//select t2.id, t2.code, t2.text, t2.id_measure_unit, t3.opis, t1.quantity
				while (rs.next()){
					Prod4Cert p = new Prod4Cert();
					CertifikatProizvodi cp = new CertifikatProizvodi();
					cp.setIdProizvod(rs.getInt(1));
					cp.setKodProizvod(rs.getString(2));
					cp.setNazivProizvod(rs.getString(3));
					p.setProd(cp);
					p.setMjera(rs.getInt(4));
					p.setNazivMjera(rs.getString(5));
					p.setKolicina(rs.getBigDecimal(6));
					listaSelCertProd.add(p);
				}
				
				rs.close();
				
				Date d = new Date();
				SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddkkmmssSSS");  
				String fileName = null;
				filePath = dt.format(d) + ".pdf";
				
				byte[] buf = null;
				
				try {
					
					parm.clear();
					parm.add(cert.getIdCertifikat());
					rs =  trx.executePreparedQueryById("test", "GetCertifikatModel3", parm);
					
					if (rs.next()){
						buf = rs.getBytes(1);
						fileName = rs.getString(2);
						//serijskiBroj = rs.getString(3);
						//oznakaSerije = rs.getString(4);
						
						System.out.println("filename: " + fileName);
						System.out.println("Size: " + buf.length);
					}	
				
					rs.close();
			
					if (buf != null && buf.length > 0){
						FileOutputStream fileOuputStream = new FileOutputStream(destination + filePath); 
						fileOuputStream.write(buf);
						fileOuputStream.close();
						
						//fileName = destination + dt.format(d) + "-" +  fileName;
						stream = new FileInputStream(destination + filePath);//((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/images/optimusprime.jpg");
						fileDownload = new DefaultStreamedContent(stream, "application/pdf", "model.pdf");
						
						model = buf;
					}
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
				
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
	
	public void approve(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		
		try {
			
			if (odobrenaKm == null){
		 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Niste unijeli kilometražu",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
			}
			else {
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				
				parm.add(odobrenaKm);
				parm.add(ub.getIdKorisnik());
				parm.add(cert.getIdCertifikatPrint());
				trx.executePreparedUpdateById("test", "ApproveCertPrinted", parm);
				trx.commit();
				
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Podaci su usješno spremljeni.",  "");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
		        
		        viewMode = false;
			}
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
		}
	}

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificates_printed?faces-redirect=true");
	}
	
	public void reset(ActionEvent event){
		setSelectedProizvod(null);
		setKolicina(null);
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public BigDecimal getCijena() {
		return cijena;
	}

	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
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

	public String getKomentar() {
		return komentar;
	}

	public void setKomentar(String komentar) {
		this.komentar = komentar;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public List<SubjektBean> getSuggestResults() {
		return suggestResults;
	}

	public void setSuggestResults(List<SubjektBean> suggestResults) {
		this.suggestResults = suggestResults;
	}

	public BigDecimal getKolicina() {
		return kolicina;
	}

	public void setKolicina(BigDecimal kolicina) {
		this.kolicina = kolicina;
	}

	public byte[] getModel() {
		return model;
	}

	public void setModel(byte[] model) {
		this.model = model;
	}

	public Date getPrintDate() {
		return printDate;
	}

	public void setPrintDate(Date printDate) {
		this.printDate = printDate;
	}

	public CertifikatPrint getCert() {
		return cert;
	}

	public void setCert(CertifikatPrint cert) {
		this.cert = cert;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public List<CertifikatProizvodi> getProizvodiList() {
		return proizvodiList;
	}

	public void setProizvodiList(List<CertifikatProizvodi> proizvodiList) {
		this.proizvodiList = proizvodiList;
	}

	public CertifikatProizvodi getSelectedProizvod() {
		return selectedProizvod;
	}

	public void setSelectedProizvod(CertifikatProizvodi selectedProizvod) {
		this.selectedProizvod = selectedProizvod;
	}

	public ArrayList<SelectItem> getMjeraList() {
		return mjeraList;
	}

	public void setMjeraList(ArrayList<SelectItem> mjeraList) {
		this.mjeraList = mjeraList;
	}

	public Integer getSelectedMjera() {
		return selectedMjera;
	}

	public void setSelectedMjera(Integer selectedMjera) {
		this.selectedMjera = selectedMjera;
	}

	public List<Prod4Cert> getListaSelCertProd() {
		return listaSelCertProd;
	}

	public void setListaSelCertProd(List<Prod4Cert> listaSelCertProd) {
		this.listaSelCertProd = listaSelCertProd;
	}

	public String getGenIzdaniPath() {
		return genIzdaniPath;
	}

	public void setGenIzdaniPath(String genIzdaniPath) {
		this.genIzdaniPath = genIzdaniPath;
	}

	public Integer getIdInsertDoc() {
		return idInsertDoc;
	}

	public void setIdInsertDoc(Integer idInsertDoc) {
		this.idInsertDoc = idInsertDoc;
	}

	public Integer getKm() {
		return km;
	}

	public void setKm(Integer km) {
		this.km = km;
	}

	public Integer getOdobrenaKm() {
		return odobrenaKm;
	}

	public void setOdobrenaKm(Integer odobrenaKm) {
		this.odobrenaKm = odobrenaKm;
	}

	public boolean isOdobreno() {
		return odobreno;
	}

	public void setOdobreno(boolean odobreno) {
		this.odobreno = odobreno;
	}

	public Integer getLocType() {
		return locType;
	}

	public void setLocType(Integer locType) {
		this.locType = locType;
	}

	public Farma getSelFarma() {
		return selFarma;
	}

	public void setSelFarma(Farma selFarma) {
		this.selFarma = selFarma;
	}

	public Objekt getSelObjekt() {
		return selObjekt;
	}

	public void setSelObjekt(Objekt selObjekt) {
		this.selObjekt = selObjekt;
	}

	public boolean isInspektor() {
		return isInspektor;
	}

	public void setInspektor(boolean isInspektor) {
		this.isInspektor = isInspektor;
	}

	public String getVeterinar() {
		return veterinar;
	}

	public void setVeterinar(String veterinar) {
		this.veterinar = veterinar;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}
	
}
