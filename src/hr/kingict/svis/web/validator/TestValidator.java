package hr.kingict.svis.web.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("TestValidator")
public class TestValidator implements Validator {


	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		String s=(String) value;
		
		if (!s.equals("ABCD")) {
			FacesMessage msg = new FacesMessage(null,"Vrijednost mora biti ABCD ");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);			
			throw new ValidatorException(msg);
		}
		
		
	}
	
}
