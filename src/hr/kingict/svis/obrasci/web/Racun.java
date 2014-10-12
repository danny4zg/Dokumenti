package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;
import java.util.Date;

public class Racun {
	private Integer idRacun;
	private SubjektBean subjekt;
	private Date datumRacuna;
	private Date datumPlacanja;
	private BigDecimal iznos;
	private BigDecimal uplaceniIznos;
	private String ira;
	private String path;
	private Integer idStatusPlacanja;
	private String nazivStatusPlacanja;
	
	public Racun() {

	}

	public Integer getIdRacun() {
		return idRacun;
	}

	public void setIdRacun(Integer idRacun) {
		this.idRacun = idRacun;
	}

	public SubjektBean getSubjekt() {
		return subjekt;
	}

	public void setSubjekt(SubjektBean subjekt) {
		this.subjekt = subjekt;
	}

	public Date getDatumRacuna() {
		return datumRacuna;
	}

	public void setDatumRacuna(Date datumRacuna) {
		this.datumRacuna = datumRacuna;
	}

	public Date getDatumPlacanja() {
		return datumPlacanja;
	}

	public void setDatumPlacanja(Date datumPlacanja) {
		this.datumPlacanja = datumPlacanja;
	}

	public BigDecimal getIznos() {
		return iznos;
	}

	public void setIznos(BigDecimal iznos) {
		this.iznos = iznos;
	}

	public BigDecimal getUplaceniIznos() {
		return uplaceniIznos;
	}

	public void setUplaceniIznos(BigDecimal uplaceniIznos) {
		this.uplaceniIznos = uplaceniIznos;
	}

	public String getIra() {
		return ira;
	}

	public void setIra(String ira) {
		this.ira = ira;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getIdStatusPlacanja() {
		return idStatusPlacanja;
	}

	public void setIdStatusPlacanja(Integer idStatusPlacanja) {
		this.idStatusPlacanja = idStatusPlacanja;
	}

	public String getNazivStatusPlacanja() {
		return nazivStatusPlacanja;
	}

	public void setNazivStatusPlacanja(String nazivStatusPlacanja) {
		this.nazivStatusPlacanja = nazivStatusPlacanja;
	}
	
}
