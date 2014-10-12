package hr.kingict.svis.web.converter;

import hr.kingict.svis.obrasci.facade.IssueCertificateFacade;
import hr.kingict.svis.obrasci.web.SubjektBean;

import java.util.List;

import javax.el.ELContext;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "subjektConverter")
public class SubjektConverter implements Converter {
	private ELContext elContext;
	private IssueCertificateFacade data;
	public List<SubjektBean> suggestDB;

    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        
    	elContext = FacesContext.getCurrentInstance().getELContext();
		data = (IssueCertificateFacade) FacesContext.getCurrentInstance().getApplication()
		          .getELResolver().getValue(elContext, null, "issueCert");
		suggestDB = data.getSuggestResults();
    	
    	if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {
                int number = Integer.parseInt(submittedValue);

                for (SubjektBean p : suggestDB) {
                    if (p.getIdSubjekt() == number) {
                    	//System.out.println("VADIM VAN:" + p.getNaziv());
                        return p;
                    }
                }

            } catch(NumberFormatException exception) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Objekt nije ispravan"));
            }
        }

        return null;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            return String.valueOf(((SubjektBean) value).getIdSubjekt());
        }
    }
}
                    
