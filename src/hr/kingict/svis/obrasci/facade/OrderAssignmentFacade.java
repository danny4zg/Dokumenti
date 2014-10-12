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
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.Objekt;
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

import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

@ManagedBean(name="orderAssig")
@ViewScoped
public class OrderAssignmentFacade implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private LazyDataModel<Assign2Bean> lazyModel;
	private Integer selected;
	
	//search params
	private Integer id;
	private DocumentBean selDoc;
	private Org4Doc selNarucitelj;
	private Objekt selObjekt;
	private Date datumOd;
	private Date datumDo;
	
	private UserBean ub;
	private int rola;
	private Boolean search;
	
	private StreamedContent fileDownload;
	private int activeTab = Const.CODE_DEFAULT;
	
	private ArrayList<DocumentBean> docList = new ArrayList<DocumentBean>();
	
	public OrderAssignmentFacade(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
			
			if (rola != 3){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
				
				ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
				
				selDoc = new DocumentBean();
				selNarucitelj = new Org4Doc();
				selObjekt = new Objekt();
		
				trx = ctxA.getDatabaseManager().createTransaction("sys");
				
				//upiti za lookup
				parm.clear();
				parm.add(ub.getIdOrganizacije());//staviti id 
				rs =  trx.executePreparedQueryById("test", "DokumentiNarudzba", parm);
				
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
				
				lazyModel = new LazyOrderAssignmentDataModel();
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
	
	
	public void handleClose() {
		System.out.println("test row: ");
		System.out.println(getSelDoc().getCode() + " * " + getSelDoc().getDesc() );
	}
	
	public void onTabChange(TabChangeEvent event) {  
        setActiveTab(event.getTab().getTitle().equals(Const.TEXT_ORG) ? Const.CODE_ORG.intValue() : event.getTab().getTitle().equals(Const.TEXT_OBJEKT) ? Const.CODE_OBJEKT.intValue() : Const.CODE_DEFAULT.intValue());
        System.out.println("ACTIVE TAB: " + getActiveTab());
        
        if (this.activeTab == 1) setSelObjekt(new Objekt());
        else if (this.activeTab == 2) setSelNarucitelj(new Org4Doc());
	} 
	
	public void clear(ActionEvent event){
		System.out.println("CLEAR");
		setSelNarucitelj(new Org4Doc());
		setSelObjekt(new Objekt());
		setSelDoc(new DocumentBean());
		setId(null);
		setDatumOd(null);
		setDatumDo(null);	
		setActiveTab(Const.CODE_DEFAULT.intValue());
	}
	
	public void search(){
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("id", SearchUtil.convertNumber(getId()));
			map.put("selDoc", SearchUtil.convertNumber(getSelDoc().getIdDocument()));
				
			if (activeTab == Const.CODE_DEFAULT.intValue()){//prvi odabir
				if (selNarucitelj.getIdOrganizacija() != null && selObjekt.getIdObjekt() == null){
					this.activeTab = Const.CODE_ORG.intValue();
				}
				else if (selObjekt.getIdObjekt() != null && selNarucitelj.getIdOrganizacija() == null){
					System.out.println("POSTAVI 2");
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
			
		    SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);
			
			map.put("datumOd", getDatumOd() == null ? format.format(Const.START_DATE) : format.format(getDatumOd()));
			map.put("datumDo", getDatumDo() == null ? format.format(new Date()) : format.format(getDatumDo()));
			
			lazyModel = new LazyOrderAssignmentDataModel(map);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
	}
	
	public String getDetail(){   	
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			
			for (Assign2Bean o : (List<Assign2Bean>) lazyModel.getWrappedData()){
				if (o.getIdNarudzbe().intValue() == this.selected.intValue()){

					context.getExternalContext().getSessionMap().put("assiord", o);
					context.getExternalContext().getSessionMap().put("idord", this.selected);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "edit_order_assignment?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void generateExcel(){
		String template = "report_narudzbe_org";
		String output_name = "narudzbe_org";
		SimpleDateFormat format = new SimpleDateFormat(Const.DATE_PATTERN);

		try {
			Map<Integer, Object> map = new HashMap<Integer, Object>();
			Date date = new Date();
			int targetYear = date.getYear();
			int historyYear = targetYear - 2;
			
			Date history = new Date();
			history.setYear(historyYear);
			
			map.put(1, ub.getIdOrganizacije());
			map.put(2, format.format(history));
			map.put(3, format.format(date));
			
			fileDownload = ReportUtil.generateExcelReportAdvanced("ReportNarudzbeTiskara", template, output_name, map);
		
		} catch (Exception e){
			e.printStackTrace();
		}
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

	public LazyDataModel<Assign2Bean> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Assign2Bean> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public StreamedContent getFileDownload() {
		return fileDownload;
	}

	public void setFileDownload(StreamedContent fileDownload) {
		this.fileDownload = fileDownload;
	}

	public int getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(int activeTab) {
		this.activeTab = activeTab;
	}

	public Objekt getSelObjekt() {
		return selObjekt;
	}

	public void setSelObjekt(Objekt selObjekt) {
		this.selObjekt = selObjekt;
	}
	
}
