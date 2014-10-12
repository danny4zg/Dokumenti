package hr.kingict.svis.web.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

public class JsfCoreLibrary extends AbstractTagLibrary{

	
	/** Namespace used to import this library in Facelets pages  */
    public static final String NAMESPACE = "http://www.king.com/jsf/core";

    /**  Current instance of library. */
    public static final JsfCoreLibrary INSTANCE = new JsfCoreLibrary();

    /**
     * Creates a new JstlCoreLibrary object.
     *
     */
    public JsfCoreLibrary() {
        super(NAMESPACE);

        this.addTagHandler("setVar", SetVarHandler.class);
        this.addTagHandler("setCustomValidator", SetCustomValidatorHandler.class);


        try {
            Method[] methods = JsfFunctions.class.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isStatic(methods[i].getModifiers())) {
                    this.addFunction(methods[i].getName(), methods[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
