package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.util.ReportUtil;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.Assign2Bean;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.Objekt;
import hr.kingict.svis.obrasci.web.Org4Doc;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;

@ManagedBean(name="orderSearch")
@ViewScoped
public class OrderSearchFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Assign2Bean> lazyModel;
	private Integer selected;
	
	//search params
	private Integer id;
	private DocumentBean selDoc;
	private Org4Doc selNarucitelj;
	private Org4Doc selDobavljac;
	private Objekt selObjekt;
	private Date datumOd;
	private Date datumDo;
	
	private UserBean ub;
	private Boolean rolaUprava = false;
	
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	private LazyDataModel<Org4Doc> lazyOrgList;
	private LazyDataModel<Org4Doc> lazyOrgListDobavljac;
	
	private List<String> selectedStatuses = new ArrayList<String>();
	private List<String> allStatuses = new ArrayList<String>();
	
	private Map statuses = new HashMap<String, Integer>();
	
	private int rola;
	private int activeTab = Const.CODE_DEFAULT;
	private Boolean search;
	
	private StreamedContent fileDownload;
	private StreamedContent fileDownload2;
	
	public OrderSearchFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 1228 && rola != 1226 && rola != 2 && rola != 12 && rola != 3 && rola != 5){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				if (rola == 1226) rolaUprava = true;
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				
				selDoc = new DocumentBean();
				selNarucitelj = new Org4Doc();
				selDobavljac = new Org4Doc();
				selObjekt = new Objekt();
	    		
				lazyModel = new LazyOrderSearchDataModelProdukcija();
				
				//upiti za statuse
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "SelectStatuses", parm);
				while (rs.next()){
					statuses.put(rs.getString(2), rs.getString(1));
					selectedStatuses.add(rs.getString(1));
					allStatuses.add(rs.getString(1));
				}
				rs.close();
				
				//upiti za lookup
				rs =  trx.executePreparedQueryById("test", "SelectAllDokumenti", parm);
				while (rs.next()){
					DocumentBean doc = new DocumentBean();
					doc.setIdDocument(rs.getInt(1));
					doc.setCode(rs.getString(2));
					doc.setDesc(rs.getString(3));
					doc.setIdUnit(rs.getInt(4));
					doc.setUnit(rs.getString(5));
					doc.setPrice(rs.getBigDecimal(6));
					
					docList.add(doc);
				}
			
				this.ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
	
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
	
	public void onTabChange(TabChangeEvent event) {  
        setActiveTab(event.getTab().getTitle().equals(Const.TEXT_ORG) ? Const.CODE_ORG.intValue() : event.getTab().getTitle().equals(Const.TEXT_OBJEKT) ? Const.CODE_OBJEKT.intValue() : Const.CODE_DEFAULT.intValue());
        
        if (this.activeTab == 1) setSelObjekt(new Objekt());
        else if (this.activeTab == 2) setSelNarucitelj(new Org4Doc());
	} 
	
	public void setParam(){
		System.out.println("set param");
		System.out.println("set param: " + getSelected());
	}
	
	public void storno(ActionEvent event){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
		
		List<Assign2Bean> ordList = (List<Assign2Bean>) lazyModel.getWrappedData();
		
		try {
			
			for (int i=0; i<ordList.size(); i++){
			
				if (ordList.get(i).getIdStavkaNarudzbe().intValue() == getSelected().intValue()){
					ordList.remove(i);
			
					trx = ctxA.getDatabaseManager().createTransaction("sys");
					
					parm.add(Const.STORNO);
					parm.add(getSelected());
					trx.executePreparedUpdateById("test", "ChangeStatusItem", parm);
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
	
	public void clear(ActionEvent event){	
		setSelNarucitelj(new Org4Doc());
		setSelDobavljac(new Org4Doc());
		setSelDoc(new DocumentBean());
		setSelObjekt(new Objekt());
		setId(null);
		setDatumOd(null);
		setDatumDo(null);
		setSelectedStatuses(allStatuses);	
		setActiveTab(Const.CODE_DEFAULT.intValue());
	}
	
	public void search(){
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("selDoc", getSelDoc() == null ? Const.BLANK : SearchUtil.convertNumber(getSelDoc().getIdDocument()));
			
			if (activeTab == Const.CODE_DEFAULT.intValue()){//prvi odabir
				if (selNarucitelj.getIdOrganizacija() != null && selObjekt.getIdObjekt() == null){
					this.activeTab = Const.CODE_ORG.intValue();
				}
				else if (selObjekt.getIdObjekt() != null && selNarucitelj.getIdOrganizacija() == null){
					this.activeTab = Const.CODE_OBJEKT.intValue();
				}
				else {
					this.activeTab = Const.CODE_DEFAULT.intValue();
				}
			} 

			if (activeTab == Const.CODE_ORG.intValue()){
				map.put("selNarucitelj", getSelNarucitelj() == null ? Const.BLANK : SearchUtil.convertNumber(getSelNarucitelj().getIdOrganizacija()));
			} else {
				map.put("selNarucitelj", getSelObjekt() == null ? Const.BLANK : SearchUtil.convertNumber(getSelObjekt().getIdObjekt()));
			}
			
			map.put("activeTab", new Integer(getActiveTab()));
			map.put("selDobavljac", getSelDobavljac() == null ? Const.BLANK : SearchUtil.convertNumber(getSelDobavljac().getIdOrganizacija()));

		    SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
			
			map.put("datumOd", getDatumOd() == null ? format.format(Const.START_DATE) : format.format(getDatumOd()));
			map.put("datumDo", getDatumDo() == null ? format.format(new Date()) : format.format(getDatumDo()));
			
			map.put("id", SearchUtil.convertNumber(getId()));

			lazyModel = new LazyOrderSearchDataModelProdukcija(map, selectedStatuses);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public String getDetail(){  
	 	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (Assign2Bean o : (List<Assign2Bean>) lazyModel.getWrappedData()){
				if (o.getIdNarudzbe().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("searchord", o);
					context.getExternalContext().getSessionMap().put("idsearchord", this.selected);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order_search_detail?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void cancel(ActionEvent event){
		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order?faces-redirect=true");
	}
	
	public void delete(){
		System.out.println("delete");
	}
	
	public void handleClose2() {
		System.out.println("test row: ");
	}
	
	public void generateExcel(){
		String template = "report_narudzbe";
		String output_name = "narudzbe";
		SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
			
		try {
			Map<Integer, Object> map = new HashMap<Integer, Object>();
			
			Date date = new Date();
			int targetYear = date.getYear();
			int historyYear = targetYear - 2;
			
			Date history = new Date();
			history.setYear(historyYear);
			
			System.out.println("DATUMI:");
			System.out.println("OD: " + format.format(history));
			System.out.println("DO: " + format.format(date));
			
			map.put(1, format.format(history));
			map.put(2, format.format(date));
			
			if (rola == 1228 || rola == 1226) {
				map.put(3, Const.BLANK);
				map.put(4, Const.BLANK);
			}
			else if (rola == 2 || rola == 12){
				map.put(3, ub.getIdOrganizacije());
				map.put(4, Const.BLANK);
			}
			else if (rola == 3){
				map.put(3, Const.BLANK);
				map.put(4, ub.getIdOrganizacije());
			}
			fileDownload = ReportUtil.generateExcelReportAdvanced("ReportNarudzbe", template, output_name, map);
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void postProcessXLS(Object document) {  
	    HSSFWorkbook wb = (HSSFWorkbook) document;  
	    HSSFSheet sheet = wb.getSheetAt(0);  
	    HSSFRow header = sheet.getRow(0);  
	      
	    HSSFCellStyle cellStyle = wb.createCellStyle();    
	    cellStyle.setFillForegroundColor(HSSFColor.GREEN.index);  
	    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
	      
	    for(int i=0; i < header.getPhysicalNumberOfCells();i++) {  
	        HSSFCell cell = header.getCell(i);  
	          
	        cell.setCellStyle(cellStyle);  
	    }  
	}  
	  
	public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {  
	    Document pdf = (Document) document;  
	    pdf.setPageSize(PageSize.A4.rotate());
	} 

	public void generateReportNarudzbeNaDan(){
		String template = "report_narudzbe_extra";
		String output_name = "narudzbe_na_dan";
		SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
		DateFormat df = new SimpleDateFormat(Const.DATE_PATTERN);
		Date history = Const.START_DATE;
		Date date = new Date();
		
		try {	
			System.out.println("Priprema reporta za period...");
			//System.out.println("OD: " + this.datumOd);
			//System.out.println("DO: " + this.datumDo);
			
			Map<Integer, Object> map = new HashMap<Integer, Object>();
			
			String datumOd = null;
			String datumDo = null;
			
			int star1 = 0;
			int star2 = 0;

			if (this.datumOd == null || this.datumDo == null ){
				if (this.datumOd == null && this.datumDo != null) {
					datumOd = format.format(history);
					datumDo = df.format(this.datumDo);
					System.out.println("datumOd je null");
					star1 = 1;
				}
				else if (this.datumOd != null && this.datumDo == null) {
					datumOd = df.format(this.datumOd);
					datumDo = format.format(date);
					System.out.println("datumDo je null");
					star2 = 1;
				}
				else {
					datumOd = format.format(history);
					datumDo = format.format(date);
					star1 = 1;
					star2 = 1;
				}
			} else if (this.datumOd != null && this.datumDo != null ){
				datumOd = df.format(this.datumOd);
				datumDo = df.format(this.datumDo);
			}
		    
			System.out.println("DATUMI:");
			System.out.println("OD: " + datumOd);
			System.out.println("DO: " + datumDo);
			
			map.put(1, datumOd);
			map.put(2, datumDo);
			map.put(3, getSelNarucitelj().getIdOrganizacija() == null ? Const.BLANK : getSelNarucitelj().getIdOrganizacija());
			map.put(4, getSelDobavljac().getIdOrganizacija() == null ? Const.BLANK : getSelDobavljac().getIdOrganizacija());
			map.put(5, rola == 3 || rola == 1228 || rola == 1226 ? Const.BLANK : ub.getIdKorisnik().toString());
			map.put(6, getSelDoc().getIdDocument() == null ? Const.BLANK : getSelDoc().getIdDocument().toString());
			map.put(7, getId() == null ? Const.BLANK : getId().toString());
			
			int counter = 8;
			
			for (int i=0; i<selectedStatuses.size(); i++){
				map.put(8+i, selectedStatuses.get(i) );
				counter++;
				System.out.println("Uzeo status: " + selectedStatuses.get(i));
			}
			
			for (int i=counter; i < 15; i++){
				map.put(i, String.valueOf(0));
				System.out.println("Dodao status 0...");
			}
			
			map.put(15, String.valueOf(star1));
			map.put(16, String.valueOf(star2));
			
			fileDownload2 = ReportUtil.generatePdfReport("ReportNarudzbeExtra", template, output_name, map);
		}	
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DocumentBean getSelDoc() {
		return selDoc;
	}

	public void setSelDoc(DocumentBean selDoc) {
		this.selDoc = selDoc;
	}

	public Org4Doc getSelNarucitelj() {
		return selNarucitelj;
	}

	public void setSelNarucitelj(Org4Doc selNarucitelj) {
		this.selNarucitelj = selNarucitelj;
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

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public ArrayList<DocumentBean> getDocList() {
		return docList;
	}

	public void setDocList(ArrayList<DocumentBean> docList) {
		this.docList = docList;
	}

	public Org4Doc getSelDobavljac() {
		return selDobavljac;
	}

	public void setSelDobavljac(Org4Doc selDobavljac) {
		this.selDobavljac = selDobavljac;
	}

	public List<String> getSelectedStatuses() {
		return selectedStatuses;
	}

	public void setSelectedStatuses(List<String> selectedStatuses) {
		this.selectedStatuses = selectedStatuses;
	}

	public Map getStatuses() {
		return statuses;
	}

	public void setStatuses(Map statuses) {
		this.statuses = statuses;
	}

	public LazyDataModel<Assign2Bean> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Assign2Bean> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	public LazyDataModel<Org4Doc> getLazyOrgList() {
		return lazyOrgList;
	}

	public void setLazyOrgList(LazyDataModel<Org4Doc> lazyOrgList) {
		this.lazyOrgList = lazyOrgList;
	}

	public LazyDataModel<Org4Doc> getLazyOrgListDobavljac() {
		return lazyOrgListDobavljac;
	}

	public void setLazyOrgListDobavljac(LazyDataModel<Org4Doc> lazyOrgListDobavljac) {
		this.lazyOrgListDobavljac = lazyOrgListDobavljac;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public Boolean getSearch() {
		return search;
	}

	public void setSearch(Boolean search) {
		this.search = search;
	}

	public StreamedContent getFileDownload() {
		return fileDownload;
	}

	public void setFileDownload(StreamedContent fileDownload) {
		this.fileDownload = fileDownload;
	}

	public Objekt getSelObjekt() {
		return selObjekt;
	}

	public void setSelObjekt(Objekt selObjekt) {
		this.selObjekt = selObjekt;
	}

	public int getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(int activeTab) {
		this.activeTab = activeTab;
	}

	public StreamedContent getFileDownload2() {
		return fileDownload2;
	}

	public void setFileDownload2(StreamedContent fileDownload2) {
		this.fileDownload2 = fileDownload2;
	}

	public List<String> getAllStatuses() {
		return allStatuses;
	}

	public void setAllStatuses(List<String> allStatuses) {
		this.allStatuses = allStatuses;
	}

	public Boolean getRolaUprava() {
		return rolaUprava;
	}

	public void setRolaUprava(Boolean rolaUprava) {
		this.rolaUprava = rolaUprava;
	}
	
}
