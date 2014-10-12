package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.Farma;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyFarmaDataModel extends LazyDataModel<Farma>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,String> searchParam = new HashMap<String, String>();

	
	public LazyFarmaDataModel(){
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
    public List<Farma> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<Farma> data = new ArrayList<Farma>();
		
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
					
			        if (filterProperty.equals("ime")){
			        	searchParam.put("ime", filterValue + "%");
			     	}
			        else if (filterProperty.equals("prezime")){
			        	searchParam.put("prezime", filterValue + "%");
			     	}
			        else if (filterProperty.equals("naziv")){
			        	searchParam.put("naziv", filterValue + "%");
			     	}
			        else if (filterProperty.equals("jibg")){
			        	searchParam.put("jibg", filterValue + "%");
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
	
	private List<Farma> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Farma> lfa = new ArrayList<Farma>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();
    	
        	if (search == false){
        	
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
				rs =  trx.executePreparedQueryById("test", "SelectHoldings", parm);
				
        	}
        	else {

        		String ime = (String) searchParam.get("ime");
				String prezime = (String) searchParam.get("prezime");
        		String naziv = (String) searchParam.get("naziv");
        		String jibg = (String) searchParam.get("jibg");
        		String oib = (String) searchParam.get("oib");
        		 
        		parm.add(ime);
        		parm.add(prezime);
        		parm.add(naziv);
        		parm.add(jibg);
        		parm.add(oib);
        		parm.add(max);
	    		parm.add(min);
        		
            	trx = ctxA.getDatabaseManager().createTransaction("sys");
    			rs =  trx.executePreparedQueryById("test", "SearchHoldings", parm);
    			
        	}
			
			Farma fa = null;
			
			while (rs.next()){
				fa = new Farma();
				fa.setIdLocation(rs.getInt(1));
				fa.setJibg(rs.getString(2));
				fa.setIme(rs.getString(3));
				fa.setPrezime(rs.getString(4));
				fa.setOib(rs.getString(5));
				fa.setNaziv(rs.getString(6));
				fa.setIdAdresa(rs.getInt(7));
				fa.setUlica(rs.getString(8));
				fa.setKbr(rs.getString(9));
				fa.setMjesto(rs.getString(10));
				fa.setPbr(rs.getInt(11));
				lfa.add(fa);
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
    	
    	return lfa;
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
				
				String ime = (String) searchParam.get("ime");
				String prezime = (String) searchParam.get("prezime");
        		String naziv = (String) searchParam.get("naziv");
        		String jibg = (String) searchParam.get("jibg");
        		String oib = (String) searchParam.get("oib");
        		 
        		parm.add(ime);
        		parm.add(prezime);
        		parm.add(naziv);
        		parm.add(jibg);
        		parm.add(oib);
        		
				rs =  trx.executePreparedQueryById("test", "CountSearchHoldings", parm);
			}
			else {
				rs =  trx.executePreparedQueryById("test", "CountHoldings", parm);
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
			if (key.equals("ime")){
				if (value == null) {
					value = "%";
					searchParam.put("ime", value);
					System.out.println(searchParam.get("ime"));
				}
			}
			else if (key.equals("prezime")){
				if (value == null) {
					value = "%";
					searchParam.put("prezime", value);
					System.out.println(searchParam.get("prezime"));
				}
			}
			else if (key.equals("naziv")){
				if (value == null) {
					value = "%";
					searchParam.put("naziv", value);
					System.out.println(searchParam.get("naziv"));
				}
			}
			else if (key.equals("jibg")){
				if (value == null) {
					value = "%";
					searchParam.put("jibg", value);
					System.out.println(searchParam.get("jibg"));
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
		
		if (searchParam.get("ime") == null) {
			searchParam.put("ime", "%");
			System.out.println(searchParam.get("ime"));
		}
		
		if (searchParam.get("prezime") == null) {
			searchParam.put("prezime", "%");
			System.out.println(searchParam.get("prezime"));
		}
		
		if (searchParam.get("naziv") == null) {
			searchParam.put("naziv", "%");
			System.out.println(searchParam.get("naziv"));
		}
		
		if (searchParam.get("jibg") == null) {
			searchParam.put("jibg", "%");
			System.out.println(searchParam.get("jibg"));
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

