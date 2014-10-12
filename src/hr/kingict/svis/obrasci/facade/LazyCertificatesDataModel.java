package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Certifikat;
import hr.kingict.svis.obrasci.web.CertifikatJezici;
import hr.kingict.svis.obrasci.web.Drzava;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.sql.DataSource;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortOrder;

public class LazyCertificatesDataModel extends LazyDataModel<Certifikat> implements SelectableDataModel<Certifikat>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,Object> searchParam = new HashMap<String, Object>();
	private int rola;
	private UserBean ub;
	private CertifikatJezici[] selectedJezici;
	private Drzava[] selectedDrzava;
	private List<String> selectedMjera;
	private int filter;
	private Integer aktivnost;
	private Integer podrucje;
	
	public LazyCertificatesDataModel(){
		this.search = false;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
	}
	
	public LazyCertificatesDataModel(Map<String, Object> searchParam, List<String> selectedMjera, CertifikatJezici[] selectedJezici, Drzava[] selectedDrzava){
		if (searchParam != null) {
			this.search = true;
			this.searchParam = searchParam;
			this.selectedMjera = selectedMjera;
			this.selectedJezici = selectedJezici;
			this.selectedDrzava = selectedDrzava;
		}	
		else this.search = false;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
	}
	
	public LazyCertificatesDataModel(int filterType){
		this.search = false;
		this.filter = filterType;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
	}
	
	@Override
    public void setRowIndex(int rowIndex) {
        /*
         * The following is in ancestor (LazyDataModel):
         * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
         */
        if (rowIndex == -1 || getPageSize() == 0) {
            super.setRowIndex(-1);
        }
        else
            super.setRowIndex(rowIndex % getPageSize());
    }
	
	@Override
	public List<Certifikat> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		
		//System.out.println("Loading data between " + first + " and " + (first + pageSize));  
        
		int dataSize = rowCount();
    	this.setRowCount(dataSize);
		
    	if (search == true){
    		try {
    			if (dataSize < pageSize){
    				System.out.println("Records " + Const.PAGE_START + " - " + pageSize);
    				return getPageData(Const.PAGE_START, pageSize);
    			}
    			else {	
    				System.out.println("Records " + first + " - " + (first + pageSize));
    				return getPageData(new Integer(first), new Integer(first+pageSize));
    			}
            }
            catch(Exception e) {
            	e.printStackTrace();
            }	
    	}
    	
		if(dataSize > pageSize) {
        	System.out.println("Change page...");
        	
            try {
            	System.out.println("Records " + first + " - " + (first + pageSize));
                return getPageData(new Integer(first), new Integer(first+pageSize));
            }
            catch(IndexOutOfBoundsException e) {
            	System.out.println("Records " + first + " - " + (first + (dataSize % pageSize)));
                return getPageData(new Integer(first), new Integer(first + (dataSize % pageSize)));
            }
        }
		else {
			return getPageData(new Integer(first), new Integer(first+pageSize));
		}
    }
	
	private List<Certifikat> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Certifikat> certList = new ArrayList<Certifikat>();
		ITransaction trx = null;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;

    	try {
    		Integer idOrganization = null;
    		
    		if (filter == 1){

    			idOrganization = ((OrganizationBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("organization")).getIdOrganization();
        		
    			trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	parm.add(idOrganization);
		        parm.add(max);
			    parm.add(min);
		    	rs =  trx.executePreparedQueryById("test", "SelectCertificatesFilter", parm);
    		}
    		else if (filter == 2){
    			
    			idOrganization = ((OrganizationBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("organization")).getIdOrganization();
        		
    			trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	parm.add(idOrganization);
		        parm.add(max);
			    parm.add(min);
		    	rs =  trx.executePreparedQueryById("test", "SelectCertificatesFilter2", parm);
    		}
    		else if (search == false){
        		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	
		        parm.add(max);
			    parm.add(min);
		    	rs =  trx.executePreparedQueryById("test", "SelectCertificatesLanguages", parm);

        	}
        	else {

        		javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
            	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
            	
            	conn = ds.getConnection(); 
            	
            	String kod = (String) searchParam.get("kod");
    			System.out.println("LAZY PARAM: " + kod);
        		String naziv = (String) searchParam.get("naziv");
        		System.out.println("LAZY PARAM: " + naziv);
        		//String selectedDrzava = (String) searchParam.get("selectedDrzava");
        		//System.out.println("LAZY PARAM: " + selectedDrzava);
        		System.out.println("LAZY PARAM: " + getPodrucje());
        		Integer aktivnost = (Integer) (searchParam.get("aktivnost"));
        		System.out.println("LAZY PARAM: " + aktivnost);
        		String datumOd = (String) searchParam.get("datumOd");
        		System.out.println("LAZY PARAM: " + datumOd);
        		String datumDo = (String) searchParam.get("datumDo");
        		System.out.println("LAZY PARAM: " + datumDo);
        		
        	    pstmt1 = conn.prepareStatement(generateQuery(true));
    			
        	    System.out.println("MAX: " + max + ", MIN: " + min);
        		pstmt1.setString(1, kod.toUpperCase());
        		pstmt1.setString(2, naziv.toUpperCase());
    			//pstmt1.setString(3, selectedDrzava);
    			pstmt1.setString(3, datumOd);
    			pstmt1.setString(4, datumDo);
    			
    			if (getPodrucje().intValue() > 0){
    				pstmt1.setInt(5, getPodrucje());
        			pstmt1.setInt(6, aktivnost);
        			pstmt1.setInt(7, max);
        			pstmt1.setInt(8, min);
    			}
    			else {
    				pstmt1.setInt(5, aktivnost);
    				pstmt1.setInt(6, max);
        			pstmt1.setInt(7, min);
    			}
    			
    			rs = pstmt1.executeQuery();
        	}
			
			Certifikat cert = null;
			
			int b = 0;
			while (rs.next()){
				b++;
				
				cert = new Certifikat();
				
				cert.setIdCertifikat(rs.getInt(1));
				cert.setJeziciTekst(rs.getString(2));
				cert.setNazivCertifikat(rs.getString(3));
				cert.setKodCertifikat(rs.getString(4));
				cert.setIdDrzava(null);//rs.getString(5)
				cert.setNazivDrzava(rs.getString(7));
				cert.setIdMjera(rs.getInt(8));
				cert.setNazivMjera(rs.getString(9));
				cert.setTip(rs.getInt(11));
				cert.setNazivTip(rs.getString(12));
				cert.setDatumPromjene(rs.getDate(13));
				cert.setDatumVrijediOd(rs.getDate(14));
				cert.setDatumVrijediDo(rs.getDate(15));
				cert.setKomentar(rs.getString(16));
				cert.setTemplateNaziv(rs.getString(17));
				cert.setSerijskiBroj(rs.getString(18));
				cert.setOznakaSerije(rs.getString(19));
				cert.setCijena(rs.getBigDecimal(20));
				
				certList.add(cert);

			}
			
			System.out.println("Ukupno vraćeno " + b + " zapisa...");
			
			if (pstmt1 != null) pstmt1.close();
			rs.close();

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
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
        	if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    	
    	return certList;
    }
	
	private int rowCount(){
		List parm = new ArrayList();
		ITransaction trx = null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
       
		//rowCount  
		int dataSize = 0;
		
    	try {
    		Integer idOrganization = null;
    		
    		if (filter == 1){

    			idOrganization = ((OrganizationBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("organization")).getIdOrganization();
        	
    			parm.add(idOrganization);
    			trx = ctxA.getDatabaseManager().createTransaction("sys");
        		rs =  trx.executePreparedQueryById("test", "CountCertificatesFilter", parm);
    		}
    		else if (filter == 2){
    			
    			idOrganization = ((OrganizationBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("organization")).getIdOrganization();
        	
    			parm.add(idOrganization);
    			trx = ctxA.getDatabaseManager().createTransaction("sys");
        		rs =  trx.executePreparedQueryById("test", "CountCertificatesFilter2", parm);
    		}
    		else if (search == true) {
				
        		javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
            	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
            	
            	conn = ds.getConnection(); 
            	
            	String kod = (String) searchParam.get("kod");
    			System.out.println("LAZY PARAM: " + kod);
        		String naziv = (String) searchParam.get("naziv");
        		System.out.println("LAZY PARAM: " + naziv);
        		//String selectedDrzava = (String) searchParam.get("selectedDrzava");
        		//System.out.println("LAZY PARAM: " + selectedDrzava);
        		setPodrucje((Integer) searchParam.get("podrucje"));
        		System.out.println("LAZY PARAM: " + getPodrucje());
        		Integer aktivnost = (Integer) searchParam.get("aktivnost");
        		System.out.println("LAZY PARAM: " + aktivnost);
        		String datumOd = (String) searchParam.get("datumOd");
        		System.out.println("LAZY PARAM: " + datumOd);
        		String datumDo = (String) searchParam.get("datumDo");
        		System.out.println("LAZY PARAM: " + datumDo);      
        		
        		System.out.println("AKTIVNOST: " + aktivnost);
            	System.out.println("PODRUCJE: " + podrucje);
        		
        		pstmt1 = conn.prepareStatement(generateQuery(false));
    			
        		pstmt1.setString(1, kod.toUpperCase());
        		pstmt1.setString(2, naziv.toUpperCase());
    			//pstmt1.setString(3, selectedDrzava);
    			
    			if (getPodrucje().intValue() > 0){
        			pstmt1.setString(3, datumOd);
        			pstmt1.setString(4, datumDo);
        			pstmt1.setInt(5, getPodrucje());
        			pstmt1.setInt(6, aktivnost);
    			}
    			else {
        			pstmt1.setString(3, datumOd);
        			pstmt1.setString(4, datumDo);
        			pstmt1.setInt(5, aktivnost);
    			}
    			
    			rs = pstmt1.executeQuery();
    		
				//trx.executePreparedQueryById("test", "CountSearchCertificatesLanguages", parm);
				
        	}
			else {
				trx = ctxA.getDatabaseManager().createTransaction("sys");
        		rs =  trx.executePreparedQueryById("test", "CountCertificatesLanguages", parm);
			}
			
			if (rs.next()){
				System.out.println("Data Size: " + rs.getInt(1));
		        dataSize = rs.getInt(1);  
			}
			
			if (pstmt1 != null) pstmt1.close();
			rs.close();
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
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
        
        return dataSize;
	}
	
	private String generateQuery(boolean search){
		String queryString = null;
		
		try {
	
    		if (search){
        	queryString = "SELECT * FROM " +
			"( " + 
			    "SELECT a.*, rownum r__ " +
			    "FROM ( " +
				    "select distinct t3.id as id_certificate, (select listagg(t2.name,', ') within group (order by t1.id) from vis_ex.cert_cert_lang t1 left join sm.sm_languages t2 on (t1.id_language = t2.id_language) where t1.id_certificate = t3.id and t1.activity = 1) as jezici, " + 
					"t3.TEXT as naziv_cert, t3.code, null as sm_states_id, null as drzava_code, (select listagg(t4.text,', ') within group (order by t4.id) from vis_ex.cert_states tcs " +
					"left join sm.sm_states t4 on (tcs.id_state = t4.id) where tcs.id_certificate = t3.id and tcs.activity = 1) as drzava, t3.id_measure, t5.name as mjera, t3.status, t3.id_type, t6.naziv as tip, t3.d_change, t3.valid_from, t3.valid_to, t3.notes, t3.document_name, t3.serial_no, t3.serija, t3.price " +
					"from vis_ex.cert_certificates t3 " +
					"left join vis_ex.cert_measure t5 on (t3.id_measure = t5.id_measure) " +
					"left join vis_ex.cert_type t6 on (t3.id_type = t6.id_type) " +
					"left join vis_ex.cert_cert_lang t2 on (t3.id = t2.id_certificate and t2.activity = 1) " +
					"left join vis_ex.cert_states t4 on (t3.id = t4.id_certificate and t4.activity = 1) " +
					"where t3.code like ? and upper(t3.text) like ? and (t3.valid_from >= nvl(to_date(?,'dd.MM.yyyy'),to_date('01.01.2000','dd.MM.yyyy')) or t3.valid_from is null) and (t3.valid_to <= nvl(to_date(?,'dd.MM.yyyy'), sysdate) or t3.valid_to is null) " +
					(getPodrucje().intValue() == 0 ? "and t3.id_type in (1, 2, 3, 4) " : "and nvl(?, t3.id_type) = t3.id_type ") +
					"and t3.activity = nvl(?, 1) ";
    		}
    		else {
    			queryString = "select count(distinct a.id_certificate) " +
    						  "from ( " +
    						  	"select distinct t3.id as id_certificate, (select listagg(t2.name,', ') within group (order by t1.id) from vis_ex.cert_cert_lang t1 left join sm.sm_languages t2 on (t1.id_language = t2.id_language) where t1.id_certificate = t3.id and t1.activity = 1) as jezici, " + 
    							"t3.TEXT as naziv_cert, t3.code, null as sm_states_id, null as drzava_code, (select listagg(t4.text,', ') within group (order by t4.id) from vis_ex.cert_states tcs " +
    							"left join sm.sm_states t4 on (tcs.id_state = t4.id) where tcs.id_certificate = t3.id and tcs.activity = 1) as drzava, t3.id_measure, t5.name as mjera, t3.status, t3.id_type, t6.naziv as tip, t3.d_change, t3.valid_from, t3.valid_to, t3.notes, t3.document_name, t3.serial_no, t3.serija, t3.price " +
    							"from vis_ex.cert_certificates t3 " +
    							"left join vis_ex.cert_measure t5 on (t3.id_measure = t5.id_measure) " +
    							"left join vis_ex.cert_type t6 on (t3.id_type = t6.id_type) " +
    							"left join vis_ex.cert_cert_lang t2 on (t3.id = t2.id_certificate and t2.activity = 1) " +
    							"left join vis_ex.cert_states t4 on (t3.id = t4.id_certificate and t4.activity = 1) " +
    						  	"where t3.code like ? and upper(t3.text) like ? and (t3.valid_from >= nvl(to_date(?,'dd.MM.yyyy'),to_date('01.01.2000','dd.MM.yyyy')) or t3.valid_from is null) and (t3.valid_to <= nvl(to_date(?,'dd.MM.yyyy'), sysdate) or t3.valid_to is null) " +
    						  	(getPodrucje().intValue() == 0 ? "and t3.id_type in (1, 2, 3, 4) " : "and nvl(?, t3.id_type) = t3.id_type ") +
    						  	"and t3.activity = nvl(?, 1) ";
    		}
    		
        	if (selectedJezici.length > 0){
        		queryString = queryString + " and t2.id_language in ( ";
        		for (CertifikatJezici c : selectedJezici){
        			queryString = queryString + c.getIdJezik() + ",";
        		}
        		queryString = queryString.substring(0, queryString.length() - 1);
        		queryString = queryString + " ) ";
        	}
        	
        	if (selectedDrzava.length > 0){
        		queryString = queryString + " and t4.id_state in ( ";
        		for (Drzava d : selectedDrzava){
        			queryString = queryString + d.getIdDrzava() + ",";
        		}
        		queryString = queryString.substring(0, queryString.length() - 1);
        		queryString = queryString + " ) ";
        	}
        	
        	if (selectedMjera.size() > 0){
        		queryString = queryString + " and t3.id_measure in (";
        		for (String  m : selectedMjera){
        			queryString = queryString + m + ",";
        		}
        		queryString = queryString.substring(0, queryString.length() - 1);
        		queryString = queryString + " ) ";
        	}
					
			if (search){		
        	queryString = queryString + 		
					//"group by t3.id, t3.TEXT, t3.code, t3.id_measure, t5.name, t3.status, t3.id_type, t6.naziv, t3.d_change, t3.valid_from, t3.valid_to, t3.notes, t3.document_name, t3.serial_no, t3.serija, t3.price " +
					"order by t3.id, t3.code " +
				") a " +
			   	"where rownum < ? " +
			") " +
			"WHERE r__ >= ?";
			}
			else {
				queryString = queryString + " ) a";
			}
			
        	System.out.println("UPIT: " + queryString);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return queryString;
	}
		
	 @Override  
	    public Certifikat getRowData(String rowKey) {  
	        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
	          
	        List<Certifikat> clist= (List<Certifikat>) getWrappedData();  
	          
	        for(Certifikat c : clist) {  
	            if(c.getIdCertifikat().equals(rowKey))  
	                return c;  
	        }  
	          
	        return null;  
	    }  
	  
	    @Override  
	    public Object getRowKey(Certifikat c) {  
	        return c.getIdCertifikat();  
	    }  

	public Boolean getSearch() {
		return search;
	}

	public void setSearch(Boolean search) {
		this.search = search;
	}

	public Map<String, Object> getSearchParam() {
		return searchParam;
	}

	public void setSearchParam(Map<String, Object> searchParam) {
		this.searchParam = searchParam;
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

	public CertifikatJezici[] getSelectedJezici() {
		return selectedJezici;
	}

	public void setSelectedJezici(CertifikatJezici[] selectedJezici) {
		this.selectedJezici = selectedJezici;
	}

	public List<String> getSelectedMjera() {
		return selectedMjera;
	}

	public void setSelectedMjera(List<String> selectedMjera) {
		this.selectedMjera = selectedMjera;
	}

	public int isFilter() {
		return filter;
	}

	public void setFilter(int filter) {
		this.filter = filter;
	}

	public Integer getAktivnost() {
		return aktivnost;
	}

	public void setAktivnost(Integer aktivnost) {
		this.aktivnost = aktivnost;
	}

	public Integer getPodrucje() {
		return podrucje;
	}

	public void setPodrucje(Integer podrucje) {
		this.podrucje = podrucje;
	}
	
}
