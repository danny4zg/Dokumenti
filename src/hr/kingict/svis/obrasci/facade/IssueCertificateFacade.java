package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.CertifikatProizvodi;
import hr.kingict.svis.obrasci.web.Farma;
import hr.kingict.svis.obrasci.web.Objekt;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.Prod4Cert;
import hr.kingict.svis.obrasci.web.SubjektBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.BLOB;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.ibm.ws.rsadapter.jdbc.WSJdbcConnection;
import com.ibm.ws.rsadapter.jdbc.WSJdbcUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

@ManagedBean(name="issueCert")
@ViewScoped
public class IssueCertificateFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer selected;
	
	private UploadedFile file;  
	private StreamedContent fileDownload;
	private StreamedContent modelDownload;
	private StreamedContent certDownload;
	private String downloadPath;
	
	private BigDecimal cijena;
	private String komentar;
	private BigDecimal kolicina;
	
	private String filePath;
	private String genIzdaniPath;

	private String serijskiBroj;
	private String oznakaSerije;
	private byte[] model;
	private Integer idInsertDoc;
	
	private boolean certGenerated = false;
	private boolean modeSavedCert = false;
	private boolean modeStorno = false;
	private boolean modeIzrada = false;
	
	//
	private Integer km;
	private Integer odobrenaKm;
	private boolean odobreno = false;
	
	private Integer locType;
	
	private Farma selFarma;
	private Objekt selObjekt;
	//autocomplete
	private List<SubjektBean> suggestResults = new ArrayList<SubjektBean>();

	private SubjektBean autoComplete;//id od "autocomplete"
	
	private String destination="C:\\temp\\";
	
	private Date printDate;
	private Certifikat cert;
	
	private UserBean ub;
	private OrganizationBean org = new OrganizationBean();
	
	private List<CertifikatProizvodi> proizvodiList = new ArrayList<CertifikatProizvodi>();
	private CertifikatProizvodi selectedProizvod;
	
	private ArrayList<SelectItem> mjeraList = new ArrayList<SelectItem>();
	private Integer selectedMjera;
	
	private List<Prod4Cert> listaSelCertProd = new ArrayList<Prod4Cert>();
	
	public IssueCertificateFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			FacesContext context = FacesContext.getCurrentInstance();
			ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
			cert = (Certifikat) context.getExternalContext().getSessionMap().get("issueCert");
			
			printDate = new Date();
			selectedProizvod = new CertifikatProizvodi();
			
			autoComplete = new SubjektBean();
			locType = 2;
			km = 0;
			
			InputStream stream = null;
			if (cert != null){ 
				
				Date d = new Date();
				SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddkkmmssSSS");  
				String fileName = null;
				filePath = dt.format(d) + ".pdf";
				
				byte[] buf = null;
				
				try {
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					parm.clear();
					parm.add(cert.getIdCertifikat());
					rs =  trx.executePreparedQueryById("test", "GetCertifikatModel3", parm);
					
					if (rs.next()){
						buf = rs.getBytes(1);
						fileName = rs.getString(2);
						serijskiBroj = rs.getString(3);
						oznakaSerije = rs.getString(4);
					}	
				
					rs.close();
					
					if (buf == null || buf.length == 0) {
			            System.out.println("Nema modela certifikata!");
						setCertGenerated(false);
						setModeIzrada(true);
						
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Model certifikata je prazan.",  "Učitajte model");  
			            FacesContext.getCurrentInstance().addMessage(null, message);
			    	}
			
					if (buf != null){
						
						FileOutputStream fileOuputStream = new FileOutputStream(destination + filePath); 
						fileOuputStream.write(buf);
						fileOuputStream.close();
						
						stream = new FileInputStream(destination + filePath);
						fileDownload = new DefaultStreamedContent(stream, "application/pdf", "model.pdf");
						
						model = buf;
						
						parm.clear();
						parm.add(ub.getIdOrganizacije());
						rs =  trx.executePreparedQueryById("test", "SelectNarucitelj2", parm);
						
						if (rs.next()){
							org.setIdOrganization(rs.getInt(1));
							org.setName(rs.getString(2));
							org.setAddress(rs.getString(3) + " " + rs.getString(4));
							org.setTel(rs.getString(5));
							org.setFax(rs.getString(6));
							org.setEmail(rs.getString(7));
							org.setOib(rs.getString(9));
						}
						
						rs.close();
						
						parm.clear();
						rs =  trx.executePreparedQueryById("test", "SelectCertifikatProizvodi", parm);
						
						while (rs.next()){
							CertifikatProizvodi p = new CertifikatProizvodi();
							p.setIdProizvod(rs.getInt(1));
							p.setKodProizvod(rs.getString(2));
							p.setNazivProizvod(rs.getString(3));
							proizvodiList.add(p);
							
							if (cert.getTip().intValue() == rs.getInt(1)){
								selectedProizvod.setIdProizvod(rs.getInt(1));
								selectedProizvod.setKodProizvod(rs.getString(2));
								selectedProizvod.setNazivProizvod(rs.getString(3));
							}
						}
						
						rs.close();
						
						rs =  trx.executePreparedQueryById("test", "SelectProizvodMjera", parm);
						
						while (rs.next()){
							mjeraList.add(new SelectItem(rs.getInt(1), rs.getString(2)));
						}
						
						rs.close();
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
	
	private String generateSerialNumber(Integer counter){
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
			 setSerijskiBroj(genSerialNum);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return genSerialNum;
	}

	public void handleClose() {
		System.out.println("test row: ");
	}

	public void handleClose2() {
		System.out.println("test row: ");	
	}
	
	public void handleSelect1() {
		autoComplete.setIdSubjekt(selFarma.getIdLocation());
		autoComplete.setNazivSubjekt(selFarma.getNaziv() == null || "".equals(selFarma.getNaziv()) ? selFarma.getIme() + " " + selFarma.getPrezime(): selFarma.getNaziv());
		autoComplete.setOib(selFarma.getOib());
		autoComplete.setAdresaSubjekt(selFarma.getUlica() + " " + selFarma.getKbr() + "\n" + selFarma.getPbr() + " " + selFarma.getMjesto());
		autoComplete.setDrzavaSubjekt("Hrvatska");
	}
	
	public void handleSelect2() {
		autoComplete.setIdSubjekt(selObjekt.getIdSubjekt());
		autoComplete.setNazivSubjekt(selObjekt.getSubjekt());
		autoComplete.setOib(selObjekt.getOib());
		autoComplete.setAdresaSubjekt(selObjekt.getAdresa() + "\n" + selObjekt.getMjesto());
		autoComplete.setDrzavaSubjekt("Hrvatska");
	}
	
	public void changeType(){
		autoComplete = new SubjektBean();
	}
	
	public void handleSelectAutoComplete(SelectEvent event) {  
	    System.out.println("cupam subjekta: " + ((SubjektBean) event.getObject()).getNazivSubjekt());
	    System.out.println("autocomplete: " + autoComplete.getAdresaSubjekt() );
	} 

	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificates?faces-redirect=true");
	}
	
	public void reset(ActionEvent event){
		setSelectedProizvod(null);
		setKolicina(null);
	}
	
	public void save(ActionEvent event){	
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			
			if (listaSelCertProd.size() == 0 || autoComplete.getIdSubjekt() == null || getKm() == null){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Niste unijeli sve potrebne podatke.",  "Niste unijeli sve potrebne podatke.");  
	            FacesContext.getCurrentInstance().addMessage(null, message);
				
				return;
			}
			
			setModeSavedCert(true);
			
			trx = ctxA.getDatabaseManager().createTransaction("sys");
        	rs =  trx.executePreparedQueryById("test", "GetSequenceIzdaniCert", parm);
			
        	if (rs.next()){
        		idInsertDoc = rs.getInt(1);
        		generateSerialNumber(prepareCertData() - 1);
        	}
        	
        	rs.close();
        	
        	parm.add(idInsertDoc);
			parm.add(cert.getIdCertifikat());
			parm.add(serijskiBroj);
			parm.add(ub.getIdOrganizacije());
			parm.add(ub.getIdKorisnik());
			parm.add(autoComplete.getIdSubjekt());
			//status
			parm.add(new String("1"));
			parm.add(ub.getIdKorisnik());
			parm.add(getKm());
			parm.add(getLocType());
			parm.add(getSelObjekt() != null ? getSelObjekt().getVkb() : null);
			parm.add(getSelFarma() != null ? getSelFarma().getJibg() : null);
			
			trx.executePreparedUpdateById("test", "InsertIzdaniCertifikat", parm);
			trx.commit();
			
			for (Prod4Cert p : listaSelCertProd){
				Integer seqIdDocProd = null;
				
				parm.clear();
				rs =  trx.executePreparedQueryById("test", "GetSequenceProizvodIzdaniCert", parm);
				
	        	if (rs.next()){
	        		seqIdDocProd = rs.getInt(1);
	        	}
	        	
	        	rs.close();

				parm.add(seqIdDocProd);
				parm.add(idInsertDoc);
				parm.add(p.getProd().getIdProizvod());
				parm.add(p.getMjera());
				parm.add(p.getKolicina());
				
				trx.executePreparedUpdateById("test", "InsertProizvodIzdaniCert", parm);
				trx.commit();
			}

		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
        	if (trx!= null)
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
	
	public void storno(ActionEvent event){
		ITransaction trx = null;
		List parm = new ArrayList();
		
		   
        try {
        	setModeStorno(true);
        	
        	trx = ctxA.getDatabaseManager().createTransaction("sys");
        	//update za idGenDoc i doc i doc_prod
        	parm.add(new String("0"));
        	parm.add(idInsertDoc);
			
			trx.executePreparedUpdateById("test", "StornoIzdaniCert", parm);
			trx.commit();
        	
        	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspješno stornirano",  "");  
            FacesContext.getCurrentInstance().addMessage(null, message);

        }
        catch (Exception e){
			e.printStackTrace();
		}
        finally{
        	if (trx!= null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
	}
	
	public void prepareCertificateToDownload(ActionEvent event){
		File file = new File(getDownloadPath());
        InputStream stream = null; 
	     
        try {     	
        	System.out.println("PREPARING MODEL 4 DOWNLOAD...");
        	System.out.println("PATH: " + destination + genIzdaniPath);
        	System.out.println(getDownloadPath());
            
            stream = new FileInputStream(file);
            this.setCertDownload(new DefaultStreamedContent(stream, "application/pdf", "model.pdf"));
        }
        catch (Exception e){
			e.printStackTrace();
		}
        
	}
	
	public void prepareModelToDownload(ActionEvent event){
		File file = new File(destination + filePath);
        InputStream stream = null; 
	     
        try {     	
        	System.out.println("PREPARING MODEL 4 DOWNLOAD...");
        	System.out.println("PATH: " + destination + filePath);
            
            stream = new FileInputStream(file);
            this.setFileDownload(new DefaultStreamedContent(stream, "application/pdf", "model.pdf"));
        }
        catch (Exception e){
			e.printStackTrace();
		}
        
	}
	
	private int getNextId(int length){	
		int maxId = 0;
		
		for (int i=0; i < length; i++){
			if (listaSelCertProd.get(i).getId() >= maxId){
				maxId = listaSelCertProd.get(i).getId();
            }
		}
		
		return maxId+1;
	}
	
	public void delete(){
		int brojac = 0;
		for (Prod4Cert item: listaSelCertProd){
			if (item.getId().intValue() == this.selected.intValue()){
				break;
			}
			brojac++;
		}
		
		listaSelCertProd.remove(brojac);
	}
	
	public void add(ActionEvent event){
		
		try {
			Prod4Cert p = new Prod4Cert();
			p.setId(getNextId(listaSelCertProd.size()));
			p.setKolicina(kolicina);
			p.setMjera(selectedMjera);
			
			for(SelectItem s: mjeraList){
				if (selectedMjera.intValue() == ((Integer) s.getValue()).intValue()){
					System.out.println("MJERA: " + s.getLabel());
					p.setNazivMjera(s.getLabel());
					break;
				}
			}
			
			CertifikatProizvodi cp = new CertifikatProizvodi();
			for (CertifikatProizvodi s : proizvodiList){
				if (selectedProizvod.getIdProizvod().intValue() == s.getIdProizvod().intValue()){
					cp = s;
					break;
				}
			}
			p.setProd(cp);
			listaSelCertProd.add(p);
			System.out.println("ADDED PROIZVOD: " + p.getProd().getNazivProizvod());
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private StreamedContent updatePdf(byte[] dok, String fileName, String oznaka, String broj) {
        String path = destination + fileName;
        StreamedContent fileDownload = null;
        
        try {
            
            final PdfReader pdfReader = new PdfReader(dok);  
            final Document document = new Document();
            final FileOutputStream outputStream = new FileOutputStream(path);
            final PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
 
            document.open();
            final PdfContentByte cb = pdfWriter.getDirectContent();
            
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                document.newPage();
    
                Font font=new Font();
                font.setFamily("Helvetica");
                font.setSize(6);
                
                Paragraph p1=new Paragraph(oznaka,font);   
                p1.setIndentationRight(30); 
                p1.setAlignment(Element.ALIGN_RIGHT);
                
                Paragraph p2=new Paragraph("No."+broj,font);
                p2.setIndentationRight(30);
                p2.setAlignment(Element.ALIGN_RIGHT);
              
                document.add(p1);
                document.add(p2);

                final PdfImportedPage importedPage = pdfWriter.getImportedPage(pdfReader, i);
                cb.addTemplate(importedPage, 0, 0);
            }
            
            //document.newPage();
    
            outputStream.flush();
            document.close();
            pdfReader.close();
            
            InputStream stream = new FileInputStream(destination + fileName);
            fileDownload = new DefaultStreamedContent(stream, "application/pdf", "certifikat.pdf");
            
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
            e.printStackTrace();
        } catch (final DocumentException e) {
            e.printStackTrace();
        }
        
        return fileDownload;
    }  
	
	public void postaviKm(){
		System.out.println("Kilometraza je : " + getKm());
	}
	
    public void izdajCertifikat(ActionEvent event){
    	ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		InputStream in = null;
		
		try {
				
			SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddkkmmssSSS");  
			String fileName = null;
			
			if (modeStorno){ 
				System.out.println("Priprema podataka za certifikat zbog storniranja...");
				//spremi u bazu
				trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	rs =  trx.executePreparedQueryById("test", "GetSequenceIzdaniCert", parm);
				
	        	if (rs.next()){
	        		idInsertDoc = rs.getInt(1);
	        		generateSerialNumber(prepareCertData() - 1);
	        	}
	        	
	        	rs.close();
				
				parm.add(idInsertDoc);
				parm.add(cert.getIdCertifikat());
				parm.add(serijskiBroj);
				parm.add(ub.getIdOrganizacije());
				parm.add(ub.getIdKorisnik());
				parm.add(autoComplete.getIdSubjekt());
				//status
				parm.add(new String("1"));
				parm.add(ub.getIdKorisnik());
				parm.add(getKm());
				parm.add(getLocType());
				parm.add(getSelObjekt() != null ? getSelObjekt().getVkb() : null);
				parm.add(getSelFarma() != null ? getSelFarma().getJibg() : null);
				
				trx.executePreparedUpdateById("test", "InsertIzdaniCertifikat", parm);
				trx.commit();
				
				for (Prod4Cert p : listaSelCertProd){
					Integer seqIdDocProd = null;
					
					parm.clear();
					rs =  trx.executePreparedQueryById("test", "GetSequenceProizvodIzdaniCert", parm);
					
		        	if (rs.next()){
		        		seqIdDocProd = rs.getInt(1);
		        	}
		        	
		        	rs.close();

					parm.add(seqIdDocProd);
					parm.add(idInsertDoc);
					parm.add(p.getProd().getIdProizvod());
					parm.add(p.getMjera());
					parm.add(p.getKolicina());
					
					trx.executePreparedUpdateById("test", "InsertProizvodIzdaniCert", parm);
					trx.commit();
				}
				
				modeStorno = false;
			}
			
			fileName = "model-" + dt.format(new Date()) + ".pdf";
			genIzdaniPath = fileName;
			
			//provjera serijskog broja

			System.out.println("PATH: " + destination + genIzdaniPath);
			modelDownload = updatePdf(model, genIzdaniPath, oznakaSerije, serijskiBroj);
			
			setCertGenerated(true);
			
			try {
				
				long ts1 = System.currentTimeMillis();
				
	            in = modelDownload.getStream();
	        	byte[] dokument = IOUtils.toByteArray(in); 
								      	
	        	javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
	        	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
	        	Connection conn = ds.getConnection(); 
	        	
	        	OracleConnection oracleConn = (OracleConnection) WSJdbcUtil.getNativeConnection((WSJdbcConnection) conn);  
	        	oracleConn.setAutoCommit(false);
	        		        	
	        	PreparedStatement pstmt = null;
	        	
	        	BLOB blob;
	        	String cmd = "SELECT pdf FROM vis_ex.cert_documents WHERE id_document = ? FOR UPDATE";
	        	pstmt = oracleConn.prepareStatement(cmd);
	        	pstmt.setInt(1, idInsertDoc);
	        	ResultSet rset = pstmt.executeQuery();
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
	        	oracleConn.commit();
	        		        	
	        	pstmt.close();
	        	
	        	//oracleConn.close();
	        	conn.close();
	        	
	        	System.out.println("Generirani certifikat spremljen u bazi");
	        	
	        	FileOutputStream fileOuputStream = new FileOutputStream(destination + genIzdaniPath); 
				fileOuputStream.write(dokument);
				fileOuputStream.close();
				
				System.out.println("Generirani certifikat spremljen u temp direktorij...");
	        	
				InputStream stream = new FileInputStream(destination + genIzdaniPath);
				setDownloadPath(destination + genIzdaniPath);
				
	        	certDownload = new DefaultStreamedContent(stream, "application/pdf", "model.pdf");
				long ts2 = System.currentTimeMillis();
				System.out.println("\n"+ (ts2 - ts1) +" ms" );      
			}
			catch (Exception e){
				e.printStackTrace();
			} finally {
			    IOUtils.closeQuietly(in);
			}
			
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
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
    
    private int prepareCertData(){
    	int param2 = 0;
    	
    	try {
    	
	    	javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
	    	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
	    	
	    	Connection conn = ds.getConnection(); 
	    
	    	String proc3StoredProcedure = "{ call vis_ex.cert_issue_serial(?, ?) }";
	       
	        CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
	    	
	        System.out.println("CERT-ID: " + cert.getIdCertifikat());
	        
	        cs.setInt(1, cert.getIdCertifikat().intValue());
	        cs.registerOutParameter(2, java.sql.Types.INTEGER);
	        cs.execute();
	        
	        param2 = cs.getInt(2);
	        System.out.println("serijski broj sufix: " + param2);
	        
	        conn.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	return param2;
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

	public SubjektBean getAutoComplete() {
		return autoComplete;
	}

	public void setAutoComplete(SubjektBean autoComplete) {
		this.autoComplete = autoComplete;
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

	public Certifikat getCert() {
		return cert;
	}

	public void setCert(Certifikat cert) {
		this.cert = cert;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public OrganizationBean getOrg() {
		return org;
	}

	public void setOrg(OrganizationBean org) {
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

	public boolean isModeSavedCert() {
		return modeSavedCert;
	}

	public void setModeSavedCert(boolean modeSavedCert) {
		this.modeSavedCert = modeSavedCert;
	}

	public boolean isModeStorno() {
		return modeStorno;
	}

	public void setModeStorno(boolean modeStorno) {
		this.modeStorno = modeStorno;
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

	public boolean isModeIzrada() {
		return modeIzrada;
	}

	public void setModeIzrada(boolean modeIzrada) {
		this.modeIzrada = modeIzrada;
	}

	public StreamedContent getCertDownload() {
		return certDownload;
	}

	public void setCertDownload(StreamedContent certDownload) {
		this.certDownload = certDownload;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}
	
	
}
