package hr.kingict.svis.obrasci.web;

import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.svis.obrasci.param.Const;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ManagedBean(name="login")
@SessionScoped
public class LoginBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private String tel;
	private String email;
	private boolean isLoggedIn = false;
	private UserBean userData;
	private int rola;
	private int count;
	private boolean isAdmin;

	public String login(){
		//custom member manager class
		MemberManager memberManager=new MemberManager();
		//default url in case of login failure;
		String url="login.jsf";
		
		//user a custom method to authenticate a user
		try {
			boolean provjera = memberManager.authenticate(username, password);
			if (provjera == true){
				//changed the state to true
				
				FacesContext context = FacesContext.getCurrentInstance();
		    	UserBean ub = (UserBean) context.getExternalContext().getSessionMap().get("user");
				this.userData = ub;
				
				rola = ((Integer) context.getExternalContext().getSessionMap().get("rola")).intValue();
				isAdmin = (Boolean) context.getExternalContext().getSessionMap().get("certadmin");
		
				System.out.println("CERT ADMIN:" + isAdmin);
				
				tel = ub.getTel();
				email = ub.getEmail();
				
	    	    isLoggedIn = true;
		    	    
	    	    if (rola == Const.ROLA_ADMIN.intValue()){
	    	    	url="documents?faces-redirect=true";
	    	    } 
	    	    else if (rola == Const.ROLA_UPRAVA_1.intValue() || rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) { 
	    	    	url="order_search?faces-redirect=true";
	    	    }	
	    	    else if (rola == Const.ROLA_UPRAVA_2.intValue() || rola == Const.ROLA_ADMIN_CERTIFIKATI.intValue()){
	    	    	url="certificates?faces-redirect=true";
	    	    }
	    	    else if (rola == Const.ROLA_ORG_DOK.intValue()){
	    	    	url="order?faces-redirect=true";			    
	    	    }
	    	    else if (rola == Const.ROLA_ORG_CERT.intValue()){
		    	    url="certificates?faces-redirect=true";		    	  
	    	    } 
	    	    else if (rola == Const.ROLA_ORG_CERT_DOK.intValue()){
		    	    url="order_search?faces-redirect=true";		
		    	    rola = 12;
		    	    context.getExternalContext().getSessionMap().put("rola", rola);
	    	    } 
	    	    else if (rola == Const.ROLA_TISKARA.intValue()){
		    	    url="order_search?faces-redirect=true";		    	   
	    	    }	   	
	    	    else if (rola == Const.ROLA_INSPEKTOR.intValue() || rola == Const.ROLA_DVI.intValue()){
		    	    url="certificates_printed?faces-redirect=true";		    	   
	    	    }	
	    	    else if (rola == Const.ROLA_OBJEKT.intValue()){
	    	    	url="order?faces-redirect=true";
	    	    }
		    	System.out.println(url);
				
				
			} else if (provjera == false){
				//set the message to display when authentication fails
				//System.out.println("Neispravni korisnički podaci ili nemate pravo za pristup...");
				
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Neispravni korisnički podaci.",  "Neispravni korisnički podaci.");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
			} else {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Trenutno nije moguće pristupiti sustavu.",  "Trenutno nije moguće pristupiti sustavu.");  
		        FacesContext.getCurrentInstance().addMessage(null, message); 
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	return url;
	}

	public String redirectToStartPage(){
		
		String url = null;
		
		try {
			if (rola == Const.ROLA_ADMIN_CERTIFIKATI.intValue() || rola == Const.ROLA_UPRAVA_2.intValue()){
			    url="certificates?faces-redirect=true";
		    }
			else if (rola == Const.ROLA_ADMIN){
			    url="documents?faces-redirect=true";
		    }
			else if (rola == Const.ROLA_ADMIN_DOKUMENTI.intValue() || rola == Const.ROLA_UPRAVA_1.intValue()){
			    url="order_search?faces-redirect=true";
		    }
			else if (rola == Const.ROLA_UPRAVA.intValue()){
				url="certificates?faces-redirect=true";
			}
		    else if (rola == Const.ROLA_ORG_DOK.intValue()){
		    	url="order?faces-redirect=true";			    
		    }
		    else if (rola == Const.ROLA_ORG_CERT.intValue()){
			    url="certificates?faces-redirect=true";		    	  
		    } 
		    else if (rola == Const.ROLA_ORG_CERT_DOK.intValue()){
			    url="certificates?faces-redirect=true";		
		    } 
		    else if (rola == Const.ROLA_TISKARA.intValue()){
			    url="order_assignment?faces-redirect=true";		    	   
		    }	   	
		    else if (rola == Const.ROLA_INSPEKTOR.intValue() || rola == Const.ROLA_DVI.intValue()){
	    	    url="certificates_printed?faces-redirect=true";		    	   
    	    }	
		    else if (rola == Const.ROLA_OBJEKT.intValue()){
    	    	url="order?faces-redirect=true";
    	    }
		} catch (Exception e){
			e.printStackTrace();
		}

		System.out.println(url);
	
		return url;
	}
	
 /**
 * An event listener for redirecting the user to login page if he/she is not currently logged in
 * @param event
 */
	public void verifyUseLogin(ComponentSystemEvent event){
		 if(!isLoggedIn){
			 doRedirect("login.jsf");
		 }
	 }

	 /**
	 * Method for redirecting a request
	 * @param url
	 */
	private void doRedirect(String url){
		try {
			FacesContext context=FacesContext.getCurrentInstance();
			context.getExternalContext().redirect("login.jsf");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Integer getDjelatnikId(){	
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null)
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
  
		return userData.getIdKorisnik();
	}
	
	
	public String getLogiraniDjelatnik () {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null)
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
  
		if (userData == null) {
			odjava();
		}
		
		return userData.getUsername();
	}
	
	public String getOdjavaLink () {
		
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null){
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    	}
    	
    	if (userData == null) {
    		FacesContext fc = FacesContext.getCurrentInstance();
	    	ExternalContext ec = fc.getExternalContext();
	    	try {
	    		HttpServletRequest request=(HttpServletRequest)ec.getRequest();
	    	    ec.redirect(request.getContextPath()+"/login.xhtml");
	    	} catch (IOException ex) {
	    	    ex.printStackTrace();
	    	}
	    	
	    	return "";
    	}
    	
    	return "Odjava";
	}
	
	public String getPrijavljeniKorisnik(){
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null)
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
  
		return  userData.getIme() + " " + userData.getPrezime() + " | ";
	}
	

	public void keepAlive() {
		count++;
	}
	
	public void automatskaOdjava() {
		
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);

    	if (session == null) {
    		System.out.println("Automatska odjava, session je null!!!!");
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    	}
    	
    	System.out.println("Automatska odjava, djelatnik: " + userData.getUsername());
    	
    	if (userData == null) {
    		odjava();
    	}
	}
	
	public void odjava() {
		
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null){
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		}
    	session.removeAttribute("userName");
    	
    	try {
    		userData = null;
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FacesContext.getCurrentInstance(); 
    	FacesContext fc = FacesContext.getCurrentInstance();
    	ExternalContext ec = fc.getExternalContext();
    	
    	//to-do
    	
    	ec.getSessionMap().remove("organization");
    	ec.getSessionMap().remove("docListOrg");
    	ec.getSessionMap().remove("user");
    	ec.getSessionMap().remove("approveCert");
    	ec.getSessionMap().remove("rola");
    	ec.getSessionMap().remove("certSif");
    	ec.getSessionMap().remove("issueCert");
    	ec.getSessionMap().remove("docSif");
    	ec.getSessionMap().remove("assiord");
    	ec.getSessionMap().remove("deliord");
    	ec.getSessionMap().remove("unitSif");
    	ec.getSessionMap().remove("rac");
    	ec.getSessionMap().remove("idord");
    	ec.getSessionMap().remove("iddelord");
    	ec.getSessionMap().remove("searchord");
    	ec.getSessionMap().remove("idsearchord");
    	ec.getSessionMap().remove("docListOrg");
    	ec.getSessionMap().remove("return");
    	ec.getSessionMap().remove("certListOrg");
    	ec.getSessionMap().remove("contingent");
    	ec.getSessionMap().remove("certadmin");
    	//to-do
    	
    	try {
			HttpServletRequest request=(HttpServletRequest)ec.getRequest();
	        ec.redirect(request.getContextPath()+"/login.xhtml");
    	} catch (IOException ex) {
	        ex.printStackTrace();
    	}
		
    	isLoggedIn=false;
	}

	public boolean isLogiran() {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    	if (session == null)
    		session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
  
		return isLoggedIn;
	}

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public UserBean getUserData() {
		return userData;
	}

	public void setUserData(UserBean userData) {
		this.userData = userData;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Boolean getIsLoggedIn() {
		return isLoggedIn;
	}

	public void setIsLoggedIn(Boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public int getRola() {
		return rola;
	}

	public void setRola(int rola) {
		this.rola = rola;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
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
	
}
