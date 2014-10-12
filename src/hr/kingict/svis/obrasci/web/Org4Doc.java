package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class Org4Doc {
	private Integer idOrganizacija;
	private String naziv;
	private BigDecimal cijena;
	private String adresa;
	private String mjesto;
	private String oib;
	
	public String getAdresa() {
		return adresa;
	}
	public void setAdresa(String adresa) {
		this.adresa = adresa;
	}
	public String getMjesto() {
		return mjesto;
	}
	public void setMjesto(String mjesto) {
		this.mjesto = mjesto;
	}
	public String getOib() {
		return oib;
	}
	public void setOib(String oib) {
		this.oib = oib;
	}
	public Integer getIdOrganizacija() {
		return idOrganizacija;
	}
	public void setIdOrganizacija(Integer idOrganizacija) {
		this.idOrganizacija = idOrganizacija;
	}
	public String getNaziv() {
		return naziv;
	}
	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}
	public BigDecimal getCijena() {
		return cijena;
	}
	public void setCijena(BigDecimal cijena) {
		this.cijena = cijena;
	}
	
}
