package hr.kingict.svis.obrasci.facade;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.svis.obrasci.param.Const;
import hr.kingict.svis.obrasci.web.DocumentBean;
import hr.kingict.svis.obrasci.web.OrderBean;
import hr.kingict.svis.obrasci.web.Org4Doc;
import hr.kingict.svis.obrasci.web.OrganizationBean;
import hr.kingict.svis.obrasci.web.UserBean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;
import javax.sql.DataSource;

@ManagedBean(name="order")
@ViewScoped
public class OrderFacadeProdukcija
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  protected IApplicationContext ctxA = null;
  private ArrayList<OrderBean> orderList = new ArrayList();
  private Integer selected;
  private boolean confirm;
  private int step;
  private Integer kolicina;
  private Integer id;
  private OrganizationBean dobavljac;
  private DocumentBean selDoc;
  private Org4Doc selDobavljac;
  private BigDecimal cijena;
  private java.util.Date datumNarudzbe;
  private String napomena;
  private int rola;
  private UserBean ub;
  private OrganizationBean narucitelj;
  private ArrayList<DocumentBean> docList = new ArrayList();
  private ArrayList<Org4Doc> orgList = new ArrayList();
  
  public OrderFacadeProdukcija()
  {
    ITransaction trx = null;
    List parm = new ArrayList();
    ResultSet rs = null;
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      this.rola = ((Integer)context.getExternalContext().getSessionMap().get("rola")).intValue();
      if ((this.rola != 5) && (this.rola != 2) && (this.rola != 12) && (this.rola != 1226) && (this.rola != 1228))
      {
        context.getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "error?faces-redirect=true");
      }
      else
      {
        this.ctxA = ApplicationContextFactory.getCurrentContext();
        setStep(1);
        
        this.datumNarudzbe = new java.util.Date();
        this.selDoc = new DocumentBean();
        this.selDobavljac = new Org4Doc();
        this.narucitelj = new OrganizationBean();
        
        trx = this.ctxA.getDatabaseManager().createTransaction("sys");
        
        this.ub = ((UserBean)context.getExternalContext().getSessionMap().get("user"));
        if (this.rola == Const.ROLA_OBJEKT.intValue())
        {
          parm.add(this.ub.getIdOrganizacije());
          rs = trx.executePreparedQueryById("test", "DokumentiNarudzbaObjektHistory", parm);
        }
        else
        {
          parm.add(this.ub.getIdOrganizacije());
          rs = trx.executePreparedQueryById("test", "DokumentiNarudzbaHistory", parm);
        }
        while (rs.next())
        {
          DocumentBean doc = new DocumentBean();
          doc.setIdDocument(Integer.valueOf(rs.getInt(1)));
          doc.setCode(rs.getString(2));
          doc.setDesc(rs.getString(3));
          doc.setIdUnit(Integer.valueOf(rs.getInt(4)));
          doc.setUnit(rs.getString(5));
          doc.setPrice(rs.getBigDecimal(6));
          doc.setMultiply(Integer.valueOf(rs.getInt(7)));
          doc.setPrefix(rs.getString(8));
          doc.setSerialNo(rs.getString(9));
          doc.setCurrentSerialNo(Integer.valueOf(rs.getInt(10)));
          doc.setPakiranjeKom(Integer.valueOf(rs.getInt(11)));
          doc.setMaxNumber(rs.getInt(12));
          this.docList.add(doc);
        }
        rs.close();
        if (this.rola == Const.ROLA_OBJEKT.intValue())
        {
          rs = trx.executePreparedQueryById("test", "SelectNaruciteljObjekt", parm);
          if (rs.next())
          {
            this.narucitelj.setIdOrganization(Integer.valueOf(rs.getInt(1)));
            this.narucitelj.setName(rs.getString(2));
            this.narucitelj.setTel(rs.getString(5));
            this.narucitelj.setFax(rs.getString(6));
            this.narucitelj.setEmail(rs.getString(7));
            this.narucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
            this.narucitelj.setOib(rs.getString(9));
            this.narucitelj.setType(Const.CODE_OBJEKT);
          }
        }
        else
        {
          rs = trx.executePreparedQueryById("test", "SelectNaruciteljOrg", parm);
          if (rs.next())
          {
            this.narucitelj.setIdOrganization(Integer.valueOf(rs.getInt(1)));
            this.narucitelj.setName(rs.getString(2));
            this.narucitelj.setTel(rs.getString(5));
            this.narucitelj.setFax(rs.getString(6));
            this.narucitelj.setEmail(rs.getString(7));
            this.narucitelj.setAddress(rs.getString(3) + ", " + rs.getString(4));
            this.narucitelj.setOib(rs.getString(9));
            this.narucitelj.setType(Const.CODE_ORG);
          }
        }
        rs.close();
        
        rs = trx.executePreparedQueryById("test", "CheckOrderReceptionExists", parm);
        if (rs.next())
        {
          int brojNepot = rs.getInt(1);
          if (brojNepot > 0) {
            context.addMessage(null, new FacesMessage("Molimo vas potvrdite prijem svih pristiglih narudžbi.", ""));
          }
        }
        rs.close();
      }
    }
    catch (DatabaseException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (SQLException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (ModuleException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
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
  }
  
  private int getNextId(int length)
  {
    int maxId = 0;
    for (int i = 0; i < length; i++) {
      if (((OrderBean)this.orderList.get(i)).getId().intValue() >= maxId) {
        maxId = ((OrderBean)this.orderList.get(i)).getId().intValue();
      }
    }
    return maxId + 1;
  }
  
  private String generateSerialNumber(Integer counter, String serijskiBroj)
  {
    String genSerialNum = null;
    try
    {
      System.out.println("SERIJSKI BROJ: " + serijskiBroj);
      System.out.println("Brojac: " + counter);
      String reverse = new StringBuffer(serijskiBroj).reverse().toString();
      String brojacRev = new StringBuffer(counter.toString()).reverse().toString();
      System.out.println("Rev serijski broj: " + reverse);
      System.out.println("Rev brojac: " + brojacRev);
      genSerialNum = brojacRev + reverse.substring(brojacRev.length(), reverse.length());
      genSerialNum = new StringBuffer(genSerialNum).reverse().toString();
      System.out.println("GENERIRANI SERIJSKI BROJ: " + genSerialNum);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return genSerialNum;
  }
  
  public void add(ActionEvent event)
  {
    DocumentBean doc = getSelDoc();
    if ((doc == null) || (this.kolicina == null) || (this.cijena == null))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage("Niste unijeli sve potrebne podatke", ""));
      
      return;
    }
    if (getSelDobavljac() == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage("Niste odabrali dobavljača.", ""));
      
      return;
    }
    if (doc.getMaxNumber().intValue() != -1 && this.kolicina.intValue() > doc.getMaxNumber().intValue())
    {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage("Maksimalna dozvoljena količina na zahtjevu za obrazac " + doc.getCode() + " je " + doc.getMaxNumber().intValue() + ".", ""));
      
      return;
    }
    if (doc.getMultiply() != null)
    {
      int visekratnik = doc.getMultiply().intValue();
      
      System.out.println("VIŠEKRATNIK : " + visekratnik);
      if (visekratnik == 0) {
        visekratnik = 1;
      }
      if (this.kolicina.intValue() % visekratnik == 0)
      {
        OrderBean ob = new OrderBean();
        ob.setId(new Integer(getNextId(this.orderList.size())));
        ob.setDoc(doc);
        ob.setCijena(getCijena());
        ob.setKolicina(getKolicina());
        ob.setDobavljac(getSelDobavljac());
        this.orderList.add(ob);
      }
      else
      {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Količina treba biti višekratnik broja " + visekratnik, ""));
      }
    }
  }
  
  public void handleClose()
  {
    ITransaction trx = null;
    List parm = new ArrayList();
    ResultSet rs = null;
    try
    {
      setSelDobavljac(null);
      this.orgList.clear();
      setKolicina(null);
      setCijena(null);
      
      trx = this.ctxA.getDatabaseManager().createTransaction("sys");
      
      parm.add(getSelDoc().getIdDocument());
      rs = trx.executePreparedQueryById("test", "Org4Doc", parm);
      
      int brojac = 0;
      while (rs.next())
      {
        Org4Doc item = new Org4Doc();
        item.setIdOrganizacija(Integer.valueOf(rs.getInt(1)));
        item.setNaziv(rs.getString(2));
        item.setCijena(rs.getBigDecimal(3));
        System.out.println("ITEM: " + item.getNaziv() + " * " + item.getIdOrganizacija());
        this.orgList.add(item);
        brojac++;
      }
      if (brojac == 1) {
        setSelDobavljac((Org4Doc)this.orgList.get(0));
      }
    }
    catch (DatabaseException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (SQLException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (ModuleException e)
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
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
  }
  
  public void clear(ActionEvent event)
  {
    setKolicina(null);
    setCijena(null);
    setSelDobavljac(null);
    setSelDoc(new DocumentBean());
  }
  
  public void next(ActionEvent event)
  {
    if (this.orderList.size() > 0)
    {
      setStep(2);
    }
    else
    {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage("Vaša narudžba je prazna!", ""));
    }
  }
  
  public void cancel(ActionEvent event)
  {
    FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "order?faces-redirect=true");
  }
  
  public void setParam()
  {
    System.out.println("set param");
    System.out.println("set param: " + getSelected());
  }
  
  public void delete()
  {
    try
    {
      for (int i = 0; i < this.docList.size(); i++)
      {
        System.out.println(((OrderBean)this.orderList.get(i)).getId());
        if (((OrderBean)this.orderList.get(i)).getId().intValue() == getSelected().intValue())
        {
          this.orderList.remove(i);
          break;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private boolean orderDB()
  {
    boolean error = false;
    ITransaction trx = null;
    List parm = new ArrayList();
    ResultSet rs = null;
    try
    {
      trx = this.ctxA.getDatabaseManager().createTransaction("sys");
      
      rs = trx.executePreparedQueryById("test", "GetSequenceOrder", parm);
      Integer idOrder = null;
      if (rs.next()) {
        idOrder = Integer.valueOf(rs.getInt(1));
      }
      rs.close();
      
      Calendar cal1 = Calendar.getInstance();
      Calendar cal2 = Calendar.getInstance();
      java.util.Date utilDate = new java.util.Date();
      cal1.setTime(utilDate);
      
      cal2.setTime(getDatumNarudzbe());
      java.sql.Date sqlDate = new java.sql.Date(cal1.getTime().getTime());
      
      parm.add(idOrder);
      parm.add(new Integer(this.orderList.size()));
      parm.add(new java.sql.Date(cal2.getTime().getTime()));
      parm.add(Const.UNESENO);
      parm.add(new java.sql.Date(cal1.getTime().getTime()));
      parm.add(this.ub.getIdKorisnik());
      parm.add(new Integer(1));
      

      parm.add(new Integer(0));
      
      BigDecimal ukupno = BigDecimal.ZERO;
      for (OrderBean ob : this.orderList) {
        ukupno = ukupno.add(ob.getCijena());
      }
      System.out.println("Ukupno: " + ukupno.toString());
      parm.add(ukupno);
      parm.add(this.ub.getIdKorisnik());
      parm.add(this.ub.getIdOrganizacije());
      parm.add(this.rola == Const.ROLA_OBJEKT.intValue() ? Const.CODE_OBJEKT : Const.CODE_ORG);
      parm.add(this.napomena);
      
      trx.executePreparedUpdateById("test", "InsertOrder", parm);
      trx.commit();
      for (OrderBean item : this.orderList)
      {
        parm.clear();
        
        Integer idItem = null;
        
        rs = trx.executePreparedQueryById("test", "GetSequenceOrderItem", parm);
        if (rs.next()) {
          idItem = Integer.valueOf(rs.getInt(1));
        }
        rs.close();
        
        int param3 = 0;
        int barcode1 = 0;
        int barcode2 = 0;
        try
        {
          InitialContext ctx = new InitialContext();
          DataSource ds = (DataSource)ctx.lookup("svisDS");
          
          Connection conn = ds.getConnection();
          String proc3StoredProcedure = "";
          if (item.getDoc().getCode().equals("BARKOD-151046"))
          {
            proc3StoredProcedure = "{ call vis_ex.assign_barcodes(?, ?, ?, ?, ?, ?) }";
            
            CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
            



            System.out.println("KOLIČINA: " + item.getKolicina().intValue());
            System.out.println("ID ORDER: " + idOrder);
            System.out.println("ID DOK: " + item.getDoc().getIdDocument().intValue());
            
            int tempKom = item.getDoc().getPakiranjeKom().intValue();
            System.out.println("TEMP PAKIRANJE: " + tempKom);
            if (tempKom == 0) {
              tempKom = item.getKolicina().intValue();
            } else {
              tempKom = item.getKolicina().intValue() * tempKom;
            }
            System.out.println("TEMP KOLIČINE FINAL: " + tempKom);
            

            cs.setInt(1, tempKom);
            cs.setInt(2, idItem.intValue());
            cs.setInt(3, this.ub.getIdKorisnik().intValue());
            cs.setInt(4, this.narucitelj.getIdOrganization().intValue());
            

            cs.registerOutParameter(5, 4);
            cs.registerOutParameter(6, 4);
            
            cs.execute();
            
            barcode1 = cs.getInt(5);
            System.out.println("barkod od: " + barcode1);
            
            barcode2 = cs.getInt(6);
            System.out.println("barkod d0: " + barcode2);
          }
          else
          {
            proc3StoredProcedure = "{ call vis_ex.doc_Order_Serial(?, ?, ?) }";
            
            CallableStatement cs = conn.prepareCall(proc3StoredProcedure);
            



            int tempKom = item.getDoc().getPakiranjeKom().intValue();
            System.out.println("TEMP PAKIRANJE: " + tempKom);
            
            System.out.println("KOLIČINA: " + item.getKolicina().intValue());
            System.out.println("ID DOK: " + item.getDoc().getIdDocument().intValue());
            if (tempKom == 0) {
              tempKom = item.getKolicina().intValue();
            } else {
              tempKom = item.getKolicina().intValue() * tempKom;
            }
            System.out.println("TEMP KOLIČINE FINAL: " + tempKom);
            
            cs.setInt(1, tempKom);
            cs.setInt(2, item.getDoc().getIdDocument().intValue());
            

            cs.registerOutParameter(3, 4);
            
            cs.execute();
            
            param3 = cs.getInt(3);
            System.out.println("rezultat: " + param3);
          }
          conn.close();
        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.out.println("error");
        }
        parm.clear();
        parm.add(idItem);
        parm.add(idOrder);
        parm.add(item.getDoc().getIdDocument());
        parm.add(item.getKolicina());
        parm.add(item.getCijena());
        parm.add(item.getDoc().getPrice());
        parm.add(item.getDobavljac().getIdOrganizacija());
        if (item.getDoc().getCode().equals("BARKOD-151046"))
        {
          parm.add(String.valueOf(barcode1));
          parm.add(String.valueOf(barcode2));
        }
        else if ("0".equals(item.getDoc().getSerialNo()))
        {
          parm.add("");
          parm.add("");
        }
        else if (item.getDoc().getPakiranjeKom().intValue() > 0)
        {
          parm.add(generateSerialNumber(new Integer(param3 - item.getDoc().getPakiranjeKom().intValue() * item.getKolicina().intValue() + 1), item.getDoc().getSerialNo()));
          parm.add(generateSerialNumber(new Integer(param3), item.getDoc().getSerialNo()));
        }
        else
        {
          parm.add(generateSerialNumber(new Integer(param3 - item.getKolicina().intValue() + 1), item.getDoc().getSerialNo()));
          parm.add(generateSerialNumber(new Integer(param3), item.getDoc().getSerialNo()));
        }
        trx.executePreparedUpdateById("test", "InsertOrderItem", parm);
        trx.commit();
      }
    }
    catch (DatabaseException e)
    {
      error = true;
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (SQLException e)
    {
      error = true;
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
        }
      }
    }
    catch (ModuleException e)
    {
      error = true;
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
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException ep)
        {
          ep.printStackTrace();
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
    return error;
  }
  
  public void potvrdi()
  {
    if (!this.confirm)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage("Niste potvrdili narudžbu!", ""));
    }
    else if (getSelDobavljac() != null)
    {
      boolean error = orderDB();
      if (!error)
      {
        setStep(3);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Vaša narudžba je zaprimljena!", ""));
      }
      else
      {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Dogodila se pogreška prilikom spremanja narudžbe!", ""));
      }
    }
  }
  
  public void handleClose2()
  {
    System.out.println("test row: ");
    System.out.println(getSelDobavljac().getIdOrganizacija() + " * " + getSelDobavljac().getNaziv());
  }
  
  public void calc()
  {
    if (getKolicina() != null) {
      setCijena(new BigDecimal(getKolicina().intValue()).multiply(getSelDoc().getPrice()));
    }
  }
  
  public ArrayList<OrderBean> getOrderList()
  {
    return this.orderList;
  }
  
  public void setOrderList(ArrayList<OrderBean> orderList)
  {
    this.orderList = orderList;
  }
  
  public Integer getSelected()
  {
    return this.selected;
  }
  
  public void setSelected(Integer selected)
  {
    this.selected = selected;
  }
  
  public Integer getKolicina()
  {
    return this.kolicina;
  }
  
  public void setKolicina(Integer kolicina)
  {
    this.kolicina = kolicina;
  }
  
  public OrganizationBean getDobavljac()
  {
    return this.dobavljac;
  }
  
  public void setDobavljac(OrganizationBean dobavljac)
  {
    this.dobavljac = dobavljac;
  }
  
  public DocumentBean getSelDoc()
  {
    return this.selDoc;
  }
  
  public void setSelDoc(DocumentBean selDoc)
  {
    this.selDoc = selDoc;
  }
  
  public Org4Doc getSelDobavljac()
  {
    return this.selDobavljac;
  }
  
  public void setSelDobavljac(Org4Doc selDobavljac)
  {
    this.selDobavljac = selDobavljac;
  }
  
  public ArrayList<DocumentBean> getDocList()
  {
    return this.docList;
  }
  
  public void setDocList(ArrayList<DocumentBean> docList)
  {
    this.docList = docList;
  }
  
  public ArrayList<Org4Doc> getOrgList()
  {
    return this.orgList;
  }
  
  public void setOrgList(ArrayList<Org4Doc> orgList)
  {
    this.orgList = orgList;
  }
  
  public BigDecimal getCijena()
  {
    return this.cijena;
  }
  
  public void setCijena(BigDecimal cijena)
  {
    this.cijena = cijena;
  }
  
  public Integer getId()
  {
    return this.id;
  }
  
  public void setId(Integer id)
  {
    this.id = id;
  }
  
  public boolean isConfirm()
  {
    return this.confirm;
  }
  
  public void setConfirm(boolean confirm)
  {
    this.confirm = confirm;
  }
  
  public int getStep()
  {
    return this.step;
  }
  
  public void setStep(int step)
  {
    this.step = step;
  }
  
  public UserBean getUb()
  {
    return this.ub;
  }
  
  public void setUb(UserBean ub)
  {
    this.ub = ub;
  }
  
  public OrganizationBean getNarucitelj()
  {
    return this.narucitelj;
  }
  
  public void setNarucitelj(OrganizationBean narucitelj)
  {
    this.narucitelj = narucitelj;
  }
  
  public java.util.Date getDatumNarudzbe()
  {
    return this.datumNarudzbe;
  }
  
  public void setDatumNarudzbe(java.util.Date datumNarudzbe)
  {
    this.datumNarudzbe = datumNarudzbe;
  }
  
  public String getNapomena()
  {
    return this.napomena;
  }
  
  public void setNapomena(String napomena)
  {
    this.napomena = napomena;
  }
  
  public int getRola()
  {
    return this.rola;
  }
  
  public void setRola(int rola)
  {
    this.rola = rola;
  }
}
