package hr.kingict.svis.reports;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

public class ReportGenerator {
	 	private DSGeneric ds = null;
	    private String reportName=null;
	    
	    protected String reportHTML="";
	    protected String reportRTF="";
	    private byte[] report = null;
	    private Map<String, Object> parameters = null;
	    
	    private String type;
	    
	    public ReportGenerator() {
	    	
	    }	     

	    public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getReport() throws ReportException {
	    	
	    	ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
            String path=context.getRealPath("/reports/grb2.png");
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();   
            
	 
	        try {
	        	 if (parameters == null) 
	        		 parameters = new HashMap<String, Object>();
	        	 
	        	 parameters.put("grb",path);
	        	 
	        	 if (type.equals("pdf")) {
	        		 report=renderPdf(getReportName(), parameters, ds);
	        		 response.resetBuffer();
	 	             response.setContentLength(report.length);
	 	             response.setContentType("application/"+type); 
	 	             response.setHeader("Content-disposition", "inline;");
	 	             response.setHeader("Cache-Control", "cache, must-revalidate");
	 	             response.setHeader("Pragma", "public");
	 	             response.getOutputStream().write(report);
	 	             response.getOutputStream().flush();            
	 	             FacesContext.getCurrentInstance().responseComplete();   
	        	 }
	        	 if (type.equals("html")) {
	                 String html=""; 
	                 if (reportHTML.equals("")) 
	                	 reportHTML=renderHTML(reportName, 0, parameters,ds);
	                 else
	                     html=reportHTML;
	                 response.reset();
	                 
	                 String datoteka = "inline; filename=" + reportName + ".html";
	                 response.setContentType("application/html");
		     		 response.setHeader("Content-Disposition", datoteka);
		     		 response.setHeader("Expires", "");
		     		 response.setHeader("Cache-Control", "");
		     		 response.setHeader("Pragma", "");
	                 response.getWriter().write(html);
	                 response.setCharacterEncoding("UTF-8");
	                 response.setLocale(new Locale("hr"));
	                 response.setContentType("text/html");      
	                 return "";
	              }
	             
	              if (type.equals("rtf")) {
	             	 
	                response.resetBuffer();

	     			if (reportRTF.equals(""))
	     				reportRTF = renderRTF(reportName, parameters, ds);

	     			String datoteka = "inline; filename=" + reportName + ".rtf";
	     			response.setContentType("application/msword");
	     			response.setHeader("Content-Disposition", datoteka);
	     			response.setHeader("Expires", "");
	     			response.setHeader("Cache-Control", "");
	     			response.setHeader("Pragma", "");
	     			response.setContentLength(reportRTF.getBytes().length);
	     			response.getOutputStream().write(reportRTF.getBytes());
	     			response.getOutputStream().flush();
	     			FacesContext.getCurrentInstance().responseComplete(); 	       
	              }
	             
	              if (type.equals("xls")) {
	                 
//	     			if (report == null)
//	     				report = renderXLS(reportName, parameters, ds, null);
//	     			
//
//	     			String datoteka = "inline; filename=" + reportName + ".xls";
//	     			response.resetBuffer();
//	     			response.setContentLength(report.length);
//	     			response.setContentType("application/vnd.ms-excel");
//	     			response.setHeader("Content-Disposition", datoteka);
//	     			response.setHeader("Expires", "");
//	     			response.setHeader("Cache-Control", "");
//	     			response.setHeader("Pragma", "");
//	     			response.getOutputStream().write(report);
//	     			response.getOutputStream().flush();
//	     			FacesContext.getCurrentInstance().responseComplete();      
	              }
	             
	             
	             
	              
	        } catch (Exception ex) {
	        	ex.printStackTrace();	           
	            throw new ReportException(ex.getMessage());
	        }

		 return null;
	    }
	    
	    
	    protected void formirajReport(HashMap<String,Object> parameters, Integer headerRows) throws ReportException{
	    	
	    	try {    
	            report = renderPdf(getReportName(), parameters, ds);
	    	} catch (Exception ex) {
	            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
	            throw new ReportException(ex.getMessage());
	    	}     
	    }
	    
	    
	    public byte[] renderPdf(String reportName, Map args, DSGeneric ds) throws ReportException {
	          

	    	try {
	    		ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
	            String path=context.getRealPath("/reports/"+reportName+".jasper");
	            
	            System.out.println("Path: " + path);
	            
	            FileInputStream file = new FileInputStream(path);
	            JasperPrint jasperPrint = JasperFillManager.fillReport(file,args, ds);     
				report =JasperExportManager.exportReportToPdf(jasperPrint);
	            return report;
			} catch (Exception ex) {
				Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
		        throw new ReportException(ex.getMessage());
			}
	    } 
	  
	    
	    public String renderHTML(String reportName, int pageIndex, Map parameters, JRDataSource ds) throws Exception {

			StringBuffer reportString = new StringBuffer();
			parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
			JasperPrint jasperPrint = null;
			
			ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
            String path=context.getRealPath("/reports/"+reportName+".jasper");

			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(path);
			jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);

			JRHtmlExporter exporter = new JRHtmlExporter();
			
