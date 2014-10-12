package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.util.ReportUtil;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.CertifikatJezici;
import hr.kingict.svis.obrasci.web.Drzava;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

@ManagedBean(name="certSif")
@ViewScoped
public class CertificatesFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Certifikat> lazyModel;
	private Integer selected;
	
	//search params
	private String kod;
	private String naziv;
	
	private List<String> selectedMjera = new ArrayList<String>();
	private Map mjere = new HashMap<String, String>();
	
	private List<Drzava> drzavaList = new ArrayList<Drzava>();
	private Drzava[] selectedDrzava = new Drzava[]{};
	
	private List<CertifikatJezici> jeziciList = new ArrayList<CertifikatJezici>();
	private CertifikatJezici[] selectedJezici = new CertifikatJezici[]{};
	
	private Date datumOd;
	private Date datumDo;
	
	private Integer podrucje;
	private Integer aktivnost;
	
	private int rola; //provjeriti rolu
	private UserBean ub;
	private Boolean rolaUprava = false;
	
	private Boolean isAdmin;
	
	private String selectedJeziciText;
	private String selectedDrzavaText;
	
	private StreamedContent fileDownload;
	
	public CertificatesFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			isAdmin = (Boolean) context.getExternalContext().getSessionMap().get("certadmin");
			
			System.out.println("IS CERT ADMIN" + isAdmin);

			if (rola != 1200 && rola != 1227 && rola != 1 && rola != 12 && !isAdmin){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();
				
		    	UserBean ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
				this.ub = ub;
				
				if (rola == 1200 || rola == 1227 || isAdmin) rolaUprava = true;
				else aktivnost = new Integer(1);
    		
				lazyModel = new LazyCertificatesDataModel();
				
				//mjere
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "SelectCertifikatMjera", parm);
				
				while (rs.next()){
					mjere.put(rs.getString(2), rs.getString(1));
				}
				
				rs.close();
				rs =  trx.executePreparedQueryById("test", "SelectLanguages", parm);
				
				while (rs.next()){
					CertifikatJezici c = new CertifikatJezici();
					c.setIdJezik(rs.getInt(1));
					c.setNazivJezik(rs.getString(2));
					jeziciList.add(c);
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

	public void clear(ActionEvent event){
		setKod("");
		setNaziv("");
		setSelectedJeziciText("");
		setSelectedDrzavaText("");
		setSelectedMjera(new ArrayList<String>());
		setSelectedDrzava(new Drzava[]{});
		setSelectedJezici(new CertifikatJezici[]{});
		setDatumOd(null);
		setDatumDo(null);
		setAktivnost(new Integer(1));
		setPodrucje(null);
	}
	
	public void add(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().remove("certSif");
		
		context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_certificate?faces-redirect=true");
	}
	
	public void generateExcel(){
		String template = "report_certifikati";
		String output_name = "certifikati";

		try {
			Map<Integer, Object> map = new HashMap<Integer, Object>();
	
			fileDownload = ReportUtil.generateExcelReportAdvanced("ReportCertifikati", template, output_name, map);
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleClose() {
		System.out.println("test row: ");
	}

	public void handleClose2() {
		System.out.println("test row: ");
	}
	
	public void search(){
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("kod", getKod() == null ? Const.BLANK : SearchUtil.testIfContainsMode(getKod()));
			map.put("naziv", getNaziv() == null ? Const.BLANK : SearchUtil.testIfContainsMode(naziv));
			
			//map.put("selectedDrzava", getSelectedDrzava() == null ? Const.BLANK : SearchUtil.convertNumber(getSelectedDrzava().getIdDrzava()));
			
		    SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
			
			map.put("datumOd", getDatumOd() == null ? format.format(Const.START_DATE) : format.format(getDatumOd()));
			map.put("datumDo", getDatumDo() == null ? format.format(new Date()) : format.format(getDatumDo()));
			
			map.put("podrucje", getPodrucje() == null  ? new Integer(0) : getPodrucje() );
			map.put("aktivnost", getAktivnost());
			
			lazyModel = new LazyCertificatesDataModel(map, selectedMjera, selectedJezici, selectedDrzava);	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public String getDetail(){  
	 	
		try {

			FacesContext context = FacesContext.getCurrentInstance();
			
			for (Certifikat o : (List<Certifikat>) lazyModel.getWrappedData()){

				if (o.getIdCertifikat().intValue() == this.selected.intValue()){
					context.getExternalContext().getSessionMap().put("certSif", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_certificate?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSelected());
	}
	
	public void delete(ActionEvent event){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		List<Certifikat> certList = (List<Certifikat>) lazyModel.getWrappedData();
		
		try {
			
			for (int i=0; i<certList.size(); i++){
			
				if (certList.get(i).getIdCertifikat().intValue() == getSelected().intValue()){
					certList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(ub.getIdKorisnik());
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteCert", parm);
					trx.commit();
					
					break;
				}
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
	
	public void selectLang(ActionEvent event){
		String tmp = "";
		
		for (CertifikatJezici p : selectedJezici){
			System.out.println(p.getIdJezik() + " - " + p.getNazivJezik());
			
			tmp += p.getNazivJezik() + ", ";
		}
		tmp = tmp.substring(0, tmp.lastIndexOf(","));
		setSelectedJeziciText(tmp);
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
	
	public void issue(){
		FacesContext context = FacesContext.getCurrentInstance();
		
		for (Certifikat cert : (List<Certifikat>) lazyModel.getWrappedData()){
			if (cert.getIdCertifikat().intValue() == getSelected().intValue()){
				context.getExternalContext().getSessionMap().put("issueCert", cert);
				break;
			}
		}
		
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificate_issue?faces-redirect=true");
	}

	public LazyDataModel<Certifikat> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Certifikat> lazyModel) {
		this.lazyModel = lazyModel;
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

	public List<String> getSelectedMjera() {
		return selectedMjera;
	}

	public void setSelectedMjera(List<String> selectedMjera) {
		this.selectedMjera = selectedMjera;
	}

	public Map getMjere() {
		return mjere;
	}

	public void setMjere(Map mjere) {
		this.mjere = mjere;
	}

	public List<Drzava> getDrzavaList() {
		return drzavaList;
	}

	public void setDrzavaList(List<Drzava> drzavaList) {
		this.drzavaList = drzavaList;
	}

	public List<CertifikatJezici> getJeziciList() {
		return jeziciList;
	}

	public void setJeziciList(List<CertifikatJezici> jeziciList) {
		this.jeziciList = jeziciList;
	}

	public CertifikatJezici[] getSelectedJezici() {
		return selectedJezici;
	}

	public void setSelectedJezici(CertifikatJezici[] selectedJezici) {
		this.selectedJezici = selectedJezici;
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
	
	public Drzava[] getSelectedDrzava() {
		return selectedDrzava;
	}

	public void setSelectedDrzava(Drzava[] selectedDrzava) {
		this.selectedDrzava = selectedDrzava;
	}

	public String getSelectedDrzavaText() {
		return selectedDrzavaText;
	}

	public void setSelectedDrzavaText(String selectedDrzavaText) {
		this.selectedDrzavaText = selectedDrzavaText;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public Boolean getRolaUprava() {
		return rolaUprava;
	}

	public void setRolaUprava(Boolean rolaUprava) {
		this.rolaUprava = rolaUprava;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public String getSelectedJeziciText() {
		return selectedJeziciText;
	}

	public void setSelectedJeziciText(String selectedJeziciText) {
		this.selectedJeziciText = selectedJeziciText;
	}

	public StreamedContent getFileDownload() {
		return fileDownload;
	}

	public void setFileDownload(StreamedContent fileDownload) {
		this.fileDownload = fileDownload;
	}

	public Integer getPodrucje() {
		return podrucje;
	}

	public void setPodrucje(Integer podrucje) {
		this.podrucje = podrucje;
	}

	public Integer getAktivnost() {
		return aktivnost;
	}

	public void setAktivnost(Integer aktivnost) {
		this.aktivnost = aktivnost;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
}
