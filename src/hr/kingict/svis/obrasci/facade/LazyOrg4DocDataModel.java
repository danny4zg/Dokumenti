package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.Org4Doc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyOrg4DocDataModel extends LazyDataModel<Org4Doc>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,String> searchParam = new HashMap<String, String>();
	private int org_type; //tiskara ili vet_org
	
	public LazyOrg4DocDataModel(int org_type){
		this.org_type = org_type;
		search = false;
		
		System.out.println("VRSTA ORGANIZACIJE: " + org_type);
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
    public List<Org4Doc> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<Org4Doc> data = new ArrayList<Org4Doc>();
		
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
					
			        if (filterProperty.equals("idOrganizacija")){
			        	searchParam.put("idOrganizacija", filterValue);
			     	}
			        else if (filterProperty.equals("naziv")){
			        	searchParam.put("naziv", filterValue + "%");
			     	}
			        else if (filterProperty.equals("adresa")){
			        	searchParam.put("adresa", filterValue + "%");
			     	}
			        else if (filterProperty.equals("mjesto")){
			        	searchParam.put("mjesto", filterValue + "%");
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
			System.out.println("Records " + first + " - " + (first + pageSize));
            return getPageData(new Integer(first), new Integer(first+pageSize));
		}
    }
	
	private List<Org4Doc> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Org4Doc> lorg = new ArrayList<Org4Doc>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();
    	
        	if (search == false){
        	
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	
	        	if (org_type == 1){
	        		rs =  trx.executePreparedQueryById("test", "SelectVetOrg", parm);
	        	}
	        	if (org_type == 2){
	        		rs =  trx.executePreparedQueryById("test", "SelectTiskare", parm);
	        	}
	        }
        	else {
        		
        		System.out.println("search...");
        		
        		String idOrganization = (String) searchParam.get("idOrganizacija");
				String naziv = (String) searchParam.get("naziv");
				String adresa = (String) searchParam.get("adresa");
				String mjesto = (String) searchParam.get("mjesto");
				String oib = (String) searchParam.get("oib");
        			
        		parm.add(idOrganization);
        		parm.add(naziv.toUpperCase());
        		parm.add(adresa.toUpperCase());
        		parm.add(mjesto.toUpperCase());
        		parm.add(oib);
        		parm.add(max);
        		parm.add(min);
        		
            	trx = ctxA.getDatabaseManager().createTransaction("sys");
            	if (org_type == 1){
	        		rs =  trx.executePreparedQueryById("test", "SearchVetOrg2", parm);
	        	}
	        	if (org_type == 2){
	        		rs =  trx.executePreparedQueryById("test", "SearchTiskare2", parm);
	        	}
    			
        	}
			
			Org4Doc o1 = null;
			
			while (rs.next()){
				o1 = new Org4Doc();
				o1.setIdOrganizacija(rs.getInt("id_organization"));
				o1.setNaziv(rs.getString("name1"));
				o1.setAdresa(rs.getString("address1"));
				o1.setMjesto(rs.getString("address2"));
				o1.setOib(rs.getString("vat_no"));
				lorg.add(o1);
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
    	
    	return lorg;
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
				
				String idOrganization = (String) searchParam.get("idOrganizacija");
				String naziv = (String) searchParam.get("naziv");
				String adresa = (String) searchParam.get("adresa");
				String mjesto = (String) searchParam.get("mjesto");
				String oib = (String) searchParam.get("oib");
        			
        		parm.add(idOrganization);
        		parm.add(naziv.toUpperCase());
        		parm.add(adresa.toUpperCase());
        		parm.add(mjesto.toUpperCase());
        		parm.add(oib);
        		
        		if (org_type == 1){
        			rs =  trx.executePreparedQueryById("test", "CountSearchVetOrg2", parm);
	        	}
	        	else if (org_type == 2){
	        		rs =  trx.executePreparedQueryById("test", "CountSearchTiskare2", parm);
	        	}
				
			}
			else {
				parm.clear();
				if (org_type == 1){
        			rs =  trx.executePreparedQueryById("test", "CountVetOrg", parm);
	        	}
	        	else if (org_type == 2){
	        		rs =  trx.executePreparedQueryById("test", "CountTiskare", parm);
	        	}
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
			if (key.equals("idOrganization")){
				if (value == null) {
					value = null;
					searchParam.put("idOrganizacija", value);
					System.out.println(searchParam.get("idOrganizacija"));
				}
			}
			else if (key.equals("naziv")){
				if (value == null) {
					value = "%";
					searchParam.put("naziv", value);
					System.out.println(searchParam.get("naziv"));
				}
			}
			else if (key.equals("adresa")){
				if (value == null) {
					value = "%";
					searchParam.put("adresa", value);
					System.out.println(searchParam.get("adresa"));
				}
			}
			else if (key.equals("mjesto")){
				if (value == null) {
					value = "%";
					searchParam.put("mjesto", value);
					System.out.println(searchParam.get("mjesto"));
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
		
		if (searchParam.get("idOrganizacija") == null) {
			searchParam.put("idOrganizacija", null);
			System.out.println(searchParam.get("idOrganizacija"));
		}
		
		if (searchParam.get("naziv") == null) {
			searchParam.put("naziv", "%");
			System.out.println(searchParam.get("naziv"));
		}
		
		if (searchParam.get("adresa") == null) {
			searchParam.put("adresa", "%");
			System.out.println(searchParam.get("adresa"));
		}
		
		if (searchParam.get("mjesto") == null) {
			searchParam.put("mjesto", "%");
			System.out.println(searchParam.get("mjesto"));
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

	public int getOrg_type() {
		return org_type;
	}

	public void setOrg_type(int org_type) {
		this.org_type = org_type;
	}

}