			// >> Potrebno za prikaz slika na HTML-u
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

			
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STRING_BUFFER, reportString);
			exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER, "");
			exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
			exporter.setParameter(JRHtmlExporterParameter.HTML_FOOTER, "");
			exporter.setParameter(JRHtmlExporterParameter.CHARACTER_ENCODING, "UTF-8");
			exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, "pt");
			exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, true);
			int lastPageIndex = 0;
			if (jasperPrint.getPages() != null) {
				lastPageIndex = jasperPrint.getPages().size() - 1;
			}

			if (pageIndex < 0) {
				pageIndex = 0;
			}

			if (pageIndex > lastPageIndex) {
				pageIndex = lastPageIndex;
			}
			if (pageIndex != 0) {
				exporter.setParameter(JRExporterParameter.PAGE_INDEX, new Integer(pageIndex));
			}
			exporter.exportReport();
			return reportString.toString();
		}

	    
	    public String renderRTF(String reportName, Map parameters, JRDataSource ds) throws Exception {
			StringBuffer reportString = new StringBuffer();			
			JasperPrint jasperPrint = null;
			JasperReport jasperReport; 
			
			ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
            String path=context.getRealPath("/reports/"+reportName+".jasper");
			
			jasperReport = (JasperReport) JRLoader.loadObject(path);

			jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);

			JRRtfExporter exporter = new JRRtfExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
			exporter.setParameter(JRExporterParameter.OUTPUT_STRING_BUFFER, reportString);

			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "../../image?image=");
			exporter.exportReport();

			return reportString.toString();

		}

	    
	    
//	    public byte[] renderXLS(String reportName,Map parameters, JRDataSource ds, Integer headerRows) throws Exception {
//		
//	    	ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
//            String path=context.getRealPath("/reports/"+reportName+".jasper");
//	    	
//			JasperPrint jasperPrint = null;
//			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(path);
//			
//			// samo za xls
//			parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
//
//			jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
//
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//			// Umjesto JRXlsExporter je potrebno koristiti JExcelApiExporter
//			// jer Jasperov engine ne generira dobro xls u nekim slucajevima (pukne Excel kod otvaranja)
//			// NFulir
////			JRXlsExporter exporter = new JRXlsExporter();
//			JExcelApiExporter exporter = new JExcelApiExporter();
//			exporter.setParameter(JExcelApiExporterParameter.JASPER_PRINT, jasperPrint);
//			exporter.setParameter(JExcelApiExporterParameter.CHARACTER_ENCODING, "UTF-8");
//			exporter.setParameter(JExcelApiExporterParameter.OUTPUT_STREAM, bos);
//			
//			// samo za xls
//			exporter.setParameter(JExcelApiExporterParameter.IGNORE_PAGE_MARGINS, true);
//			exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, true);
//			exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true);
//			exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
//			exporter.exportReport();
//			
//			return formatXLS(bos, jasperReport, headerRows);
//		}
		

//		private byte[] formatXLS (ByteArrayOutputStream bos, JasperReport jasperReport, Integer headerRows) throws IOException {		
//			ByteArrayInputStream baris = new ByteArrayInputStream(bos.toByteArray());
//			HSSFWorkbook wb = new HSSFWorkbook(baris);
//			HSSFSheet sheet = wb.getSheetAt(0);
//			// Generiranje statickog headera:
//			if (headerRows != null) {
//				// Title bar header:
//				if (headerRows.compareTo(new Integer(0)) == 0) {
//					Integer titleBarHeight = jasperReport.getTitle().getHeight();
//					Integer freezeRows = 0;
//					List<JRBaseElement> elements = new ArrayList<JRBaseElement>();
//					List<JRBaseElement> baseTitleElements = jasperReport.getTitle().getChildren();
//					for (JRBaseElement e : baseTitleElements) {
//						if (e instanceof JRBaseSubreport) {
//							freezeRows = freezeRows + 2;
//						} else {
//							elements.add(e);
//						}
//					}
//					List<Integer> heights = new ArrayList<Integer>();
//					for (JRBaseElement e : elements) {
//						Integer elementHeight = new Integer(e.getY() + e.getHeight());
//						if (elementHeight.compareTo(new Integer(0)) > 0 && (new Integer(e.getY())).compareTo(titleBarHeight) < 0) {
//							if (!heights.contains((Integer) elementHeight)){
//								heights.add(elementHeight);
//							}
//						}
//					}
//					if (!heights.isEmpty()) {
//						if (jasperReport.getColumnHeader().getHeight() > 0) {
//							// Ulazi i column header
//							freezeRows = freezeRows + heights.size() + 1;						
//						} else {
//							freezeRows = freezeRows + heights.size();
//						}
//						sheet.createFreezePane(0, freezeRows);
//					}
//				// Eksplicitno zadani header:
//				} else if (headerRows.compareTo(new Integer(0)) > 0) {
//					sheet.createFreezePane(0, headerRows);
//				}
//			}
//			// Postavljanje ispravnih margina:
//			if (jasperReport.getOrientationValue().getValue() == OrientationEnum.PORTRAIT.getValue()) {
//				sheet.setMargin(Sheet.TopMargin, 0.4);
//				sheet.setMargin(Sheet.BottomMargin, 0.4);
//				sheet.setMargin(Sheet.LeftMargin, 1.4);
//				sheet.setMargin(Sheet.RightMargin, 0.7);
//			} else {
//				sheet.setMargin(Sheet.TopMargin, 1.4);
//				sheet.setMargin(Sheet.BottomMargin, 0.7);
//				sheet.setMargin(Sheet.LeftMargin, 0.4);
//				sheet.setMargin(Sheet.RightMargin, 0.4);
//			}
//			ByteArrayOutputStream baros = new ByteArrayOutputStream();
//			wb.write(baros);
//			return baros.toByteArray();
//		}
	    
	    

	    public JRDataSource getDs() {
	        return ds;
	    }


	    public void setDs(DSGeneric ds) {
	        this.ds = ds;
	    }


	    public String getReportName() {
	        return reportName;
	    }

	  
	    public void setReportName(String reportName) {
	        this.reportName = reportName;
	    }

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}

}
