package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Assign2Bean;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyOrderDeliveryDataModel extends LazyDataModel<Assign2Bean>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private UserBean ub;
	private int activeTab;
	private Map<String,Object> searchParam = new HashMap<String, Object>();
	
	public LazyOrderDeliveryDataModel(){
		FacesContext context = FacesContext.getCurrentInstance();
		this.ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
		this.activeTab = Const.CODE_DEFAULT;
		this.search = false;
	}
	
	public LazyOrderDeliveryDataModel(Map<String, Object> searchParam){	
		FacesContext context = FacesContext.getCurrentInstance();
		this.ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
		
		if (searchParam != null) this.search = true;
		this.searchParam = searchParam;
		this.activeTab = ((Integer) searchParam.get("activeTab")).intValue();
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
	public List<Assign2Bean> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<Assign2Bean> data = new ArrayList<Assign2Bean>();
		
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
	
	private List<Assign2Bean> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Assign2Bean> ld = new ArrayList<Assign2Bean>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
        	
        	this.ctxA = ApplicationContextFactory.getCurrentContext();
    	
        	if (search == false){
        		
        		parm.add(ub.getIdOrganizacije());
        		parm.add(ub.getIdOrganizacije());
	        	parm.add(max);
	    		parm.add(min);
	    		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	rs =  trx.executePreparedQueryById("test", "SelectDeliveryOrder", parm);
        	}
        	else {
        		
        		String id = (String) searchParam.get("id");
				String selDoc = (String) searchParam.get("selDoc");
        		String selNarucitelj = (String) searchParam.get("selNarucitelj");
        	
        		String datumOd = (String) searchParam.get("datumOd");
        		String datumDo = (String) searchParam.get("datumDo");
        		       
        		int counter = 0;
        		
        		if (this.activeTab == Const.CODE_DEFAULT.intValue()){
        			counter = 2;
        		}
        		else {
        			counter = 1;
        		}
        		
        		for (int i=0; i<counter; i++){
	        		parm.add(ub.getIdOrganizacije());
	        		parm.add(id);
	        		parm.add(selDoc);
	        		parm.add(selNarucitelj);
	        		parm.add(datumOd);
	        		parm.add(datumDo);
        		}
        		
        		parm.add(max);
        		parm.add(min);
        		
            	trx = ctxA.getDatabaseManager().createTransaction("sys");
            	
            	if (this.activeTab == Const.CODE_DEFAULT.intValue()){
            		rs =  trx.executePreparedQueryById("test", "SearchDeliveryOrderAll", parm);
        		}
        		else if (this.activeTab == Const.CODE_ORG.intValue()){
        			rs =  trx.executePreparedQueryById("test", "SearchDeliveryOrderType1", parm);
        		}
        		else if (this.activeTab == Const.CODE_OBJEKT.intValue()){
        			rs =  trx.executePreparedQueryById("test", "SearchDeliveryOrderType2", parm);
        		}
    			
        	}
			
			while (rs.next()){
				Assign2Bean ab = new Assign2Bean();
				ab.setIdStavkaNarudzbe(rs.getInt(1));
				ab.setDatumNarudzbe(rs.getDate(2));
				ab.setIdNarudzbe(rs.getInt(3));
				
				DocumentBean doc = new DocumentBean();
				doc.setIdDocument(rs.getInt(4));
				doc.setCode(rs.getString(5));
				doc.setDesc(rs.getString(6));
				doc.setIdUnit(rs.getInt(12));
				doc.setUnit(rs.getString(13));
				doc.setPrice(rs.getBigDecimal(18));
				ab.setDoc(doc);
				
				ab.setKolicina(rs.getInt(7));
				ab.setCijena(rs.getBigDecimal(8));
				ab.setIdStatus(rs.getInt(9));
				ab.setStatus(rs.getString(10));
				ab.setIdNarucitelj(rs.getInt(11));
				ab.setNarucitelj(rs.getString(14));
				ab.setOrgType(rs.getInt(19));
				ab.setSerijskiBrojOd(rs.getString(16));
				ab.setSerijskiBrojDo(rs.getString(17));
				ld.add(ab);
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
    	
    	return ld;
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
				String id = (String) searchParam.get("id");
				String selDoc = (String) searchParam.get("selDoc");
        		String selNarucitelj = (String) searchParam.get("selNarucitelj");
        	
        		String datumOd = (String) searchParam.get("datumOd");
        		String datumDo = (String) searchParam.get("datumDo");
        		       		
        		parm.add(ub.getIdOrganizacije());
        		parm.add(id);
        		parm.add(selDoc);
        		parm.add(selNarucitelj);
        		parm.add(datumOd);
        		parm.add(datumDo);
        		
        		if (this.activeTab == Const.CODE_DEFAULT.intValue()){
            		rs =  trx.executePreparedQueryById("test", "CountSearchDeliveryOrderAll", parm);
        		}
        		else if (this.activeTab == Const.CODE_ORG.intValue()){
        			rs =  trx.executePreparedQueryById("test", "CountSearchDeliveryOrderType1", parm);
        		}
        		else if (this.activeTab == Const.CODE_OBJEKT.intValue()){
        			rs =  trx.executePreparedQueryById("test", "CountSearchDeliveryOrderType2", parm);
        		}
				
			}
			else {
				parm.add(ub.getIdOrganizacije());
				rs =  trx.executePreparedQueryById("test", "CountDeliveryOrder", parm);
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

	public UserBean getUb() {
		return ub;
	}

	public void setUb(UserBean ub) {
		this.ub = ub;
	}

	public int getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(int activeTab) {
		this.activeTab = activeTab;
	}
	
}

