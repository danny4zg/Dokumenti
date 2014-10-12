package hr.kingict.svis.obrasci.util;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.DatabaseManagerNotAvailableException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.database.TransactionCreationException;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

public final class ReportUtil {
	protected static ReportUtil instance = null;
	protected IApplicationContext ctxA = null;
	
	public ReportUtil() {
		this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
	}
	
	private static void init() {
		if (instance == null) {
			instance = new ReportUtil();
		}
	}

	public static StreamedContent generateExcelReport(String query, String template, String output_name, Map<Integer, Object> map){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs;
		StreamedContent fileDownload = null;
		
		try {
			init();
			
			String destination = Const.TMP_DIRECTORY;
			
			DateFormat dateFormat = new SimpleDateFormat("HHmmssSSS");
	        Date date = new Date();
	        String tempMark = dateFormat.format(date);
	        
	        String filePath = output_name + tempMark + ".xls";
	        
	        ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
	        String path1 = context.getRealPath("/reports/" + template + ".jasper");

    		//System.out.println(path1);
    		
	    	FileInputStream file1 = new FileInputStream(path1);
			
	    	for (Map.Entry<Integer, Object> entry : map.entrySet()) {
	    	    //Integer key = entry.getKey();
	    	    Object value = entry.getValue();
	    	    parm.add(String.valueOf(value));
	    	}
	   
	    	trx = instance.ctxA.getDatabaseManager().createTransaction("sys");
	    	rs =  trx.executePreparedQueryById("test", query, parm);
	    	
			JRResultSetDataSource datasource = new JRResultSetDataSource(rs);
			JasperPrint jp = JasperFillManager.fillReport(file1, new HashMap(), datasource);
        	
			JRXlsExporter exporter = new JRXlsExporter();
	           
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET,Boolean.FALSE);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp); //Note : jp is JasperPrint Object 
			
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destination + filePath);
            
            exporter.setParameter(JRXlsExporterParameter.SHEET_NAMES, new String[]{output_name});
            exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS,Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.OFFSET_X, 0);
            exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BORDER, Boolean.FALSE);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, true);
            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
			exporter.exportReport();
            
			//List lCustomColumnNames = Arrays.asList(new String [] {"Red. broj","Datum izdavanja","Oznaka (HVI)", "Broj certifikata", "Vrsta certifikata", "Prijeđeni kilometri", "Ime i prezime ovlaštene osobe za izdavanje certifikata", "VKB/JIBG", "Subjekt kojem je izdan certifikat"});
			//ctxA.getReportManager().createExcelReport(destination + filePath, "Izdani certifikati", rs);//, lCustomColumnNames);
            InputStream stream = new FileInputStream(destination + filePath);
            fileDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", output_name + ".xls");
			
		    stream = new FileInputStream(destination + filePath);//((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/images/optimusprime.jpg");
			
			rs.close();
			
		}	
		 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseManagerNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
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
		}
		
		return fileDownload;
	}
	
	
	public static StreamedContent generatePdfReport(String query, String template, String output_name, Map<Integer, Object> map){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs;
		StreamedContent fileDownload = null;
		
		try {
			init();
			
			String destination = Const.TMP_DIRECTORY;
			
			DateFormat dateFormat = new SimpleDateFormat("HHmmssSSS");
	        Date date = new Date();
	        String tempMark = dateFormat.format(date);
	        
	        String filePath = output_name + tempMark + ".pdf";
	        
	        ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
	        String path1 = context.getRealPath("/reports/" + template + ".jasper");

    		//System.out.println(path1);
    		
	    	FileInputStream file1 = new FileInputStream(path1);
	    	Map parameters = new HashMap();
	    	
	    	String datumOd = null;
	    	String datumDo = null;
	    	
	    	for (Map.Entry<Integer, Object> entry : map.entrySet()) {
	    	    Integer key = entry.getKey();
	    	    Object value = entry.getValue();
	    	    
	    	    if (key.intValue() < 15) parm.add(String.valueOf(value));
	    	    
	    	    if (key.intValue() == 1) {
	    	    	datumOd = String.valueOf(value);
	    	    }
	    		else if (key.intValue() == 2){
	    			datumDo = String.valueOf(value);
	    		}
	    		else if (key.intValue() == 15){
	    			//System.out.println("Zvjezdica 1 " + String.valueOf(value));
	    			if (String.valueOf(value).equals("1")) datumOd = Const.STAR;
	    		}
	    		else if (key.intValue() == 16){
	    			//System.out.println("Zvjezdica 2 " + String.valueOf(value));
	    			if (String.valueOf(value).equals("1")) datumDo = Const.STAR;
	    		}
	    	}
	    	
	    	parameters.put("datum_od", datumOd);
    	    parameters.put("datum_do", datumDo);
	   
	    	trx = instance.ctxA.getDatabaseManager().createTransaction("sys");
	    	rs =  trx.executePreparedQueryById("test", query, parm);
	    	
	    	JRResultSetDataSource datasource = new JRResultSetDataSource(rs);
			JasperPrint jp = JasperFillManager.fillReport(file1, parameters, datasource);
	            
	        JRPdfExporter exporter = new JRPdfExporter();
		    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp); //Note : jp is JasperPrint Object 
			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destination + filePath);
			exporter.exportReport();    
	    	
			File test = new File(destination + filePath);
			
			if (test.isFile() && test.canRead()){
			
				System.out.println(" FAJL POSTOJI ");
				
				InputStream stream = new FileInputStream(destination + filePath);
				fileDownload = new DefaultStreamedContent(stream, "application/pdf", output_name + ".pdf");
			
				stream = new FileInputStream(destination + filePath);//((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/images/optimusprime.jpg");
			}
			rs.close();
            
		}	
		 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseManagerNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
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
		}
		
		return fileDownload;
		
	} 
	
	public static StreamedContent generateExcelReportAdvanced(String query, String template, String output_name, Map<Integer, Object> map){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		StreamedContent fileDownload = null;
		
		try {
			init();
			
			String destination = Const.TMP_DIRECTORY;
			
			DateFormat dateFormat = new SimpleDateFormat("HHmmssSSS");
	        Date date = new Date();
	        String tempMark = dateFormat.format(date);
	        
	        String filePath = output_name + tempMark + ".xls";
	        
	        ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
	        String path1 = context.getRealPath("/reports/" + template + ".jasper");

    		//System.out.println(path1);
    		
	    	FileInputStream file1 = new FileInputStream(path1);
			
	    	for (Map.Entry<Integer, Object> entry : map.entrySet()) {
	    	    //Integer key = entry.getKey();
	    	    Object value = entry.getValue();
	    	    parm.add(String.valueOf(value));
	    	}
	   
	    	trx = instance.ctxA.getDatabaseManager().createTransaction("sys");
	    	rs =  trx.executePreparedQueryById("test", query, parm);
	    	
	    	Map parameters = new HashMap();
	    	parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
	    	
			JRResultSetDataSource datasource = new JRResultSetDataSource(rs);
			JasperPrint jp = JasperFillManager.fillReport(file1, parameters, datasource);
			
			// Umjesto JRXlsExporter je potrebno koristiti JExcelApiExporter
			// jer Jasperov engine ne generira dobro xls u nekim slucajevima (pukne Excel kod otvaranja)
			// NFulir

			JExcelApiExporter exporter = new JExcelApiExporter();
			
			exporter.setParameter(JExcelApiExporterParameter.JASPER_PRINT, jp);
			exporter.setParameter(JExcelApiExporterParameter.CHARACTER_ENCODING, "UTF-8");
		    
			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destination + filePath);
		       
			// samo za xls
			exporter.setParameter(JExcelApiExporterParameter.IGNORE_PAGE_MARGINS, true);
			exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, true);
			exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true);
			exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);

			exporter.exportReport();
            
			//List lCustomColumnNames = Arrays.asList(new String [] {"Red. broj","Datum izdavanja","Oznaka (HVI)", "Broj certifikata", "Vrsta certifikata", "Prijeđeni kilometri", "Ime i prezime ovlaštene osobe za izdavanje certifikata", "VKB/JIBG", "Subjekt kojem je izdan certifikat"});
			//ctxA.getReportManager().createExcelReport(destination + filePath, "Izdani certifikati", rs);//, lCustomColumnNames);
            InputStream stream = new FileInputStream(destination + filePath);
            fileDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", output_name + ".xls");
			
		    stream = new FileInputStream(destination + filePath);
			
		}	
		 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseManagerNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
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
		
		return fileDownload;
	}
	
}
