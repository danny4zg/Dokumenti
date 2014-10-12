package hr.kingict.svis.obrasci.web;

import java.util.Date;

public class Assign4Bean {
	private Assign2Bean stavka;
	private String serijaOd;
	private String serijaDo;
	private Date datum;
	private String notes;
	
	public Assign2Bean getStavka() {
		return stavka;
	}
	public void setStavka(Assign2Bean stavka) {
		this.stavka = stavka;
	}
	public String getSerijaOd() {
		return serijaOd;
	}
	public void setSerijaOd(String serijaOd) {
		this.serijaOd = serijaOd;
	}
	public String getSerijaDo() {
		return serijaDo;
	}
	public void setSerijaDo(String serijaDo) {
		this.serijaDo = serijaDo;
	}
	public Date getDatum() {
		return datum;
	}
	public void setDatum(Date datum) {
		this.datum = datum;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	
	
}
