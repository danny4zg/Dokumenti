package hr.kingict.svis.obrasci.web;

import java.util.HashMap;

public class UserBean {
	Integer idKorisnik;
	Integer idOrganizacije;
	String ime;
	String prezime;
	String username;
	String oib;
	String tel;
	String email;
	String fax;
	HashMap<String, Integer> rola = new HashMap<String, Integer>();
	
	public Integer getIdKorisnik() {
		return idKorisnik;
	}

	public void setIdKorisnik(Integer idKorisnik) {
		this.idKorisnik = idKorisnik;
	}

	public Integer getIdOrganizacije() {
		return idOrganizacije;
	}

	public void setIdOrganizacije(Integer idOrganizacije) {
		this.idOrganizacije = idOrganizacije;
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getPrezime() {
		return prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOib() {
		return oib;
	}

	public void setOib(String oib) {
		this.oib = oib;
	}

	public HashMap getRola() {
		return rola;
	}

	public void setRola(HashMap rola) {
		this.rola = rola;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

}
