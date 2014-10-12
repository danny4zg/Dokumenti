package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.util.SearchUtil;
import hr.kingict.svis.obrasci.web.Racun;
import hr.kingict.svis.obrasci.web.RacunGroupHelperBean;
import hr.kingict.svis.obrasci.web.RacunHelperBean;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.primefaces.model.LazyDataModel;
 
@ManagedBean(name="obracun")
@ViewScoped
public class ObracunBean {
	protected IApplicationContext ctxA = null;
	
	private String subjekt;
	
	private Date datumOd;
	private Date datumDo;
	
	private List<String> selectedStatus = new ArrayList<String>();
	private Map statusi = new HashMap<String, String>();

	private LazyDataModel<Racun> lazyModel;
	
	private Integer selected; 
	private int rola;

	private Boolean isAdmin;
	
	private Integer getBrojRacunaGod(ITransaction trx){
		Integer brojRacuna = null;
		ResultSet rs = null;
		List parm = new ArrayList();
		
		try {
			rs =  trx.executePreparedQueryById("test", "GetBrRacGod", parm);
			if (rs.next()){
				brojRacuna = rs.getInt("value_number");
			}
			
			rs.close();
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return brojRacuna;
	}

	String path = null;

	private Integer brojRacuna;
	private Integer podrucje;
	
	public ObracunBean(){
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			
			rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();

			isAdmin = (Boolean) context.getExternalContext().getSessionMap().get("certadmin");
			
			if (rola != 4 && !isAdmin){
				context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
			}
			else {
				this.ctxA = ApplicationContextFactory.getCurrentContext();
				trx = ctxA.getDatabaseManager().createTransaction("sys");
	
				//status plaćanja
				rs =  trx.executePreparedQueryById("test", "SelectRacunStatus", parm);
				
				while (rs.next()){
					if (!rs.getString(1).equals("2")) statusi.put(rs.getString(2), rs.getString(1));//makni djelomično plaćen
				}
				rs.close();
				
				lazyModel = new LazyObracunDataModel();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			if (trx != null)
				try {
					trx.close();
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
	}
	
	public String getDetail(){
		try {

			FacesContext context = FacesContext.getCurrentInstance();
			
			for (Racun o : (List<Racun>) lazyModel.getWrappedData()){
				if (o.getIdRacun().intValue() == this.selected.intValue()){
					context.getExternalContext().getSessionMap().put("rac", o);
					break;
				}
			}
			
	        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "racun_detail?faces-redirect=true");
	 	}
	 	catch (Exception ex){
	 		System.out.println(ex.getMessage());
	 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "System Error",  "Please try again later.");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	 	}
	 	
	 	return null;
	}
	
	public void search(){
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("subjekt", SearchUtil.testIfContainsMode(getSubjekt()));
			
			String pattern = "dd.MM.yyyy";
		    SimpleDateFormat format = new SimpleDateFormat(pattern);
			
			map.put("datumOd", getDatumOd() == null ? format.format(new Date(112,1,1)) : format.format(getDatumOd()));
			map.put("datumDo", getDatumDo() == null ? format.format(new Date()) : format.format(getDatumDo()));
			map.put("statusi", selectedStatus);
			map.put("podrucje", getPodrucje() == null ? new Integer(0) : getPodrucje());
			
			lazyModel = new LazyObracunDataModel(map);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void showPdf(){
		
	}
	
	private boolean updateBrojRacunaGod(ITransaction trx, Integer value){
		boolean status = false;
		List parm = new ArrayList();
		
		try {
			parm.add(value);
			trx.executePreparedUpdateById("test", "UpdateBrRacGod", parm);
			trx.commit();
			
			status = true;
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return status;
	}
	
	public void clear(ActionEvent e){
		setDatumOd(null);
		setDatumDo(null);
		setSelectedStatus(new ArrayList<String>());
		setSubjekt("");
		setPodrucje(null);
	}

	//obracun logika
	
	public void napraviObracun() throws DatabaseException, SQLException{
		String min = null;
		String max = null;
		
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		
		try{
			ctxA = ApplicationContextFactory.getCurrentContext();//framework init
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			Calendar calendar = Calendar.getInstance();
			Calendar datumOd = Calendar.getInstance();
			Calendar datumDo = Calendar.getInstance();
			
			int day = calendar.get(Calendar.DATE);
	        int month = calendar.get(Calendar.MONTH);//0-11
	        int year = calendar.get(Calendar.YEAR);
	        int maxDay = 1;
	        
	        trx = ctxA.getDatabaseManager().createTransaction("sys");
	        
	        if (month == 0) {
	        	//*Postaviti brojač za račune na 1*//
	        	try {
	        		if (updateBrojRacunaGod(trx, Const.POCETNI_BROJ_RACUNA)) setBrojRacuna(1);
	        		else System.out.println("NIJE RESETIRAN BROJAČ...");
	        	}
	        	catch (Exception e){
	        		e.printStackTrace();
	        	}
	        	
				year = year - 1;
				month = 12;
				calendar.set(year, month - 1, day);//test mjesec 2 param
				maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			} else {
				
				setBrojRacuna(getBrojRacunaGod(trx));
				
				calendar.set(year, month - 1, day);//test mjesec 2 param
				maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			}
	        
	        /*datumOd.set(year, month - 1, 1); //test mjesec 2 param
	        datumDo.set(year, month - 1, maxDay); //test mjesec 2 param*/
	        
	        datumOd.set(year, month, 1); //test mjesec 2 param
	        datumDo.set(year, month, maxDay); //test mjesec 2 
	        
	        System.out.println(sdf.format(datumOd.getTime()));
	        System.out.println(sdf.format(datumDo.getTime()));
	        
			min=sdf.format(datumOd.getTime());
			max=sdf.format(datumDo.getTime());
			
			System.out.println("OBRAČUN OD - DO -> " + min + ", " + max);
			
			parm.add(min);
			parm.add(max);
			rs =  trx.executePreparedQueryById("test", "ObracunCert", parm);
			
			ArrayList<RacunHelperBean> lJasperData = new ArrayList<RacunHelperBean>();
			ArrayList<RacunHelperBean> lJasperDataPrevious = new ArrayList<RacunHelperBean>();
			
			List<Integer> lradniNalog = new ArrayList<Integer>();
			Integer idRadniNalog = null;
			
			Map param = new HashMap();
			ResultSet rs1;
			List parm1 = new ArrayList();
			
			RacunHelperBean rhb = new RacunHelperBean();
			BigDecimal suma = BigDecimal.ZERO;
			
			boolean testIfObracunEmpty = false;
			
			if (rs.next()){	
				rhb.setIdDokument(rs.getInt("id_document"));
				rhb.setIdSubjekt(rs.getInt("id_subject"));
				rhb.setIdCertifikat(rs.getInt("id_certificate"));
				rhb.setJedCijena(rs.getBigDecimal("price"));
				rhb.setKodCertifikat(rs.getString("cert_code"));
				rhb.setNazivCertifikat(rs.getString("cert_name"));

				idRadniNalog = rs.getInt("id_document");
				lradniNalog.add(idRadniNalog);
				suma = suma.add(rs.getBigDecimal("price"));
				
				param.put("D1", min);
				param.put("D2", max);

				param.put("NAZIV_SUBJEKTA", rs.getString("sub_name"));
				param.put("OIB", rs.getString("vat_no"));
				param.put("SIFRA_KUPCA", rs.getString("sif_kupca"));
				param.put("ULICA", rs.getString("street") + " " + rs.getString("hn"));
				param.put("PBR_MJESTO", rs.getString("code") + " " + rs.getString("name"));
				param.put("MJESEC_OBRACUNA", mjesecObracuna(month-1));
				param.put("GODINA_OBRACUNA", String.valueOf(year));
				param.put("TEST", Const.TEST);
				
				//System.out.println(rhb.getIdDokument() +  " " + rhb.getKodCertifikat());
				
			}
			else {
				testIfObracunEmpty = true;
			}
						
			if (!testIfObracunEmpty){
				
				ITransaction trx2 = ctxA.getDatabaseManager().createTransaction("sys");//za update
				
				while (rs.next()){

					System.out.println("SUBJEKT: " + rs.getInt("id_subject"));

					if (rhb.getIdSubjekt().intValue() == rs.getInt("id_subject")){
						lJasperData.add(rhb);	
					}
					else {
						lJasperDataPrevious.clear();
						lJasperData.add(rhb);
						lJasperDataPrevious.addAll(lJasperData);
						
						System.out.println("ADDING: " + rhb.getKodCertifikat());
						
						//insert racun
						//update radni_nalog
						
						List p1 = new ArrayList();
						ResultSet rs2 = null;
						
						try {
							Integer seqIdRacun = null;
							rs2 =  trx2.executePreparedQueryById("test", "GetSequenceRacun", p1);
							setBrojRacuna(getBrojRacuna() + 1);
							
							System.out.println("------------------------------------------------------------");
							
							if (rs2.next()){
								seqIdRacun = rs2.getInt(1);
							}
							
							//param.put("ID_RACUNA", seqIdRacun);//ovo je seqnum kada se zapisuje u tablicu racun
							param.put("ID_RACUNA", getBrojRacuna().toString());//ovo je seqnum kada se zapisuje u tablicu racun
							
							System.out.println("generiranje reporta: " + param.get("NAZIV_SUBJEKTA"));
							generateCertReport(param.get("NAZIV_SUBJEKTA") + "-" + mjesecObracuna(month-1) + "-" + year, param, calc(lJasperDataPrevious));
							
							System.out.println("SEKVENCA: " + seqIdRacun);
							System.out.println("------------------------------------------------------------");
							
							rs2.close();
							
							p1.add(seqIdRacun);
							p1.add((Integer) rhb.getIdSubjekt());

							p1.add(suma);
							p1.add(path);
							p1.add(getBrojRacuna());
							p1.add(getBrojRacuna());
							p1.add(new Integer(year));
							p1.add(new Integer(month));
							
							//insert racun
							trx2.executePreparedUpdateById("test", "InsertRacun", p1);
							trx2.commit();
							
							//update radni nalog
							for (Integer i:lradniNalog){
								p1.clear();
								p1.add(seqIdRacun);
								p1.add(i);
								trx2.executePreparedUpdateById("test", "UpdateRadniNalog4Racun", p1);
								trx2.commit();
							}
						}
						catch (Exception e){
							e.printStackTrace();
						}
						
						param.clear();
						lJasperData.clear();
						lradniNalog.clear();
						suma = BigDecimal.ZERO;
					}
					
					rhb = new RacunHelperBean();
					
					rhb.setIdDokument(rs.getInt("id_document"));
					rhb.setIdSubjekt(rs.getInt("id_subject"));
					rhb.setIdCertifikat(rs.getInt("id_certificate"));
					rhb.setJedCijena(rs.getBigDecimal("price"));
					rhb.setKodCertifikat(rs.getString("cert_code"));
					rhb.setNazivCertifikat(rs.getString("cert_name"));

					idRadniNalog = rs.getInt("id_document");
					lradniNalog.add(idRadniNalog);
					suma = suma.add(rs.getBigDecimal("price"));
					
					param.put("D1", min);
					param.put("D2", max);

					param.put("NAZIV_SUBJEKTA", rs.getString("sub_name"));
					param.put("OIB", rs.getString("vat_no"));
					param.put("SIFRA_KUPCA", rs.getString("sif_kupca"));
					param.put("ULICA", rs.getString("street") + " " + rs.getString("hn"));
					param.put("PBR_MJESTO", rs.getString("code") + " " + rs.getString("name"));
					param.put("MJESEC_OBRACUNA", mjesecObracuna(month-1));
					param.put("GODINA_OBRACUNA", String.valueOf(year));
					param.put("TEST", Const.TEST);
					
					
					System.out.println(rhb.getIdDokument() +  " " + rhb.getKodCertifikat());
				}
				
				//last report
				System.out.println("ADDING: " + rhb.getKodCertifikat());
				lJasperData.add(rhb);
				//System.out.println("Lista za report ima veličinu :" + lJasperData.size());
				//ArrayList<RacunGroupHelperBean> converted = calc(lJasperData);
				//System.out.println("Pretvorena lista ima veličinu: " + converted.size());
				
				List p1 = new ArrayList();
				ResultSet rs2 = null;
				
				try {
					Integer seqIdRacun = null;
					rs2 =  trx2.executePreparedQueryById("test", "GetSequenceRacun", p1);
					
					System.out.println("------------------------------------------------------------");
					
					if (rs2.next()){
						seqIdRacun = rs2.getInt(1);
					}
					
					setBrojRacuna(getBrojRacuna() + 1);
					
					//param.put("ID_RACUNA", seqIdRacun);//ovo je seqnum kada se zapisuje u tablicu racun
					
					System.out.println("ID_RACUN: " + seqIdRacun + "/" + year);
					param.put("ID_RACUNA", getBrojRacuna().toString());//ovo je seqnum kada se zapisuje u tablicu racun
					
					
					System.out.println("generiranje reporta: " + param.get("NAZIV_SUBJEKTA"));
					generateCertReport(param.get("NAZIV_SUBJEKTA") + "-" + mjesecObracuna(month-1) + "-" + year, param, calc(lJasperData));
					
					System.out.println("SEKVENCA: " + seqIdRacun);
					System.out.println("------------------------------------------------------------");
					
					rs2.close();
					
					p1.add(seqIdRacun);
					p1.add((Integer) rhb.getIdSubjekt());
					
					p1.add(suma);
					p1.add(path);
					p1.add(getBrojRacuna());
					p1.add(getBrojRacuna());
					p1.add(new Integer(year));
					p1.add(new Integer(month));
					
					trx2.executePreparedUpdateById("test", "InsertRacun", p1);
					trx2.commit();
					
					for (Integer i:lradniNalog){
						p1.clear();
						p1.add(seqIdRacun);
						p1.add(i);
						trx2.executePreparedUpdateById("test", "UpdateRadniNalog4Racun", p1);
						trx2.commit();
					}
					
					//update radni nalog
				}
				catch (Exception e){
					e.printStackTrace();
				}
				
				System.out.println("ZAVRŠEN OBRAČUN...");
				trx2.close();
				//refresh racuna
				/* Update brojača */
				if (testIfObracunEmpty)
				if (updateBrojRacunaGod(trx, getBrojRacuna())) System.out.println("AŽURIRAN BROJAČ...");
        		else System.out.println("NIJE AŽURIRAN BROJAČ...");
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			if (trx != null) trx.close();
			if (rs != null) rs.close();
		}
	}
	
	private String repairPath(String input){
		String output = null;
		
		try{
			output = input
            .replaceAll("Š", "S")
            .replaceAll("Đ", "D")
            .replaceAll("Ž", "Z")
            .replaceAll("Č", "C")
            .replaceAll("Ć", "C")
            .replaceAll("š", "s")
            .replaceAll("đ", "d")
            .replaceAll("ž", "z")
            .replaceAll("č", "č")
            .replaceAll("ć", "c")
    		.replaceAll(" ", "_");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return output;
	}
	
	private ArrayList<RacunGroupHelperBean> calc(ArrayList<RacunHelperBean> lista){
		ArrayList<RacunGroupHelperBean> lracun = new ArrayList<RacunGroupHelperBean>();
		
		try{
			System.out.println("CONVERTING");
			
			Integer idCertifikat = null;
			Integer brojac = 0;
			BigDecimal jedCijena = BigDecimal.ZERO;
			BigDecimal ukupno = BigDecimal.ZERO;
			String kod = null;
			String opis = null;
			int max = lista.size();
			RacunHelperBean item = null;
			
			for (int i = 0; i<max; i++){
				
				item = lista.get(i);
				
				if (idCertifikat == null) {//prvi zapis
					System.out.println("PRVI ZAPIS");
					idCertifikat = item.getIdCertifikat();
					brojac++;
					jedCijena = item.getJedCijena();
					kod = item.getKodCertifikat();
					opis = item.getNazivCertifikat();
					
					System.out.println("CERT: " + idCertifikat + ", brojac: " + brojac);
				}
				else if (idCertifikat.intValue() == item.getIdCertifikat().intValue()){
					brojac++;
					System.out.println("UVEćAVAM BROJAC: " + brojac);
				}
				else {//izradi report
					
					ukupno = jedCijena.multiply(new BigDecimal(brojac.intValue()));
					
					RacunGroupHelperBean rb = new RacunGroupHelperBean();
					rb.setNaziv(opis);
					rb.setKod(kod);
					rb.setJedCijena(jedCijena);
					rb.setKolicina(new BigDecimal(brojac.intValue()));
					rb.setUkupno(ukupno);
					
					//inicijalizacija
					idCertifikat = item.getIdCertifikat();
					brojac = 1;
					jedCijena = item.getJedCijena();
					ukupno = BigDecimal.ZERO;
					
					kod = item.getKodCertifikat();
					opis = item.getNazivCertifikat();
					
					System.out.println("Dodajem u listu...");
					//dodaj u listu
					lracun.add(rb);
				}
				
				if (i == (max - 1)){//zadnji zapis - report
					ukupno = jedCijena.multiply(new BigDecimal(brojac.intValue()));
					
					RacunGroupHelperBean rb = new RacunGroupHelperBean();
					rb.setNaziv(opis);
					rb.setKod(kod);
					rb.setJedCijena(jedCijena);
					rb.setKolicina(new BigDecimal(brojac.intValue()));
					rb.setUkupno(ukupno);
					
					System.out.println("Dodajem u listu...");
					//dodaj u listu
					lracun.add(rb);
				}

			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return lracun;
	}
	
	public byte[] generateCertReport(String reportName, Map parameters, ArrayList<RacunGroupHelperBean> lJasperData) {
    	byte[] report = null;
		
    	try {
    		System.out.println("GENERATING REPORT");
    		ServletContext  context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
            
    		for (RacunGroupHelperBean rh :lJasperData){
				System.out.println(rh.getKod() + " " + rh.getNaziv() + " " + rh.getKolicina() + " " + rh.getJedCijena() + " " + rh.getUkupno());
			}
    		
    		String path1=context.getRealPath("/reports/CERT_RACUN.jasper");

    		System.out.println(path1);
    		
	    	JRBeanCollectionDataSource beanColDataSource1 = new JRBeanCollectionDataSource(lJasperData);
	    	FileInputStream file1 = new FileInputStream(path1);
	    	JasperPrint jp1 = JasperFillManager.fillReport(file1, parameters, beanColDataSource1);
	    	
	    	//root path
	    	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	    	String realPath = ctx.getRealPath("/");
	    	
	    	path = repairPath(reportName) + ".pdf";
	    	
            JasperExportManager.exportReportToPdfFile(jp1, realPath + "\\racuni\\" + path);
            report = JasperExportManager.exportReportToPdf(jp1);
            
            System.out.println("ROOT: " + realPath + "\\racuni\\" );
            System.out.println("PATH: " + path);
            
		} catch (Exception ex) {
			ex.getMessage();
			ex.printStackTrace();
		}
		
		return report;
		
	} 
	
	public String mjesecObracuna(int mjesec){
		String mjesec2Text = "";
		if (mjesec == 0) mjesec2Text = "SIJEČANJ";
		if (mjesec == 1) mjesec2Text = "VELJAČA";
		if (mjesec == 2) mjesec2Text = "OŽUJAK";
		if (mjesec == 3) mjesec2Text = "TRAVANJ";
		if (mjesec == 4) mjesec2Text = "SVIBANJ";
		if (mjesec == 5) mjesec2Text = "LIPANJ";
		if (mjesec == 6) mjesec2Text = "SRPANJ";
		if (mjesec == 7) mjesec2Text = "KOLOVOZ";
		if (mjesec == 8) mjesec2Text = "RUJAN";
		if (mjesec == 9) mjesec2Text = "LISTOPAD";
		if (mjesec == 10) mjesec2Text = "STUDENI";
		if (mjesec == 11) mjesec2Text = "PROSINAC";
		
		return mjesec2Text;
	}

	public Date getDatumOd() {
		return datumOd;
	}

	public void setDatumOd(Date datumOd) {
		this.datumOd = datumOd;
	}

	public Date getDatumDo() {
		return datumDo;
	}

	public void setDatumDo(Date datumDo) {
		this.datumDo = datumDo;
	}

	public List<String> getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(List<String> selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	public Map getStatusi() {
		return statusi;
	}

	public void setStatusi(Map statusi) {
		this.statusi = statusi;
	}

	public Integer getSelected() {
		return selected;
	}

	public void setSelected(Integer selected) {
		this.selected = selected;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSubjekt() {
		return subjekt;
	}

	public void setSubjekt(String subjekt) {
		this.subjekt = subjekt;
	}

	public LazyDataModel<Racun> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Racun> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public Integer getBrojRacuna() {
		return brojRacuna;
	}

	public void setBrojRacuna(Integer brojRacuna) {
		this.brojRacuna = brojRacuna;
	}

	public Integer getPodrucje() {
		return podrucje;
	}

	public void setPodrucje(Integer podrucje) {
		this.podrucje = podrucje;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
		
	
}

