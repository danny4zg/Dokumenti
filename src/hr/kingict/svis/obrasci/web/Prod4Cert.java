package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class Prod4Cert {
	private Integer id;
	private CertifikatProizvodi prod;
	private Integer mjera;
	private String nazivMjera;
	private BigDecimal kolicina;
	
	public Prod4Cert() {
		super();
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public CertifikatProizvodi getProd() {
		return prod;
	}
	public void setProd(CertifikatProizvodi prod) {
		this.prod = prod;
	}
	public Integer getMjera() {
		return mjera;
	}
	public void setMjera(Integer mjera) {
		this.mjera = mjera;
	}
	public BigDecimal getKolicina() {
		return kolicina;
	}
	public void setKolicina(BigDecimal kolicina) {
		this.kolicina = kolicina;
	}
	public String getNazivMjera() {
		return nazivMjera;
	}
	public void setNazivMjera(String nazivMjera) {
		this.nazivMjera = nazivMjera;
	}

}
