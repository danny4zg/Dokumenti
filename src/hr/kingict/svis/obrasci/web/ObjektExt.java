package hr.kingict.svis.obrasci.web;

public class ObjektExt extends Objekt{
	private String tel;
	private String email;
	private String fax;
	
	public ObjektExt() {
		super();
		// TODO Auto-generated constructor stub
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
