package hr.kingict.svis.obrasci.web;

import java.util.Date;
import java.util.List;

public class AssignBean {
	private Integer idNarudzbe;
	
	private List<OrderBean> oList;
	private Date datumNarudzbe;
	private Integer idStatusNarudzbe;
	private String status;
	private Integer idNarucitelj;
	
	public Integer getIdNarudzbe() {
		return idNarudzbe;
	}
	public void setIdNarudzbe(Integer idNarudzbe) {
		this.idNarudzbe = idNarudzbe;
	}
	public List<OrderBean> getOList() {
		return oList;
	}
	public void setOList(List<OrderBean> list) {
		oList = list;
	}
	public Date getDatumNarudzbe() {
		return datumNarudzbe;
	}
	public void setDatumNarudzbe(Date datumNarudzbe) {
		this.datumNarudzbe = datumNarudzbe;
	}
	public Integer getIdStatusNarudzbe() {
		return idStatusNarudzbe;
	}
	public void setIdStatusNarudzbe(Integer idStatusNarudzbe) {
		this.idStatusNarudzbe = idStatusNarudzbe;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getIdNarucitelj() {
		return idNarucitelj;
	}
	public void setIdNarucitelj(Integer idNarucitelj) {
		this.idNarucitelj = idNarucitelj;
	}
	
}
