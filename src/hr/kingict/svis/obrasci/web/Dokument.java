package hr.kingict.svis.obrasci.web;

public class Dokument {
	private Integer value;
	private String label;
	
	public Dokument() {
		super();
	}
	
	public Dokument(Integer value, String label) {
		super();
		this.value = value;
		this.label = label;
	}

	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
