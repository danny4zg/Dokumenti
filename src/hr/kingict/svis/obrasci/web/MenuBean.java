package hr.kingict.svis.obrasci.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

public class MenuBean {
	
	public void save(ActionEvent actionEvent) {
		addMessage("Data saved");
	}
	
	public void update(ActionEvent actionEvent) {
		addMessage("Data updated");
	}
	
	public void delete(ActionEvent actionEvent) {
		addMessage("Data deleted");
	}
	
	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}