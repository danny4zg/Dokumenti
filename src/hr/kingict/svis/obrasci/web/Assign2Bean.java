package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;
import java.util.Date;

public class Assign2Bean {
	private Integer idStavkaNarudzbe;
	private Date datumNarudzbe;
	private Integer idNarudzbe;
	private DocumentBean doc;
	private Integer kolicina;
	private BigDecimal cijena;
	private Integer idStatus;
	private String status;
	private Integer idNarucitelj;
	private String narucitelj;
	private Integer idDobavljac;
	private String dobavljac;
	private String serijskiBrojOd;
	private String serijskiBrojDo;
	private String prefix;
	private String napomena;
	private Integer orgType;
	
	public Integer getIdStavkaNarudzbe() {
		return idStavkaNarudzbe;
	}
	public void setIdStavkaNarudzbe(Integer idStavkaNarudzbe) {
		this.idStavkaNarudzbe = idStavkaNarudzbe;
	}
	public Date getDatumNarudzbe() {
		return datumNarudzbe;
	}
	public void setDatumNarudzbe(Date datumNarudzbe) {
		this.datumNarudzbe = datumNarudzbe;
	}
	public Integer getIdNarudzbe() {
		return idNarudzbe;
	}
	public void setIdNarudzbe(Integer idNarudzbe) {
		this.idNarudzbe = idNarudzbe;
	}
	public DocumentBean getDoc() {
		return doc;
	}
	public void setDoc(DocumentBean doc) {
		this.doc = doc;
	}
	public Integer getKolicina() {
		return kolicina;
	}
	public void setKolicina(Integer kolicina) {
		this.kolicina = kolicina;
	}
	public BigDecimal getCijena() {
		return cijena;
	}
	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
	}
	public Integer getIdStatus() {
		return idStatus;
	}
	public void setIdStatus(Integer idStatus) {
		this.idStatus = idStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getIdNarucitelj() {
		return idNarucitelj;
	}
	public void setIdNarucitelj(Integer idNarucitelj) {
		this.idNarucitelj = idNarucitelj;
	}
	public String getNarucitelj() {
		return narucitelj;
	}
	public void setNarucitelj(String narucitelj) {
		this.narucitelj = narucitelj;
	}
	public Integer getIdDobavljac() {
		return idDobavljac;
	}
	public void setIdDobavljac(Integer idDobavljac) {
		this.idDobavljac = idDobavljac;
	}
	public String getDobavljac() {
		return dobavljac;
	}
	public void setDobavljac(String dobavljac) {
		this.dobavljac = dobavljac;
	}
	public String getSerijskiBrojOd() {
		return serijskiBrojOd;
	}
	public void setSerijskiBrojOd(String serijskiBrojOd) {
		this.serijskiBrojOd = serijskiBrojOd;
	}
	public String getSerijskiBrojDo() {
		return serijskiBrojDo;
	}
	public void setSerijskiBrojDo(String serijskiBrojDo) {
		this.serijskiBrojDo = serijskiBrojDo;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getNapomena() {
		return napomena;
	}
	public void setNapomena(String napomena) {
		this.napomena = napomena;
	}
	public Integer getOrgType() {
		return orgType;
	}
	public void setOrgType(Integer orgType) {
		this.orgType = orgType;
	}
	
}
