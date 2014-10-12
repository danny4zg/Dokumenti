package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.CertifikatJezici;
import hr.kingict.svis.obrasci.web.CertifikatPrint;
import hr.kingict.svis.obrasci.web.Drzava;
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
import org.primefaces.model.SortOrder;

public class LazyCertPrintedDataModel extends LazyDataModel<CertifikatPrint>{
	
	private static final long serialVersionUID = 1L;
	protected IApplicationContext ctxA = null;
	private Boolean search;
	private Map<String,Object> searchParam = new HashMap<String, Object>();
	private int rola;
	private UserBean ub;
	private CertifikatJezici[] selectedJezici;
	private Drzava[] selectedDrzava;
	private List<String> selectedMjera;
	private boolean storno;
	private boolean certAdmin = false;
	
	public LazyCertPrintedDataModel(boolean storno){
		this.search = false;
		this.storno = storno;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
		FacesContext context = FacesContext.getCurrentInstance();
		
		rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
		ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
		
		certAdmin = ((Boolean) context.getExternalContext().getSessionMap().get("certadmin")).booleanValue();
	}
	
	public LazyCertPrintedDataModel(Map<String, Object> searchParam, List<String> selectedMjera, CertifikatJezici[] selectedJezici, Drzava[] selectedDrzava, boolean storno){
		if (searchParam != null) this.search = true;
		this.searchParam = searchParam;
		this.selectedMjera = selectedMjera;
		this.selectedJezici = selectedJezici;
		this.selectedDrzava = selectedDrzava;
		this.storno = storno;
		this.ctxA = ApplicationContextFactory.getCurrentContext();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
		ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
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
	public List<CertifikatPrint> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
		List<CertifikatPrint> data = new ArrayList<CertifikatPrint>();
		
		System.out.println("Loading data between " + first + " and " + (first + pageSize));  
        
		int dataSize = rowCount();
    	this.setRowCount(dataSize);
		
    	System.out.println("ROLA - LAZY LOAD - CERT PRINTED/STORNO -> " + rola);
    	
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
	
	private List<CertifikatPrint> getPageData(Integer min, Integer max){
    	List parm = new ArrayList();
    	List<CertifikatPrint> certList = new ArrayList<CertifikatPrint>();
		ITransaction trx = null;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;

    	try {
    	
        	if (search == false){
        		
	        	trx = ctxA.getDatabaseManager().createTransaction("sys");
	        	if (!storno){
		        	if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin || rola == Const.ROLA_UPRAVA_2 || rola == Const.ROLA_DVI.intValue() || rola == Const.ROLA_INSPEKTOR.intValue()){
		        		parm.add(max);
			    		parm.add(min);
		    			rs =  trx.executePreparedQueryById("test", "SelectPrintedCertificatesLanguages", parm);
		    		} else if (rola == 2 || rola == 12){
		    			parm.add(ub.getIdOrganizacije());
		    			parm.add(max);
			    		parm.add(min);
						rs =  trx.executePreparedQueryById("test", "SelectPrintedCertificatesLanguagesOrg", parm);
		    		}	
	        	}
	        	else {
	        		if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin || rola == Const.ROLA_UPRAVA_2 || rola == Const.ROLA_DVI.intValue() || rola == Const.ROLA_INSPEKTOR.intValue()){
	        			parm.add(max);
			    		parm.add(min);
		    			rs =  trx.executePreparedQueryById("test", "SelectPrintedCertificatesLanguagesStorno2", parm);
					} else if (rola == 2 || rola == 12){
						parm.add(ub.getIdOrganizacije());
						parm.add(max);
			    		parm.add(min);
						rs =  trx.executePreparedQueryById("test", "SelectPrintedCertificatesLanguagesStornoOrg", parm);
		    		}
	        	}
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
        		String datumOd = (String) searchParam.get("datumOd");
        		System.out.println("LAZY PARAM: " + datumOd);
        		String datumDo = (String) searchParam.get("datumDo");
        		System.out.println("LAZY PARAM: " + datumDo);
        		String selectedOrg = (String) searchParam.get("selectedOrg");
        		System.out.println("LAZY PARAM: " + selectedOrg);
        		
        		pstmt1 = conn.prepareStatement(generateQuery(true, storno));
     			
         	    System.out.println("MAX: " + max + ", MIN: " + min);
         		pstmt1.setString(1, kod.toUpperCase());
         		pstmt1.setString(2, naziv.toUpperCase());
     			//pstmt1.setString(3, selectedDrzava);
     			pstmt1.setString(3, datumOd);
     			pstmt1.setString(4, datumDo);
        		
            	if (!storno){
            		
	            	if (rola == Const.ROLA_ADMIN_CERTIFIKATI || rola == Const.ROLA_UPRAVA_2 || certAdmin){
	            		pstmt1.setString(5, selectedOrg);
	    				//rs =  trx.executePreparedQueryById("test", "SearchPrintedCertificatesLanguages", parm);
	    			} else if (rola == 2 || rola == 12){
	    				pstmt1.setString(5, String.valueOf(ub.getIdOrganizacije()));
	    				//rs =  trx.executePreparedQueryById("test", "SearchPrintedCertificatesLanguagesOrg", parm);
	    			}
	            	pstmt1.setInt(6, max);
         			pstmt1.setInt(7, min);
         			
	            	rs = pstmt1.executeQuery();
            	}
            	else {
            		
            		if (rola == Const.ROLA_ADMIN_CERTIFIKATI || rola == Const.ROLA_UPRAVA_2 || certAdmin){
            			pstmt1.setString(5, selectedOrg);
        				//rs =  trx.executePreparedQueryById("test", "SearchPrintedCertificatesLanguagesStorno2", parm);
        			} else if (rola == 2 || rola == 12){
        				pstmt1.setString(5, String.valueOf(ub.getIdOrganizacije()));
        				//rs =  trx.executePreparedQueryById("test", "SearchPrintedCertificatesLanguagesStornoOrg", parm);
        			}
            		
            		pstmt1.setInt(6, max);
         			pstmt1.setInt(7, min);
            		
            		rs = pstmt1.executeQuery();
            	}
    			
        	}
			
			CertifikatPrint cert = null;
			
			while (rs.next()){
				
				cert = new CertifikatPrint();
				
				cert.setIdCertifikatPrint(rs.getInt(1));
				cert.setIdCertifikat(rs.getInt(2));
			
				//cert.setJeziciTekst(jeziciLista2Text(ltmplang));
				cert.setKodCertifikat(rs.getString(3));
				cert.setNazivCertifikat(rs.getString(4));
				
				cert.setJeziciTekst(rs.getString(5));
				
				cert.setIdMjera(rs.getInt(6));
				cert.setNazivMjera(rs.getString(7));
				
				cert.setIdDrzava(rs.getInt(8));
				cert.setNazivDrzava(rs.getString(9));
				
				cert.setSerijskiBroj(rs.getString(10));
				cert.setCijena(rs.getBigDecimal(11));
				
				if (!storno){
					cert.setDatumPrintanja(rs.getDate(12));
				}
				else {
					cert.setDatumStorniranja(rs.getDate(12));
				} 
				
				cert.setOrgPrint(rs.getInt(13));
				cert.setNazivOrgPrint(rs.getString(14));
				
				cert.setOznakaSerije(rs.getString(15));
				
				certList.add(cert);
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
    	
    	return certList;
    }
	
	private int rowCount(){
		List parm = new ArrayList();
		ITransaction trx = null;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
       
		//rowCount  
		int dataSize = 0;
		
    	try {

			if (search == true) {
				
        		javax.naming.InitialContext ctx = new javax.naming.InitialContext(); 
            	DataSource ds = (DataSource) ctx.lookup(Const.DATASOURCE);
            	
            	conn = ds.getConnection(); 
            	
            	String kod = (String) searchParam.get("kod");
    			System.out.println("LAZY PARAM: " + kod);
        		String naziv = (String) searchParam.get("naziv");
        		System.out.println("LAZY PARAM: " + naziv);
        		//String selectedDrzava = (String) searchParam.get("selectedDrzava");
        		//System.out.println("LAZY PARAM: " + selectedDrzava);
        		String datumOd = (String) searchParam.get("datumOd");
        		System.out.println("LAZY PARAM: " + datumOd);
        		String datumDo = (String) searchParam.get("datumDo");
        		System.out.println("LAZY PARAM: " + datumDo);
        		String selectedOrg = (String) searchParam.get("selectedOrg");
        		System.out.println("LAZY PARAM: " + selectedOrg);
    			
        		pstmt1 = conn.prepareStatement(generateQuery(false, storno));
    			
        		pstmt1.setString(1, kod.toUpperCase());
        		pstmt1.setString(2, naziv.toUpperCase());
    			//pstmt1.setString(3, selectedDrzava);
    			pstmt1.setString(3, datumOd);
    			pstmt1.setString(4, datumDo);
        		
        		if (!storno){
        			
					if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin ||  rola == Const.ROLA_UPRAVA_2){
						pstmt1.setString(5, selectedOrg);
						//rs =  trx.executePreparedQueryById("test", "CountSearchPrintedCertificatesLanguages", parm);
						
					} else if (rola == 2 || rola == 12){
						//parm.add(ub.getIdOrganizacije());
						pstmt1.setString(5, String.valueOf(ub.getIdOrganizacije()));
						//rs =  trx.executePreparedQueryById("test", "CountSearchPrintedCertificatesLanguagesOrg", parm);
					}
					
					rs = pstmt1.executeQuery();
					
				}
				else {
					
					if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin || rola == Const.ROLA_UPRAVA_2){
						pstmt1.setString(5, selectedOrg);
						//rs =  trx.executePreparedQueryById("test", "CountSearchPrintedCertificatesLanguagesStorno2", parm);
					} else if (rola == 2 || rola == 12){
						//parm.add(ub.getIdOrganizacije());
						pstmt1.setString(5, String.valueOf(ub.getIdOrganizacije()));
						//rs =  trx.executePreparedQueryById("test", "CountSearchPrintedCertificatesLanguagesStornoOrg", parm);
					}
					
					rs = pstmt1.executeQuery();
				}
        	}
			else {
				
				trx = ctxA.getDatabaseManager().createTransaction("sys");
						
				if (!storno){
        			if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin || rola == Const.ROLA_UPRAVA_2 || rola == Const.ROLA_DVI.intValue() || rola == Const.ROLA_INSPEKTOR.intValue() ){
        				rs =  trx.executePreparedQueryById("test", "CountPrintedCertificatesLanguages", parm);
        			} else if (rola == 2 || rola == 12){
        				parm.add(ub.getIdOrganizacije());
        				rs =  trx.executePreparedQueryById("test", "CountPrintedCertificatesLanguagesOrg", parm);
        			}
        		} 
        		else {
        			if (rola == Const.ROLA_ADMIN_CERTIFIKATI || certAdmin || rola == Const.ROLA_UPRAVA_2 || rola == Const.ROLA_DVI.intValue() || rola == Const.ROLA_INSPEKTOR.intValue()){
        				rs =  trx.executePreparedQueryById("test", "CountPrintedCertificatesLanguagesStorno2", parm);
        			} else if (rola == 2 || rola == 12){
        				parm.add(ub.getIdOrganizacije());
        				rs =  trx.executePreparedQueryById("test", "CountPrintedCertificatesLanguagesStornoOrg", parm);
        			}
        		}
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
	
	private String generateQuery(boolean search, boolean storno){
		String queryString = null;
		String status = null;
		String status2 = null;
		String addOrg = null;
		
		try {
			//storno status
			if (storno){
				status = " t0.activity = 1 and t1.status = '0' and t1.activity = 0 ";
				status2 = " t1.status = '0' and t1.activity = 0 ";
			}
			else {
				status = " t0.activity = 1 and t1.status = '1' and t1.activity = 1 ";
				status2 = " t1.status = '1' and t1.activity = 1 ";
			}
			
			//dodati id_org
			addOrg = " and t1.id_regional_office like nvl(?, '%') ";
			
    		if (search){
    			if (!storno){
		        	queryString = "SELECT * FROM " +
					"( " + 
					    "SELECT a.*, rownum r__ " +
					    "FROM ( " +
					    	"select distinct t1.ID_DOCUMENT, t1.id_certificate, t2.code, t2.text, (select listagg(t6.name,', ') within group (order by t0.id) from vis_ex.cert_cert_lang t0 left join sm.sm_languages t6 on (t0.id_language = t6.id_language) where t0.id_certificate = t2.id and t0.activity = 1) as jezici, t3.id_measure, t3.name, " + 
							"null as id, (select listagg(t4.text,', ') within group (order by t4.id) from vis_ex.cert_states tcs " +
							"left join sm.sm_states t4 on (tcs.id_state = t4.id) where tcs.id_certificate = t2.id and tcs.activity = 1) as text2, t1.serial_no, t2.price, t1.d_issue, t5.id_organization, t5.name1, t2.serija " +
							"from vis_ex.cert_documents t1 " +
							"left join vis_ex.cert_certificates t2 on (t2.id = t1.id_certificate) " +
							"left join vis_ex.cert_measure t3 on (t2.id_measure = t3.id_measure) " +
							"left join sm.sm_organizations t5 on (t1.id_regional_office = t5.id_organization) " +
							"left join vis_ex.cert_cert_lang t0 on (t2.id = t0.id_certificate and t0.activity = 1) " +
							"left join vis_ex.cert_states t4 on (t2.id = t4.id_certificate and t4.activity = 1) " +
							"where " + status + " and t2.code like ? and upper(t2.text) like ? and (t1.d_issue >= TO_DATE(?,'dd.MM.yyyy') or t1.d_issue <= TO_DATE(?,'dd.MM.yyyy')) " + addOrg;
    			}
    			else {
    				queryString = "SELECT * FROM " +
					"( " + 
					    "SELECT a.*, rownum r__ " +
					    "FROM ( " +
						    "select distinct t1.ID_DOCUMENT, t1.id_certificate, t2.code, t2.text, (select listagg(t6.name,', ') within group (order by t0.id) from vis_ex.cert_cert_lang t0 left join sm.sm_languages t6 on (t0.id_language = t6.id_language) where t0.id_certificate = t2.id and t0.activity = 1) as jezici, t3.id_measure, t3.name, " + 
							"null as id, (select listagg(t4.text,', ') within group (order by t4.id) from vis_ex.cert_states tcs " +
							"left join sm.sm_states t4 on (tcs.id_state = t4.id) where tcs.id_certificate = t2.id and tcs.activity = 1) as text2, t1.serial_no, t2.price, t1.d_issue, t5.id_organization, t5.name1, t2.serija " +
							"from vis_ex.cert_documents t1 " +
							"left join vis_ex.cert_certificates t2 on (t2.id = t1.id_certificate) " +
							"left join vis_ex.cert_measure t3 on (t2.id_measure = t3.id_measure) " +
							"left join sm.sm_organizations t5 on (t1.id_regional_office = t5.id_organization) " +
							"left join vis_ex.cert_cert_lang t0 on (t2.id = t0.id_certificate and t0.activity = 1) " +
							"left join vis_ex.cert_states t4 on (t2.id = t4.id_certificate and t4.activity = 1) " +
							"where " + status + " and t2.code like ? and upper(t2.text) like ? and (t1.datum_opoziva >= TO_DATE(?,'dd.MM.yyyy') or t1.datum_opoziva <= TO_DATE(?,'dd.MM.yyyy')) " + addOrg;
    			}
    		}
    		else {
    			if (selectedJezici.length > 0 || selectedDrzava.length > 0 || selectedMjera.size() > 0){
    			queryString = "select count(distinct a.id_certificate) " +
    						  "from ( " +
	    						"select distinct t1.ID_DOCUMENT, t1.id_certificate, t2.code, t2.text, (select listagg(t6.name,', ') within group (order by t0.id) from vis_ex.cert_cert_lang t0 left join sm.sm_languages t6 on (t0.id_language = t6.id_language) where t0.id_certificate = t2.id and t0.activity = 1) as jezici, t3.id_measure, t3.name, " + 
	  							"null as id, (select listagg(t4.text,', ') within group (order by t4.id) from vis_ex.cert_states tcs " +
	  							"left join sm.sm_states t4 on (tcs.id_state = t4.id) where tcs.id_certificate = t2.id and tcs.activity = 1) as text2, t1.serial_no, t2.price, t1.d_issue, t5.id_organization, t5.name1, t2.serija " +
	  							"from vis_ex.cert_documents t1 " +
	  							"left join vis_ex.cert_certificates t2 on (t2.id = t1.id_certificate) " +
	  							"left join vis_ex.cert_measure t3 on (t2.id_measure = t3.id_measure) " +
	  							"left join sm.sm_organizations t5 on (t1.id_regional_office = t5.id_organization) " +
	  							"left join vis_ex.cert_cert_lang t0 on (t2.id = t0.id_certificate and t0.activity = 1) " +
	  							"left join vis_ex.cert_states t4 on (t2.id = t4.id_certificate and t4.activity = 1) " +
    						  	"where " + status + " and t2.code like ? and upper(t2.text) like ? and (t1.d_issue >= TO_DATE(?,'dd.MM.yyyy') or t1.d_issue <= TO_DATE(?,'dd.MM.yyyy')) " + addOrg;
    			}
    			else {
    				queryString = "select count(*) " + 
    							  "from vis_ex.cert_documents t1 " + 
    							  "left join vis_ex.cert_certificates t2 on (t2.id = t1.id_certificate) " +
    							  "where " + status2 + " and t2.code like ? and upper(t2.text) like ? and (t1.d_issue >= TO_DATE(?,'dd.MM.yyyy') or t1.d_issue <= TO_DATE(?,'dd.MM.yyyy')) " + addOrg;  
    			}
    		}
    		
        	if (selectedJezici.length > 0){
        		queryString = queryString + " and t0.id_language in ( ";
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
        		queryString = queryString + " and t2.id_measure in (";
        		for (String  m : selectedMjera){
        			queryString = queryString + m + ",";
        		}
        		queryString = queryString.substring(0, queryString.length() - 1);
        		queryString = queryString + " ) ";
        	}
					
			if (search){	
				if (!storno){
		        	queryString = queryString + 		
							//"group by t1.ID_DOCUMENT, t1.id_certificate, t2.code, t2.text, t3.id_measure, t3.name, t4.id, t4.text, t1.serial_no, t2.price, t1.d_issue, t5.id_organization, t5.name1, t2.serija " +
							"order by t1.ID_DOCUMENT desc, t1.id_certificate, t2.code " +
						") a " +
					   	"where rownum < ? " +
					") " +
					"WHERE r__ >= ?";
	        	}
				else {
					queryString = queryString + 		
							//"group by t1.ID_DOCUMENT, t1.id_certificate, t2.code, t2.text, t3.id_measure, t3.name, t4.id, t4.text, t1.serial_no, t2.price, t1.datum_opoziva, t5.id_organization, t5.name1, t2.serija " +
							"order by t1.ID_DOCUMENT desc, t1.id_certificate, t2.code " +
						") a " +
					   	"where rownum < ? " +
					") " +
					"WHERE r__ >= ?";
				}
			}
			else if (selectedJezici.length > 0 || selectedDrzava.length > 0 || selectedMjera.size() > 0){
				queryString = queryString + " ) a";
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

	public boolean isStorno() {
		return storno;
	}

	public void setStorno(boolean storno) {
		this.storno = storno;
	}

	public boolean isCertAdmin() {
		return certAdmin;
	}

	public void setCertAdmin(boolean certAdmin) {
		this.certAdmin = certAdmin;
	}
	
}
