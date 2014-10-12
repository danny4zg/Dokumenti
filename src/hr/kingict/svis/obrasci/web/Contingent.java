package hr.kingict.svis.obrasci.web;

import java.util.Date;

public class Contingent {
	private Integer rownum;
	private Integer idContingent;
	private String barcodeType;
	private Integer barcodeTypeCode;
	private Date createDate;
	private Integer amount;
	private Integer barcodeFrom;	
	private Integer barcodeTo;
	private Integer available;
	
	public Contingent() {
	}

	public Integer getRownum() {
		return rownum;
	}

	public void setRownum(Integer rownum) {
		this.rownum = rownum;
	}

	public Integer getIdContingent() {
		return idContingent;
	}

	public void setIdContingent(Integer idContingent) {
		this.idContingent = idContingent;
	}

	public String getBarcodeType() {
		return barcodeType;
	}

	public void setBarcodeType(String barcodeType) {
		this.barcodeType = barcodeType;
	}

	public Integer getBarcodeTypeCode() {
		return barcodeTypeCode;
	}

	public void setBarcodeTypeCode(Integer barcodeTypeCode) {
		this.barcodeTypeCode = barcodeTypeCode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getBarcodeFrom() {
		return barcodeFrom;
	}

	public void setBarcodeFrom(Integer barcodeFrom) {
		this.barcodeFrom = barcodeFrom;
	}

	public Integer getBarcodeTo() {
		return barcodeTo;
	}

	public void setBarcodeTo(Integer barcodeTo) {
		this.barcodeTo = barcodeTo;
	}

	public Integer getAvailable() {
		return available;
	}

	public void setAvailable(Integer available) {
		this.available = available;
	}
	
}
