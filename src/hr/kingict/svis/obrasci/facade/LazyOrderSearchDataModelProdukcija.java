package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.Assign2Bean;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.UserBean;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyOrderSearchDataModelProdukcija
  extends LazyDataModel<Assign2Bean>
{
  private static final long serialVersionUID = 1L;
  protected IApplicationContext ctxA = null;
  private Boolean search;
  private int rola;
  private UserBean ub;
  private Map<String, Object> searchParam = new HashMap();
  private List<String> selectedStatusi;
  private int activeTab;
  
  public LazyOrderSearchDataModelProdukcija()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    this.rola = ((Integer)context.getExternalContext().getSessionMap().get("rola")).intValue();
    this.ub = ((UserBean)context.getExternalContext().getSessionMap().get("user"));
    
    this.search = Boolean.valueOf(false);
    this.activeTab = Const.CODE_DEFAULT.intValue();
    this.ctxA = ApplicationContextFactory.getCurrentContext();
  }
  
  public LazyOrderSearchDataModelProdukcija(Map<String, Object> searchParam, List<String> selectedStatusi)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    this.rola = ((Integer)context.getExternalContext().getSessionMap().get("rola")).intValue();
    this.ub = ((UserBean)context.getExternalContext().getSessionMap().get("user"));
    if (searchParam != null)
    {
      this.search = Boolean.valueOf(true);
      this.searchParam = searchParam;
      this.selectedStatusi = selectedStatusi;
      this.activeTab = ((Integer)searchParam.get("activeTab")).intValue();
    }
    else
    {
      this.search = Boolean.valueOf(false);
    }
    this.ctxA = ApplicationContextFactory.getCurrentContext();
  }
  
  public void setRowIndex(int rowIndex)
  {
    if ((rowIndex == -1) || (getPageSize() == 0)) {
      super.setRowIndex(-1);
    } else {
      super.setRowIndex(rowIndex % getPageSize());
    }
  }
  
  public List<Assign2Bean> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
  {
    System.out.println("Loading data between " + first + " and " + (first + pageSize));
    
    int dataSize = rowCount();
    setRowCount(dataSize);
    if (this.search.booleanValue()) {
      try
      {
        System.out.println("SELECTED STATUSI: " + this.selectedStatusi.size());
        if (dataSize < pageSize)
        {
          System.out.println("Records " + Const.PAGE_START + " - " + pageSize);
          return getPageData(Const.PAGE_START, Integer.valueOf(pageSize));
        }
        System.out.println("Records " + first + " - " + (first + pageSize));
        return getPageData(new Integer(first), new Integer(first + pageSize));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    if (dataSize > pageSize)
    {
      System.out.println("Change page...");
      try
      {
        System.out.println("Records " + first + " - " + (first + pageSize));
        return getPageData(new Integer(first), new Integer(first + pageSize));
      }
      catch (IndexOutOfBoundsException e)
      {
        System.out.println("Records " + first + " - " + (first + dataSize % pageSize));
        return getPageData(new Integer(first), new Integer(first + dataSize % pageSize));
      }
    }
    return getPageData(new Integer(first), new Integer(first + pageSize));
  }
  
  private List<Assign2Bean> getPageData(Integer min, Integer max)
  {
    List parm = new ArrayList();
    List<Assign2Bean> lassign = new ArrayList();
    Connection conn = null;
    PreparedStatement pstmt1 = null;
    ResultSet rs = null;
    int counter = 0;
    try
    {
      if (!this.search.booleanValue())
      {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("svisDS");
        
        conn = ds.getConnection();
        pstmt1 = conn.prepareStatement(generateQuery(true));
        if ((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) {
          counter = 2;
        } else {
          counter = 1;
        }
        System.out.println("COUNTER: " + counter);
        for (int i = 0; i < counter; i++)
        {
          pstmt1.setString(i * 6 + 1, "%");
          System.out.println("PARAM " + (i * 6 + 1) + ": " + "%");
          if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5))
          {
            pstmt1.setString(i * 6 + 2, this.ub.getIdOrganizacije().toString());
            System.out.println("PARAM " + (i * 6 + 1) + ": " + this.ub.getIdOrganizacije().toString());
          }
          else if ((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()))
          {
            pstmt1.setString(i * 6 + 2, "%");
            System.out.println("PARAM " + (i * 6 + 2) + ": " + "%");
          }
          if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()))
          {
            pstmt1.setString(i * 6 + 3, "%");
            System.out.println("PARAM " + (i * 6 + 3) + ": " + "%");
          }
          else if (this.rola == 3)
          {
            pstmt1.setString(i * 6 + 3, this.ub.getIdOrganizacije().toString());
            System.out.println("PARAM " + (i * 6 + 3) + ": " + this.ub.getIdOrganizacije().toString());
          }
          SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
          
          pstmt1.setString(i * 6 + 4, format.format(Const.START_DATE));
          System.out.println("PARAM " + (i * 6 + 4) + ": " + format.format(Const.START_DATE));
          pstmt1.setString(i * 6 + 5, format.format(new Date()));
          System.out.println("PARAM " + (i * 6 + 5) + ": " + format.format(new Date()));
          pstmt1.setString(i * 6 + 6, "%");
          System.out.println("PARAM " + (i * 6 + 6) + ": " + "%");
        }
        if ((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()))
        {
          pstmt1.setInt(13, max.intValue());
          System.out.println("PARAM 13: " + max);
          pstmt1.setInt(14, min.intValue());
          System.out.println("PARAM 13: " + min);
        }
        else
        {
          pstmt1.setInt(7, max.intValue());
          System.out.println("PARAM 7: " + max);
          pstmt1.setInt(8, min.intValue());
          System.out.println("PARAM 8: " + min);
        }
        rs = pstmt1.executeQuery();
      }
      else
      {
        String selDoc = (String)this.searchParam.get("selDoc");
        String selNarucitelj = (String)this.searchParam.get("selNarucitelj");
        String selDobavljac = (String)this.searchParam.get("selDobavljac");
        
        String datumOd = (String)this.searchParam.get("datumOd");
        String datumDo = (String)this.searchParam.get("datumDo");
        
        String id = (String)this.searchParam.get("id");
        
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("svisDS");
        
        conn = ds.getConnection();
        pstmt1 = conn.prepareStatement(generateQuery(true));
        if (((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) && (this.activeTab == Const.CODE_DEFAULT.intValue())) {
          counter = 2;
        } else {
          counter = 1;
        }
        System.out.println("COUNTER: " + counter);
        for (int i = 0; i < counter; i++)
        {
          pstmt1.setString(i * 6 + 1, selDoc);
          System.out.println("PARAM " + (i * 6 + 1) + ": " + selDoc);
          if ((this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()) || (this.rola == 3))
          {
            pstmt1.setString(i * 6 + 2, selNarucitelj);
            System.out.println("PARAM " + (i * 6 + 2) + ": " + selNarucitelj);
          }
          else if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5))
          {
            pstmt1.setString(i * 6 + 2, this.ub.getIdOrganizacije().toString());
            System.out.println("PARAM " + (i * 6 + 2) + ": " + this.ub.getIdOrganizacije().toString());
          }
          if ((this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()) || (this.rola == 2) || (this.rola == 12) || (this.rola == 5))
          {
            pstmt1.setString(i * 6 + 3, selDobavljac);
            System.out.println("PARAM " + (i * 6 + 3) + ": " + selDobavljac);
          }
          else if (this.rola == 3)
          {
            pstmt1.setString(i * 6 + 3, this.ub.getIdOrganizacije().toString());
            System.out.println("PARAM " + (i * 6 + 3) + ": " + this.ub.getIdOrganizacije().toString());
          }
          pstmt1.setString(i * 6 + 4, datumOd);
          System.out.println("PARAM " + (i * 6 + 4) + ": " + datumOd);
          pstmt1.setString(i * 6 + 5, datumDo);
          System.out.println("PARAM " + (i * 6 + 5) + ": " + datumDo);
          pstmt1.setString(i * 6 + 6, id);
          System.out.println("PARAM " + (i * 6 + 6) + ": " + id);
        }
        if (((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) && (counter == 2))
        {
          pstmt1.setInt(13, max.intValue());
          System.out.println("PARAM 13: " + max);
          pstmt1.setInt(14, min.intValue());
          System.out.println("PARAM 14: " + min);
        }
        else
        {
          pstmt1.setInt(7, max.intValue());
          System.out.println("PARAM 7: " + max);
          pstmt1.setInt(8, min.intValue());
          System.out.println("PARAM 8: " + min);
        }
        rs = pstmt1.executeQuery();
      }
      while (rs.next())
      {
        Assign2Bean ab = new Assign2Bean();
        ab.setIdStavkaNarudzbe(Integer.valueOf(rs.getInt(1)));
        ab.setDatumNarudzbe(rs.getDate(2));
        ab.setIdNarudzbe(Integer.valueOf(rs.getInt(3)));
        
        DocumentBean doc = new DocumentBean();
        doc.setIdDocument(Integer.valueOf(rs.getInt(4)));
        doc.setCode(rs.getString(5));
        doc.setDesc(rs.getString(6));
        doc.setIdUnit(Integer.valueOf(rs.getInt(12)));
        doc.setUnit(rs.getString(13));
        doc.setPrice(rs.getBigDecimal(20));
        ab.setDoc(doc);
        
        ab.setKolicina(Integer.valueOf(rs.getInt(7)));
        ab.setCijena(rs.getBigDecimal(8));
        ab.setIdStatus(Integer.valueOf(rs.getInt(9)));
        ab.setStatus(rs.getString(10));
        ab.setIdNarucitelj(Integer.valueOf(rs.getInt(11)));
        ab.setNarucitelj(rs.getString(14));
        

        ab.setIdDobavljac(Integer.valueOf(rs.getInt(16)));
        ab.setDobavljac(rs.getString(17));
        ab.setOrgType(Integer.valueOf(rs.getInt(21)));
        ab.setSerijskiBrojOd(rs.getString(18));
        ab.setSerijskiBrojDo(rs.getString(19));
        
        lassign.add(ab);
      }
      if (pstmt1 != null) {
        pstmt1.close();
      }
      rs.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      if (conn != null) {
        try
        {
          conn.close();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
      }
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ex)
        {
          ex.printStackTrace();
        }
      }
    }
    finally
    {
      if (conn != null) {
        try
        {
          conn.close();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
      }
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
    return lassign;
  }
  
  private int rowCount()
  {
    List parm = new ArrayList();
    ITransaction trx = null;
    Connection conn = null;
    PreparedStatement pstmt1 = null;
    ResultSet rs = null;
    

    int dataSize = 0;
    try
    {
      String selDoc = (String)this.searchParam.get("selDoc");
      String selNarucitelj = (String)this.searchParam.get("selNarucitelj");
      String selDobavljac = (String)this.searchParam.get("selDobavljac");
      
      String datumOd = (String)this.searchParam.get("datumOd");
      String datumDo = (String)this.searchParam.get("datumDo");
      
      String id = (String)this.searchParam.get("id");
      if (this.search.booleanValue())
      {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("svisDS");
        
        conn = ds.getConnection();
        pstmt1 = conn.prepareStatement(generateQuery(false));
        
        pstmt1.setString(1, selDoc);
        if ((this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) {
          pstmt1.setString(2, selNarucitelj);
        } else if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5)) {
          pstmt1.setString(2, this.ub.getIdOrganizacije().toString());
        } else if (this.rola == 3) {
          pstmt1.setString(2, selNarucitelj);
        }
        if ((this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) {
          pstmt1.setString(3, selDobavljac);
        } else if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5)) {
          pstmt1.setString(3, selDobavljac);
        } else if (this.rola == 3) {
          pstmt1.setString(3, this.ub.getIdOrganizacije().toString());
        }
        pstmt1.setString(4, datumOd);
        pstmt1.setString(5, datumDo);
        pstmt1.setString(6, id);
        
        rs = pstmt1.executeQuery();
      }
      else
      {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("svisDS");
        
        conn = ds.getConnection();
        pstmt1 = conn.prepareStatement(generateQuery(false));
        
        pstmt1.setString(1, "%");
        if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5)) {
          pstmt1.setString(2, this.ub.getIdOrganizacije().toString());
        } else if ((this.rola == 3) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) {
          pstmt1.setString(2, "%");
        }
        if ((this.rola == 2) || (this.rola == 12) || (this.rola == 5) || (this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue())) {
          pstmt1.setString(3, "%");
        } else if (this.rola == 3) {
          pstmt1.setString(3, this.ub.getIdOrganizacije().toString());
        }
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        
        pstmt1.setString(4, format.format(Const.START_DATE));
        pstmt1.setString(5, format.format(new Date()));
        pstmt1.setString(6, "%");
        
        rs = pstmt1.executeQuery();
      }
      if (rs.next())
      {
        System.out.println("Data Size: " + rs.getInt(1));
        dataSize = rs.getInt(1);
      }
      if (pstmt1 != null) {
        pstmt1.close();
      }
      rs.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      if (trx != null) {
        try
        {
          trx.close();
        }
        catch (DatabaseException ex)
        {
          ex.printStackTrace();
        }
      }
      if (conn != null) {
        try
        {
          conn.close();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
      }
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ex)
        {
          ex.printStackTrace();
        }
      }
    }
    finally
    {
      if (trx != null) {
        try
        {
          trx.close();
        }
        catch (DatabaseException e)
        {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        try
        {
          conn.close();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
      }
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
    return dataSize;
  }
  
  private String generateQuery(boolean search)
  {
    String queryString = null;
    try
    {
      String selNarucitelj = (String)this.searchParam.get("selNarucitelj");
      if (this.selectedStatusi != null) {
        System.out.println("statusa: " + this.selectedStatusi.size());
      } else {
        System.out.println("Lista statusa je prazna");
      }
      System.out.println("ACTIVE TAB: " + this.activeTab);
      if ((this.rola == Const.ROLA_ADMIN_DOKUMENTI.intValue()) || (this.rola == Const.ROLA_UPRAVA_1.intValue()) || (this.rola == 3))
      {
        if ((this.activeTab == Const.CODE_ORG.intValue()) && (!selNarucitelj.equals("")))
        {
          if (search) {
            queryString = "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t6.name1, t1.id_status as id_status2, id_dobavljac, t7.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t2.narucitelj_tip = 1 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') order by t2.id_order desc, t1.id_gn_item_order desc, t1.id_gn_item_orders_type ";
          } else {
            queryString = 
              "select count(*) from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t2.narucitelj_tip = 1 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and  t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
          }
        }
        else if ((this.activeTab == Const.CODE_OBJEKT.intValue()) && (!selNarucitelj.equals("")))
        {
          if (search) {
            queryString = "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t7.name as name1, t1.id_status as id_status2, id_dobavljac, t8.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join vis.est_establishments t6 on (t2.id_narucitelj = t6.id_establishment) left join vis.est_documents t7 on (t6.id_document = t7.id_document) left join sm.sm_organizations t8 on (t1.id_dobavljac = t8.id_organization) where t2.narucitelj_tip = 2 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') order by t2.id_order desc, t1.id_gn_item_order desc, t1.id_gn_item_orders_type ";
          } else {
            queryString = 
              "select count(*) from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t2.narucitelj_tip = 2 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and  t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
          }
        }
        else if (this.activeTab == Const.CODE_DEFAULT.intValue()) {
          if (search) {
            queryString = "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t6.name1, t1.id_status as id_status2, id_dobavljac, t7.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t2.narucitelj_tip = 1 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') union all select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t7.name as name1, t1.id_status as id_status2, id_dobavljac, t8.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join vis.est_establishments t6 on (t2.id_narucitelj = t6.id_establishment) left join vis.est_documents t7 on (t6.id_document = t7.id_document) left join sm.sm_organizations t8 on (t1.id_dobavljac = t8.id_organization) where t2.narucitelj_tip = 2 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') order by 22 desc, 3 desc, 1 ";
          } else {
            queryString = 
              "select count(*) from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and  t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
          }
        }
        if (search)
        {
          queryString = 
          
            queryString + ") a " + "where rownum <= ? ";
          if (this.selectedStatusi != null)
          {
            int broj = this.selectedStatusi.size();
            System.out.println("Stat. br. " + broj);
            if (broj > 0)
            {
              queryString = queryString + " and a.id_status in ( ";
              for (String s : this.selectedStatusi) {
                queryString = queryString + s + ",";
              }
              queryString = queryString.substring(0, queryString.length() - 1);
              queryString = queryString + " ) ";
            }
          }
          queryString = 
          
            queryString + "order by a.id_order desc " + ") " + "WHERE r__ > ?";
        }
        else if (this.selectedStatusi != null)
        {
          int broj = this.selectedStatusi.size();
          System.out.println("Stat. br. " + broj);
          if (broj > 0)
          {
            queryString = queryString + " and t4.id in ( ";
            for (String s : this.selectedStatusi) {
              queryString = queryString + s + ",";
            }
            queryString = queryString.substring(0, queryString.length() - 1);
            queryString = queryString + " ) ";
          }
        }
      }
      else
      {
        if (search)
        {
          if (this.rola == Const.ROLA_OBJEKT.intValue()) {
            queryString = "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t7.name as name1, t1.id_status as id_status2, id_dobavljac, t8.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join vis.est_establishments t6 on (t2.id_narucitelj = t6.id_establishment) left join vis.est_documents t7 on (t6.id_document = t7.id_document) left join sm.sm_organizations t8 on (t1.id_dobavljac = t8.id_organization) where t2.narucitelj_tip = 2 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
          } else {
            queryString = "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( select t1.id_gn_item_orders_type, t2.d_order, t1.id_gn_item_order, t1.id_gn_org_docs, t3.name, t3.notes, t1.kolicina, t1.iznos, t1.id_status, t4.naziv, t2.id_narucitelj, t5.id, t5.naziv as mjera, t6.name1, t1.id_status as id_status2, id_dobavljac, t7.name1 as naziv_org, t1.serijski_broj_od, t1.serijski_broj_do, t3.price, t2.narucitelj_tip, t2.id_order from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t2.narucitelj_tip = 1 and t1.id_gn_org_docs like nvl(?, '%') and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
          }
        }
        else if (this.rola == Const.ROLA_OBJEKT.intValue()) {
          queryString = 
            "select count(*) from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t1.id_gn_org_docs like nvl(?, '%') and t2.narucitelj_tip = 2 and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and  t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
        } else {
          queryString = 
            "select count(*) from vis_ex.gn_item_orders_type t1 left join vis_ex.gn_item_orders t2 on (t1.id_gn_item_order = t2.id_order) left join vis_ex.gn_docs t3 on (t1.id_gn_org_docs = t3.id_gn_org_docs) left join vis_ex.gn_item_order_status t4 on (t1.id_status = t4.id) left join vis_ex.gn_measure_unit t5 on (t3.id_measure_unit = t5.id) left join sm.sm_organizations t6 on (t2.id_narucitelj = t6.id_organization) left join sm.sm_organizations t7 on (t1.id_dobavljac = t7.id_organization) where t1.id_gn_org_docs like nvl(?, '%') and t2.narucitelj_tip = 1 and t2.id_narucitelj like nvl(?, '%') and id_dobavljac like nvl(?, '%') and  t2.d_order between to_date(?,'dd.MM.yyyy') and to_date(?,'dd.MM.yyyy') and t1.id_gn_item_order like nvl(?, '%') ";
        }
        if (this.selectedStatusi != null)
        {
          int broj = this.selectedStatusi.size();
          System.out.println("Stat. br. " + broj);
          if (broj > 0)
          {
            queryString = queryString + " and t2.id_status in ( ";
            for (String s : this.selectedStatusi) {
              queryString = queryString + s + ",";
            }
            queryString = queryString.substring(0, queryString.length() - 1);
            queryString = queryString + " ) ";
          }
        }
        if (search) {
          queryString = 
          



            queryString + "order by t2.id_order desc, t1.id_gn_item_order desc " + ") a " + "where rownum <= ? " + ") " + "WHERE r__ > ?";
        }
      }
      System.out.println("UPIT: " + queryString);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return queryString;
  }
  
  public Boolean getSearch()
  {
    return this.search;
  }
  
  public void setSearch(Boolean search)
  {
    this.search = search;
  }
  
  public Map<String, Object> getSearchParam()
  {
    return this.searchParam;
  }
  
  public void setSearchParam(Map<String, Object> searchParam)
  {
    this.searchParam = searchParam;
  }
  
  public int getRola()
  {
    return this.rola;
  }
  
  public void setRola(int rola)
  {
    this.rola = rola;
  }
  
  public List<String> getSelectedStatusi()
  {
    return this.selectedStatusi;
  }
  
  public void setSelectedStatusi(List<String> selectedStatusi)
  {
    this.selectedStatusi = selectedStatusi;
  }
}
