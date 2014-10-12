package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class CertifikatPrint {
	private Integer idCertifikatPrint;
	private Integer idCertifikat;
	private String nazivCertifikat;
	private String kodCertifikat;
	private Integer idDrzava;
	private String nazivDrzava;
	private Integer idMjera;
	private String nazivMjera;
	
	private Date datumPrintanja;
	private Date datumStorniranja;
	
	private String serijskiBroj;
	private String oznakaSerije;
	private BigDecimal cijena;
	private String jeziciTekst;
	
	private Integer status;
	private String nazivStatus;
	
	private Integer tip;
	private String nazivTip;
	
	private List<CertifikatJezici> jezici;
	
	private Integer orgPrint;
	private String nazivOrgPrint;
	
	public CertifikatPrint() {

	}

	public Integer getIdCertifikat() {
		return idCertifikat;
	}

	public void setIdCertifikat(Integer idCertifikat) {
		this.idCertifikat = idCertifikat;
	}

	public String getNazivCertifikat() {
		return nazivCertifikat;
	}

	public void setNazivCertifikat(String nazivCertifikat) {
		this.nazivCertifikat = nazivCertifikat;
	}

	public String getKodCertifikat() {
		return kodCertifikat;
	}

	public void setKodCertifikat(String kodCertifikat) {
		this.kodCertifikat = kodCertifikat;
	}

	public Integer getIdDrzava() {
		return idDrzava;
	}

	public void setIdDrzava(Integer idDrzava) {
		this.idDrzava = idDrzava;
	}

	public String getNazivDrzava() {
		return nazivDrzava;
	}

	public void setNazivDrzava(String nazivDrzava) {
		this.nazivDrzava = nazivDrzava;
	}

	public Integer getIdMjera() {
		return idMjera;
	}

	public void setIdMjera(Integer idMjera) {
		this.idMjera = idMjera;
	}

	public String getNazivMjera() {
		return nazivMjera;
	}

	public void setNazivMjera(String nazivMjera) {
		this.nazivMjera = nazivMjera;
	}

	public Date getDatumPrintanja() {
		return datumPrintanja;
	}

	public void setDatumPrintanja(Date datumPrintanja) {
		this.datumPrintanja = datumPrintanja;
	}

	public String getSerijskiBroj() {
		return serijskiBroj;
	}

	public void setSerijskiBroj(String serijskiBroj) {
		this.serijskiBroj = serijskiBroj;
	}

	public String getOznakaSerije() {
		return oznakaSerije;
	}

	public void setOznakaSerije(String oznakaSerije) {
		this.oznakaSerije = oznakaSerije;
	}

	public BigDecimal getCijena() {
		return cijena;
	}

	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
	}

	public String getJeziciTekst() {
		return jeziciTekst;
	}

	public void setJeziciTekst(String jeziciTekst) {
		this.jeziciTekst = jeziciTekst;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getNazivStatus() {
		return nazivStatus;
	}

	public void setNazivStatus(String nazivStatus) {
		this.nazivStatus = nazivStatus;
	}

	public Integer getTip() {
		return tip;
	}

	public void setTip(Integer tip) {
		this.tip = tip;
	}

	public String getNazivTip() {
		return nazivTip;
	}

	public void setNazivTip(String nazivTip) {
		this.nazivTip = nazivTip;
	}

	public List<CertifikatJezici> getJezici() {
		return jezici;
	}

	public void setJezici(List<CertifikatJezici> jezici) {
		this.jezici = jezici;
	}

	public Integer getOrgPrint() {
		return orgPrint;
	}

	public void setOrgPrint(Integer orgPrint) {
		this.orgPrint = orgPrint;
	}

	public String getNazivOrgPrint() {
		return nazivOrgPrint;
	}

	public void setNazivOrgPrint(String nazivOrgPrint) {
		this.nazivOrgPrint = nazivOrgPrint;
	}

	public Integer getIdCertifikatPrint() {
		return idCertifikatPrint;
	}

	public void setIdCertifikatPrint(Integer idCertifikatPrint) {
		this.idCertifikatPrint = idCertifikatPrint;
	}

	public Date getDatumStorniranja() {
		return datumStorniranja;
	}

	public void setDatumStorniranja(Date datumStorniranja) {
		this.datumStorniranja = datumStorniranja;
	}
	
	
	
}
