package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.Objekt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyObjektDataModel extends LazyDataModel<Objekt>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,String> searchParam = new HashMap<String, String>();

	public LazyObjektDataModel(){
		search = false;
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
    public List<Objekt> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<Objekt> data = new ArrayList<Objekt>();
		
		System.out.println("Loading data between " + first + " and " + (first + pageSize));  
        
		for(Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
	        try {
	            String filterProperty = it.next();
	            String filterValue = filters.get(filterProperty);
	
	            System.out.println("FILTER:" + filterProperty);
	            System.out.println("VALUE:" + filterValue);
	        
				if (filterValue != null){
					search = true;
					searchParam.clear();
					
			        if (filterProperty.equals("objekt")){
			        	searchParam.put("objekt", filterValue);
			     	}
			        else if (filterProperty.equals("vkb")){
			        	searchParam.put("vkb", filterValue + "%");
			     	}
			        else if (filterProperty.equals("subjekt")){
			        	searchParam.put("subjekt", filterValue + "%");
			     	}
			        else if (filterProperty.equals("oib")){
			        	searchParam.put("oib", filterValue + "%");
			     	}
				}
	        }
	        catch (Exception e){
	        	e.printStackTrace();
	        }
		}    
		
		if (search == true) repairSearchParam();
		
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
			return data;
		}
    }
	
	private List<Objekt> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Objekt> lobj = new ArrayList<Objekt>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();
    	
        	if (search == false){
        	
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "SelectEstablishments", parm);
				
        	}
        	else {
        		
        		String objekt = (String) searchParam.get("objekt");
				String vkb = (String) searchParam.get("vkb");
        		String subjekt = (String) searchParam.get("subjekt");
        		String oib = (String) searchParam.get("oib");
        		 
        		parm.add(objekt);
        		parm.add(vkb);
        		parm.add(subjekt);
        		parm.add(oib);
        		parm.add(max);
        		parm.add(min);
        		
            	trx = ctxA.getDatabaseManager().createTransaction("sys");
    			rs =  trx.executePreparedQueryById("test", "SearchEstablishments", parm);
    			
        	}
			
			Objekt o = null;
			
			while (rs.next()){
				
				o = new Objekt();
				o.setIdObjekt(rs.getInt(1));
				o.setObjekt(rs.getString(2));
				o.setVkb(rs.getString(3));
				o.setIdSubjekt(rs.getInt(4));
				o.setSubjekt(rs.getString(5));
				o.setOib(rs.getString(6));
				o.setAdresa(rs.getString(7));
				o.setMjesto(rs.getString(8));
				
				lobj.add(o);
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
    	
    	return lobj;
    }
	
	private int rowCount(){
		List parm = new ArrayList();
		ITransaction trx = null;
		ResultSet rs = null;
       
		//rowCount  
		int dataSize = 0;
		
    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();

        	trx = ctxA.getDatabaseManager().createTransaction("sys");
			if (search == true) {
				
				String objekt = (String) searchParam.get("objekt");
				String vkb = (String) searchParam.get("vkb");
        		String subjekt = (String) searchParam.get("subjekt");
        		String oib = (String) searchParam.get("oib");
        		 
        		parm.add(objekt);
        		parm.add(vkb);
        		parm.add(subjekt);
        		parm.add(oib);
        		
				rs =  trx.executePreparedQueryById("test", "CountSearchEstablishments", parm);
			}
			else {
				rs =  trx.executePreparedQueryById("test", "CountEstablishments", parm);
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
	
	private void repairSearchParam(){
		String key = null;
		String value = null;
		
		for (Map.Entry<String, String> entry : searchParam.entrySet()){
			key = entry.getKey();
			value = entry.getValue();
			
			System.out.println("Key: " + key + ", Value: " + value);
			if (key.equals("objekt")){
				if (value == null) {
					value = "%";
					searchParam.put("objekt", value);
					System.out.println(searchParam.get("objekt"));
				}
			}
			else if (key.equals("vkb")){
				if (value == null) {
					value = "%";
					searchParam.put("vkb", value);
					System.out.println(searchParam.get("vkb"));
				}
			}
			else if (key.equals("subjekt")){
				if (value == null) {
					value = "%";
					searchParam.put("subjekt", value);
					System.out.println(searchParam.get("subjekt"));
				}
			}
			else if (key.equals("oib")){
				if (value == null) {
					value = "%";
					searchParam.put("oib", value);
					System.out.println(searchParam.get("oib"));
				}
			}
		}
		
		if (searchParam.get("objekt") == null) {
			searchParam.put("objekt", "%");
			System.out.println(searchParam.get("objekt"));
		}
		
		if (searchParam.get("vkb") == null) {
			searchParam.put("vkb", "%");
			System.out.println(searchParam.get("vkb"));
		}
		
		if (searchParam.get("subjekt") == null) {
			searchParam.put("subjekt", "%");
			System.out.println(searchParam.get("subjekt"));
		}
		
		if (searchParam.get("oib") == null) {
			searchParam.put("oib", "%");
			System.out.println(searchParam.get("oib"));
		}
	}

	public Boolean getSearch() {
		return search;
	}

	public void setSearch(Boolean search) {
		this.search = search;
	}

	public Map<String, String> getSearchParam() {
		return searchParam;
	}

	public void setSearchParam(Map<String, String> searchParam) {
		this.searchParam = searchParam;
	}

}

