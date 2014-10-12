package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Certifikat {
	private Integer idCertifikat;
	private String nazivCertifikat;
	private String kodCertifikat;
	private Integer idDrzava;
	private String nazivDrzava;
	private Integer idMjera;
	private String nazivMjera;
	
	private Integer tip;
	private String nazivTip;
	private Date datumPromjene;
	private Date datumVrijediOd;
	private Date datumVrijediDo;
	
	private String serijskiBroj;
	private String oznakaSerije;
	private BigDecimal cijena;
	private String jeziciTekst;
	
	private Integer status;
	private String nazivStatus;
	
	private List<CertifikatJezici> jezici;
	private List<Drzava> drzave;
	
	private byte[] template;
	private String templateNaziv;
	
	private String komentar;
	
	private String activity;
	
	public Certifikat() {
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

	public Integer getTip() {
		return tip;
	}

	public void setTip(Integer tip) {
		this.tip = tip;
	}

	public Date getDatumPromjene() {
		return datumPromjene;
	}

	public void setDatumPromjene(Date datumPromjene) {
		this.datumPromjene = datumPromjene;
	}

	public Date getDatumVrijediOd() {
		return datumVrijediOd;
	}

	public void setDatumVrijediOd(Date datumVrijediOd) {
		this.datumVrijediOd = datumVrijediOd;
	}

	public Date getDatumVrijediDo() {
		return datumVrijediDo;
	}

	public void setDatumVrijediDo(Date datumVrijediDo) {
		this.datumVrijediDo = datumVrijediDo;
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

	public List<CertifikatJezici> getJezici() {
		return jezici;
	}

	public void setJezici(List<CertifikatJezici> jezici) {
		this.jezici = jezici;
	}

	public byte[] getTemplate() {
		return template;
	}

	public void setTemplate(byte[] template) {
		this.template = template;
	}

	public String getTemplateNaziv() {
		return templateNaziv;
	}

	public void setTemplateNaziv(String templateNaziv) {
		this.templateNaziv = templateNaziv;
	}

	public String getKomentar() {
		return komentar;
	}

	public void setKomentar(String komentar) {
		this.komentar = komentar;
	}

	public String getNazivTip() {
		return nazivTip;
	}

	public void setNazivTip(String nazivTip) {
		this.nazivTip = nazivTip;
	}

	public String getJeziciTekst() {
		return jeziciTekst;
	}

	public void setJeziciTekst(String jeziciTekst) {
		this.jeziciTekst = jeziciTekst;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public List<Drzava> getDrzave() {
		return drzave;
	}

	public void setDrzave(List<Drzava> drzave) {
		this.drzave = drzave;
	}

}
