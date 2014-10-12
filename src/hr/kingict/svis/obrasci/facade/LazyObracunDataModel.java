package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Racun;
import hr.kingict.svis.obrasci.web.SubjektBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyObracunDataModel extends LazyDataModel<Racun>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,Object> searchParam = new HashMap<String, Object>();
	private Integer podrucje;
	
	public LazyObracunDataModel(){
		search = false;
	}
	
	public LazyObracunDataModel(Map<String, Object> searchParam){	
	
		if (searchParam != null) search = true;
		this.searchParam = searchParam;
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
	public List<Racun> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<Racun> data = new ArrayList<Racun>();
		
		System.out.println("Loading data between " + first + " and " + (first + pageSize));  
        
		int dataSize = rowCount();
    	this.setRowCount(dataSize);
		
    	if (search == true){
    		try {
            	System.out.println("Records " + first + " - " + (first + pageSize));
                return getPageData(new Integer(first), new Integer(first+pageSize));
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
	
	private List<Racun> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Racun> lracuni = new ArrayList<Racun>();
		ITransaction trx = null;
		ResultSet rs = null;
		
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		
    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();
    	
			//varijable za praćenje
			Racun rac = null;
			SubjektBean sub = null;
        	
        	if (search == false){
        		
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	rs =  trx.executePreparedQueryById("test", "RacunCert", parm);
        	}
        	else {
        		
        		javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
            	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
            	
            	conn = ds.getConnection(); 
            	
            	String subjekt = (String) searchParam.get("subjekt");
        		String datumOd = (String) searchParam.get("datumOd");
        		String datumDo = (String) searchParam.get("datumDo");
        		
        		pstmt1 = conn.prepareStatement(generateQuery(true));
    			
        		pstmt1.setString(1, subjekt.toUpperCase());
    			pstmt1.setString(2, datumOd);
    			pstmt1.setString(3, datumDo);
    			
    			if (getPodrucje().intValue() > 0){
		    		pstmt1.setInt(4, getPodrucje());
		    		pstmt1.setInt(5, max);
		    		pstmt1.setInt(6, min);
    			}
    			else {
    				pstmt1.setInt(4, max);
		    		pstmt1.setInt(5, min);
    			}
    			
    			rs = pstmt1.executeQuery();
        	}
			
			while (rs.next()){
				rac = new Racun();
				sub = new SubjektBean();
				
				//podaci racun
				rac.setIdRacun(rs.getInt(1));
				
				sub.setIdSubjekt(rs.getInt(3));
				sub.setNazivSubjekt(rs.getString(4));
				sub.setOib(rs.getString(5));
				
				sub.setAdresaSubjekt(rs.getString(6) + " " + rs.getString(7) + ", " + rs.getString(8) + " " + rs.getString(9));
				
				rac.setDatumRacuna(rs.getDate(2));
				rac.setDatumPlacanja(rs.getDate(12));
				rac.setUplaceniIznos(rs.getBigDecimal(13));
				rac.setIznos(rs.getBigDecimal(10));
				rac.setIra(rs.getString(14));
				rac.setPath(rs.getString(11));
				rac.setIdStatusPlacanja(rs.getInt(15));
				rac.setNazivStatusPlacanja(rs.getString(16));
				
				rac.setSubjekt(sub);
				
				lracuni.add(rac);
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
    	
    	return lracuni;
    }
	
	private int rowCount(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
       
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		//rowCount  
		int dataSize = 0;
		
    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();

        	trx = ctxA.getDatabaseManager().createTransaction("sys");
			
        	if (search == true) {
				
				javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
            	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
            	
            	conn = ds.getConnection(); 
            	
            	String subjekt = (String) searchParam.get("subjekt");
        		String datumOd = (String) searchParam.get("datumOd");
        		String datumDo = (String) searchParam.get("datumDo");
        		setPodrucje((Integer) searchParam.get("podrucje"));
        		
        		pstmt1 = conn.prepareStatement(generateQuery(false));
        		
        		pstmt1.setString(1, subjekt.toUpperCase());
    			pstmt1.setString(2, datumOd);
    			pstmt1.setString(3, datumDo);
    			
    			if (getPodrucje().intValue() > 0){
		    		pstmt1.setInt(4, getPodrucje());
    			}
    			
    			rs = pstmt1.executeQuery();			
			}
			else {
				rs =  trx.executePreparedQueryById("test", "CountRacunCert", parm);
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
			List<String> statusi = (List<String>) searchParam.get("statusi");
			
    		if (search){
    		
        	queryString = "SELECT * FROM " +
			"( " + 
			    "SELECT a.*, rownum r__ " +
			    "FROM ( " +
					"select t1.id_racun, t1.datum_racuna, t2.id_subject, t2.name as subjekt, t2.vat_no, t4.street, t4.hn, t5.code, t5.name, t1.iznos, t1.path, datum_placanja, uplaceni_iznos, ira, t1.id_status_placanja, t7.naziv " +
					"from vis_ex.cert_racun t1 " +
					"left join sm.sm_subjects t2 on (t1.id_subject = t2.id_subject) " +
					"left join loc.loc_addresses t4 on (t2.id_address = t4.id_address) " +
					"left join loc.loc_zip_codes t5 on (t4.id_zip_code = t5.id_zip_code) " + 
					"left join loc.loc_villages t6 on (t4.id_village = t6.id_village) " +
					"left join vis_ex.sif_racun_status t7 on (t1.id_status_placanja = t7.id_status_placanja) " +
					"where upper(t2.name) like nvl(?,'%') and t1.datum_racuna between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') + 1 " + 
					(getPodrucje().intValue() == 0 ? "" : "and t1.id_racun in (select c1.id_cert_racun from vis_ex.cert_documents c1, vis_ex.cert_certificates c2 where c1.id_certificate = c2.id and c1.id_cert_racun = t1.id_racun and nvl(?, c2.id_type) = c2.id_type ) ");
    		}
    		else {
    			queryString = "select count(*) " +
    						  "from vis_ex.cert_racun t1 " +
    						  "left join sm.sm_subjects t2 on (t1.id_subject = t2.id_subject) " +
    						  "left join loc.loc_addresses t4 on (t2.id_address = t4.id_address) " +
    						  "left join loc.loc_zip_codes t5 on (t4.id_zip_code = t5.id_zip_code) " +
    						  "left join loc.loc_villages t6 on (t4.id_village = t6.id_village) " + 
    						  "left join vis_ex.sif_racun_status t7 on (t1.id_status_placanja = t7.id_status_placanja) " +
    						  "where upper(t2.name) like nvl(?,'%') and t1.datum_racuna between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') + 1 " + 
    						  (getPodrucje().intValue() == 0 ? "" : "and t1.id_racun in (select c1.id_cert_racun from vis_ex.cert_documents c1, vis_ex.cert_certificates c2 where c1.id_certificate = c2.id and c1.id_cert_racun = t1.id_racun and nvl(?, c2.id_type) = c2.id_type ) ");
    		}
    		
        	if (statusi.size() > 0){
        		queryString = queryString + " and t1.id_status_placanja in ( ";
        		for (String s : statusi){
        			queryString = queryString + s + ",";
        		}
        		queryString = queryString.substring(0, queryString.length() - 1);
        		queryString = queryString + " ) ";
        	}
        		
			if (search){		
        	queryString = queryString + 		
					"order by t1.id_racun desc " +
				") a " +
			   	"where rownum < ? " +
			") " +
			"WHERE r__ >= ?";
			}
			
        	System.out.println("UPIT: " + queryString);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return queryString;
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

	public Integer getPodrucje() {
		return podrucje;
	}

	public void setPodrucje(Integer podrucje) {
		this.podrucje = podrucje;
	}
	
}

