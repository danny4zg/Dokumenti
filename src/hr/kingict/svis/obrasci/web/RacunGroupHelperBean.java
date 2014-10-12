package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class RacunGroupHelperBean {
	private String kod;
	private String naziv;
	private BigDecimal jedCijena;
	private BigDecimal kolicina;
	private BigDecimal ukupno;
		
	public RacunGroupHelperBean() {
	}

	public BigDecimal getJedCijena() {
		return jedCijena;
	}

	public void setJedCijena(BigDecimal jedCijena) {
		this.jedCijena = jedCijena;
	}

	public String getKod() {
		return kod;
	}

	public void setKod(String kod) {
		this.kod = kod;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public BigDecimal getKolicina() {
		return kolicina;
	}

	public void setKolicina(BigDecimal kolicina) {
		this.kolicina = kolicina;
	}

	public BigDecimal getUkupno() {
		return ukupno;
	}

	public void setUkupno(BigDecimal ukupno) {
		this.ukupno = ukupno;
	}
	
}
