package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class OrderBean {
	private DocumentBean doc;
	private Org4Doc dobavljac;
	private Integer kolicina;
	private BigDecimal cijena;
	private String serijskiBrojOd;
	private String serijskiBrojDo;
	private Integer id;
	
	public DocumentBean getDoc() {
		return doc;
	}
	public void setDoc(DocumentBean doc) {
		this.doc = doc;
	}
	public Org4Doc getDobavljac() {
		return dobavljac;
	}
	public void setDobavljac(Org4Doc dobavljac) {
		this.dobavljac = dobavljac;
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
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	
}
