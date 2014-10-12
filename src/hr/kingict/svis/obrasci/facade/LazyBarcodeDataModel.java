package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.web.Barcode;
import hr.kingict.svis.obrasci.web.Contingent;
import hr.kingict.svis.obrasci.web.Objekt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyBarcodeDataModel extends LazyDataModel<Barcode>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Integer idContingent;
	
	public LazyBarcodeDataModel(Integer idContingent){
		this.ctxA = ApplicationContextFactory.getCurrentContext();
		this.idContingent = idContingent;
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
    public List<Barcode> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		
		System.out.println("Loading data between " + first + " and " + (first + pageSize));  
		
		int dataSize = rowCount();
    	this.setRowCount(dataSize);

    	
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
	
	private List<Barcode> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<Barcode> lobj = new ArrayList<Barcode>();
		ITransaction trx = null;
		ResultSet rs = null;

    	try {
    		parm.add(getIdContingent());
        	parm.add(max);
    		parm.add(min);
    		
        	trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "SelectBarcodes", parm);
			
			Barcode b = null;
			
			/*ID_CONTINGENT
			TUBES_NO
			TYPE
			D_INSERT
			ID_INSERTER
			ACTIVITY
			VALID_TO
			ID_SESSION
			D_UPDATE
			ID_UPDATER
			NOTES*/
			
			while (rs.next()){
				
				b = new Barcode();
				b.setIdTube(rs.getInt(1));
				b.setBarkod(rs.getInt(2));
				b.setStatus(rs.getInt(3));
				
				lobj.add(b);
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
    		parm.add(getIdContingent());
        	trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "CountBarcodes", parm);
			
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

	public Integer getIdContingent() {
		return idContingent;
	}

	public void setIdContingent(Integer idContingent) {
		this.idContingent = idContingent;
	}

}

