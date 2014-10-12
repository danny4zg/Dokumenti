package hr.kingict.svis.obrasci.web;

import java.math.BigDecimal;

public class DocumentBean {
	private Integer idDocument;
	private String code;
	private String desc;
	private Integer idState;
	private String stateCode;
	private String state;
	private Integer idLanguage;
	private String language;
	private String javaLangCode;
	private Integer idUnit;
	private String unit;
	private BigDecimal price;
	private Integer idOrgDocument;
	private Integer multiply;
	private String prefix;
	private String serialNo;
	private Integer currentSerialNo;
	private Integer pakiranjeKom;
	private String status;
	private Integer maxNumber;
	
	public Integer getIdDocument() {
		return idDocument;
	}
	public void setIdDocument(Integer idDocument) {
		this.idDocument = idDocument;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public Integer getIdState() {
		return idState;
	}
	public void setIdState(Integer idState) {
		this.idState = idState;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public Integer getIdLanguage() {
		return idLanguage;
	}
	public void setIdLanguage(Integer idLanguage) {
		this.idLanguage = idLanguage;
	}
	public String getJavaLangCode() {
		return javaLangCode;
	}
	public void setJavaLangCode(String javaLangCode) {
		this.javaLangCode = javaLangCode;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Integer getIdUnit() {
		return idUnit;
	}
	public void setIdUnit(Integer idUnit) {
		this.idUnit = idUnit;
	}
	public Integer getIdOrgDocument() {
		return idOrgDocument;
	}
	public void setIdOrgDocument(Integer idOrgDocument) {
		this.idOrgDocument = idOrgDocument;
	}
	public Integer getMultiply() {
		return multiply;
	}
	public void setMultiply(Integer multiply) {
		this.multiply = multiply;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public Integer getCurrentSerialNo() {
		return currentSerialNo;
	}
	public void setCurrentSerialNo(Integer currentSerialNo) {
		this.currentSerialNo = currentSerialNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getPakiranjeKom() {
		return pakiranjeKom;
	}
	public void setPakiranjeKom(Integer pakiranjeKom) {
		this.pakiranjeKom = pakiranjeKom;
	}
	public Integer getMaxNumber() {
		return maxNumber;
	}
	public void setMaxNumber(Integer maxNumber) {
		this.maxNumber = maxNumber;
	}

	
}
