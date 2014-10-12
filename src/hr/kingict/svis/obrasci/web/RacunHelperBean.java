package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class RacunHelperBean {
	private Integer idSubjekt;
	private Integer idCertifikat;
	private String kodCertifikat;
	private String nazivCertifikat;
	private BigDecimal jedCijena;
	private Integer idDokument;
		
	public RacunHelperBean() {
	}

	public Integer getIdSubjekt() {
		return idSubjekt;
	}

	public void setIdSubjekt(Integer idSubjekt) {
		this.idSubjekt = idSubjekt;
	}

	public Integer getIdCertifikat() {
		return idCertifikat;
	}

	public void setIdCertifikat(Integer idCertifikat) {
		this.idCertifikat = idCertifikat;
	}

	public String getKodCertifikat() {
		return kodCertifikat;
	}

	public void setKodCertifikat(String kodCertifikat) {
		this.kodCertifikat = kodCertifikat;
	}

	public String getNazivCertifikat() {
		return nazivCertifikat;
	}

	public void setNazivCertifikat(String nazivCertifikat) {
		this.nazivCertifikat = nazivCertifikat;
	}

	public BigDecimal getJedCijena() {
		return jedCijena;
	}

	public void setJedCijena(BigDecimal jedCijena) {
		this.jedCijena = jedCijena;
	}

	public Integer getIdDokument() {
		return idDokument;
	}

	public void setIdDokument(Integer idDokument) {
		this.idDokument = idDokument;
	}

	
	
}
