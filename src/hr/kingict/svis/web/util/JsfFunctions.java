package hr.kingict.svis.web.util;

import java.text.MessageFormat;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * Functions to aid developing JSF applications.
 */
public final class JsfFunctions {
	
	/*public static final String  BUNDLE_NAME = "hr.abit.bassx2.biz.message.CustomMessages";	
	public static final String  CONTROLLER_BUNDLE_NAME = "hr.abit.bassx2.biz.message.ControllerMessages";
	public static final String  ENTITY_BUNDLE_NAME = "hr.abit.bassx2.biz.message.EntityMessages";	
	public static final String  BEAN_BUNDLE_NAME = "hr.abit.bassx2.biz.message.BeanMessages";	
	*/
	
	/**
	 * Stops creation of a new JsfFunctions object.
	 */
	private JsfFunctions() {
	}

	public static String getFieldLabel(final String fieldName) {
		return getFieldLabel(fieldName, "", new Object[0]);
	}
	public static String getFieldLabel(final String fieldName,
			final String formId) {
		return getFieldLabel(fieldName, formId, new Object[0]);
	}
	public static String getFieldLabel(final String fieldName, Object[] messageArguments) {
		return getFieldLabel(fieldName, "", messageArguments);
	}
	/**
	 * Get the field label.
	 *
	 * @param fieldName
	 *            fieldName
	 * @param formId
	 *            form id
	 * @return Message from the Message Source.
	 */
	public static String getFieldLabel(final String fieldName,
			final String formId, Object[] messageArguments) {
		
	

		Locale locale = null;
		
		if(FacesContext.getCurrentInstance() == null) {
			locale = new Locale("hr", "HR");
		} else {
			locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
			if(session.getAttribute("locale") != null) {
				locale = (Locale)session.getAttribute("locale");
			}
		}
		
		/*
		String bundleName = null;
		if (fieldName.startsWith("controller_")) bundleName = CONTROLLER_BUNDLE_NAME;
		else if (fieldName.startsWith("bean_")) bundleName = BEAN_BUNDLE_NAME;
		else if (fieldName.startsWith("entity_")) bundleName = ENTITY_BUNDLE_NAME;		
		else bundleName = BUNDLE_NAME;

		*/
		
		//ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, getClassLoader());

	    MessageFormat formatter = new MessageFormat("");
	    formatter.setLocale(locale);
	    
		/** Look for formId.fieldName, e.g., EmployeeForm.firstName. */
/*
		String label = null;
		try {
			label = bundle.getString(formId + fieldName);
			//return label;
			formatter.applyPattern(label);
			return formatter.format(messageArguments);
		} catch (MissingResourceException e) {
			// do nothing on purpose.
		}

		try {

			label = bundle.getString(fieldName);
		} catch (MissingResourceException e) {

			label = generateLabelValue(fieldName);
		}
		formatter.applyPattern(label);
		
		*/
		return formatter.format(messageArguments);
	}

	private static ClassLoader getClassLoader() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			return JsfFunctions.class.getClassLoader();
		}
		return classLoader;
	}

	/**
	 * Generate the field. Transforms firstName into First Name. This allows
	 * reasonable defaults for labels.
	 *
	 * @param fieldName
	 *            fieldName
	 *
	 * @return generated label name.
	 */
	public static String generateLabelValue(final String fieldName) {
		StringBuffer buffer = new StringBuffer(fieldName.length() * 2);
		char[] chars = fieldName.toCharArray();

		/* Change firstName to First Name. */
		for (int index = 0; index < chars.length; index++) {
			char cchar = chars[index];

			/* Make the first character uppercase. */
			if (index == 0) {
				cchar = Character.toUpperCase(cchar);
				buffer.append(cchar);

				continue;
			}

			/* Look for an uppercase character, if found add a space. */
			if (Character.isUpperCase(cchar)) {
				//buffer.append(' '); Kristijan i Nikica maknuli
				buffer.append(cchar);

				continue;
			}

			buffer.append(cchar);
		}

		return buffer.toString();
	}
}
