package hr.kingict.svis.reports;

import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

public interface DSGenericInterface extends JRDataSource {
	public List<?> getList();
}
