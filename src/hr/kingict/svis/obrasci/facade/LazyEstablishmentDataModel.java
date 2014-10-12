package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.ObjektExt;
import hr.kingict.svis.obrasci.web.OrganizationBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyEstablishmentDataModel extends LazyDataModel<ObjektExt>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,Object> searchParam = new HashMap<String, Object>();
	
	public LazyEstablishmentDataModel(){
		search = false;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
	}
	
	public LazyEstablishmentDataModel(Map<String, Object> searchParam){
		if (searchParam != null) search = true;
		this.searchParam = searchParam;
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
	public List<ObjektExt> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<ObjektExt> data = new ArrayList<ObjektExt>();
		
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
	
	private List<ObjektExt> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<ObjektExt> lest = new ArrayList<ObjektExt>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
        	
        	if (search == false){
        		
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	rs =  trx.executePreparedQueryById("test", "SelectEstablishmentsExt", parm);
				
        	}
        	else {
        		
        		String naziv = (String) searchParam.get("naziv");
        		String adresa = (String) searchParam.get("adresa");
        			
        		parm.add(naziv.toUpperCase());
        		parm.add(adresa.toUpperCase());
        		parm.add(max);
        		parm.add(min);
        		
            	trx = ctxA.getDatabaseManager().createTransaction("sys");
    			rs =  trx.executePreparedQueryById("test", "SearchEstablishmentsExt", parm);
    			
        	}
        	
			while (rs.next()){
				ObjektExt o = new ObjektExt();
				o.setIdObjekt(rs.getInt(1));
				o.setObjekt(rs.getString(2));
				o.setVkb(rs.getString(3));
				o.setIdSubjekt(rs.getInt(4));
				o.setSubjekt(rs.getString(5));
				o.setOib(rs.getString(6));
				o.setAdresa(rs.getString(7));
				o.setMjesto(rs.getString(8));
				o.setTel(rs.getString(9));
				o.setFax(rs.getString(10));
				o.setEmail(rs.getString(11));
				lest.add(o);
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
        	if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    	
    	return lest;
    }
	
	private int rowCount(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
       
		//rowCount  
		int dataSize = 0;
		
    	try {
        	
        	trx = ctxA.getDatabaseManager().createTransaction("sys");
			if (search == true) {
				String naziv = (String) searchParam.get("naziv");
        		String adresa = (String) searchParam.get("adresa");
        			
        		parm.add(naziv.toUpperCase());
        		parm.add(adresa.toUpperCase());
        		
				rs =  trx.executePreparedQueryById("test", "CountSearchEstablishmentsExt", parm);
			}
			else {
				rs =  trx.executePreparedQueryById("test", "CountEstablishmentsExt", parm);
			}
			
			if (rs.next()){
				System.out.println("Data Size: " + rs.getInt(1));
		        dataSize = rs.getInt(1);  
			}
			
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
	
}
