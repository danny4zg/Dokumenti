package hr.kingict.svis.reports;

import hr.kingict.svis.obrasci.util.StringUtil;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


public class DSGeneric<E> implements DSGenericInterface{
	protected List<E> _lista = null;
	private int index=-1;
	

	
	public DSGeneric(List<E> lista) {
		_lista=lista;
	}

	public boolean next() throws JRException {
    	index++;
    	if(index<_lista.size()) 
    		return true;
    	else  
    		return false;
    }

	public Object getFieldValue(JRField arg0) throws JRException {
		E obj = (E) _lista.get(index);

		if (obj.getClass().getName().equals(Object[].class.getName())) {
                    Integer index = Integer.valueOf(arg0.getName());
                    index = index - 1;
                    return ((Object[])obj)[index]; 
		} else {
                    Method method = fetchGetterMethod(arg0.getName(), obj);
                    if (method != null) {
                        try {
                            return method.invoke(obj);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    }
		}

    	if (arg0.getName().equals("recordCount")) {
    		return index + 1;
    	}
		
    	return null;
	}

	private Method fetchGetterMethod(String fieldName, E obj) {
		try {
            return obj.getClass().getMethod("get" + StringUtil.capitalize(fieldName));
		}
		catch (NoSuchMethodException ex) {
            return null;
		}
	}
	
	public List<E> getList() {
		return _lista;
	}
	

}