package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.util.ReportUtil;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.CertifikatJezici;
import hr.kingict.svis.obrasci.web.CertifikatPrint;
import hr.kingict.svis.obrasci.web.Drzava;
import hr.kingict.svis.obrasci.web.Org4Doc;
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

@ManagedBean(name="certPrinted")
@ViewScoped
public class CertificatesPrintedFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<CertifikatPrint> lazyModel;
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
	private String selectedJeziciText;
	private String selectedDrzavaText;
	
	private List<Org4Doc> orgList = new ArrayList<Org4Doc>();
	private Org4Doc selectedOrg;
	
	private Date datumOd;
	private Date datumDo;
	
	private boolean rolaUprava = false; //provjeriti rolu
	private boolean rolaInspektor = false;
	private int rola;
	private UserBean ub;
	
	private Boolean isAdmin;
	
	private StreamedContent fileDownload;
	private StreamedContent fileDownload2;
	
	public CertificatesPrintedFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			isAdmin = (Boolean) context.getExternalContext().getSessionMap().get("certadmin");

			if (rola != 1200 && rola != 1227 && !isAdmin && rola != 2 && rola != 12 && rola != Const.ROLA_DVI.intValue() && rola != Const.ROLA_INSPEKTOR.intValue()){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				
				if (rola == Const.ROLA_DVI.intValue() || rola == Const.ROLA_INSPEKTOR.intValue()){
					rolaInspektor = true;
				}

				ub = (UserBean) context.getExternalContext().getSessionMap().get("user");

	    		trx = ctxA.getDatabaseManager().createTransaction("sys");
	    		
	    		lazyModel = new LazyCertPrintedDataModel(false);
				
				//mjere
				parm.clear();
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
				
				selectedJezici = new CertifikatJezici[]{};
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
		selectedMjera.clear();
		setSelectedDrzava(new Drzava[]{});
		selectedJezici = new CertifikatJezici[]{};
		setSelectedJeziciText("");
		setSelectedDrzavaText("");
		setDatumOd(null);
		setDatumDo(null);
		setSelectedOrg(new Org4Doc());
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
	
	
	public void search(){

		try {

			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("kod", getKod() == null ? Const.BLANK : SearchUtil.testIfContainsMode(getKod()));
			map.put("naziv", getNaziv() == null ? Const.BLANK : SearchUtil.testIfContainsMode(naziv));
			
			//map.put("selectedDrzava", getSelectedDrzava() == null ? Const.BLANK : SearchUtil.convertNumber(getSelectedDrzava().getIdDrzava()));
			
		    SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
			
			map.put("datumOd", getDatumOd() == null ? format.format(Const.START_DATE) : format.format(getDatumOd()));
			map.put("datumDo", getDatumDo() == null ? format.format(new Date()) : format.format(getDatumDo()));
			
			map.put("selectedOrg", getSelectedOrg() == null ? Const.BLANK : SearchUtil.convertNumber(getSelectedOrg().getIdOrganizacija()));
			
			lazyModel = new LazyCertPrintedDataModel(map, selectedMjera, selectedJezici, selectedDrzava, false);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public String getDetail(){  
	 	
		try {

			FacesContext context = FacesContext.getCurrentInstance();
			
			for (CertifikatPrint o : (List<CertifikatPrint>) lazyModel.getWrappedData()){

				if (o.getIdCertifikatPrint().intValue() == this.selected.intValue()){
					context.getExternalContext().getSessionMap().put("approveCert", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificate_approve?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void delete(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		List<CertifikatPrint> certList = (List<CertifikatPrint>) lazyModel.getWrappedData();
		
		try {
			for (int i=0; i<certList.size(); i++){
				System.out.println(certList.get(i).getIdCertifikat());
				if (certList.get(i).getIdCertifikat().intValue() == getSelected().intValue()){
					certList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "DeleteDoc", parm);
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
	
	public void issue(){
		FacesContext context = FacesContext.getCurrentInstance();
		
		for (CertifikatPrint cert : (List<CertifikatPrint>) lazyModel.getWrappedData()){
			if (cert.getIdCertifikat().intValue() == getSelected().intValue()){
				context.getExternalContext().getSessionMap().put("issueCert", cert);
				break;
			}
		}
		
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "certificate_issue?faces-redirect=true");
	}
	
	public void generateExcel(){
		String template = "report_izdani";
		String output_name = "izdani_certifikati";

		try {
			Map<Integer, Object> map = new HashMap<Integer, Object>();
			if (rola != 1200 || rola != 1227 ) map.put(1, ub.getIdOrganizacije());
			else map.put(1, Const.BLANK);
			fileDownload2 = ReportUtil.generateExcelReportAdvanced("ReportIzdaniCertifikati", template, output_name, map);
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void generateReportCertPrinted(){
		String template = "izdani_certifikati";
		String output_name = "izvjesce_izdani_cert";
		
		try {
	
			Map<Integer, Object> map = new HashMap<Integer, Object>();
			map.put(1, Const.BLANK);
			map.put(2, Const.BLANK);
			fileDownload = ReportUtil.generateExcelReportAdvanced("GetIzdani4Report", template, output_name, map);
		}	
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleClose() {
		System.out.println("test row: ");
	}

	public void handleClose2() {
		System.out.println("test row: ");
	}

	public LazyDataModel<CertifikatPrint> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<CertifikatPrint> lazyModel) {
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

	public boolean isRolaUprava() {
		return rolaUprava;
	}

	public void setRolaUprava(boolean rolaUprava) {
		this.rolaUprava = rolaUprava;
	}

	public List<Org4Doc> getOrgList() {
		return orgList;
	}

	public void setOrgList(List<Org4Doc> orgList) {
		this.orgList = orgList;
	}

	public Org4Doc getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(Org4Doc selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public String getSelectedJeziciText() {
		return selectedJeziciText;
	}

	public void setSelectedJeziciText(String selectedJeziciText) {
		this.selectedJeziciText = selectedJeziciText;
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

	public StreamedContent getFileDownload() {
		return fileDownload;
	}

	public void setFileDownload(StreamedContent fileDownload) {
		this.fileDownload = fileDownload;
	}
	
	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public StreamedContent getFileDownload2() {
		return fileDownload2;
	}

	public void setFileDownload2(StreamedContent fileDownload2) {
		this.fileDownload2 = fileDownload2;
	}

	public boolean isRolaInspektor() {
		return rolaInspektor;
	}

	public void setRolaInspektor(boolean rolaInspektor) {
		this.rolaInspektor = rolaInspektor;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
