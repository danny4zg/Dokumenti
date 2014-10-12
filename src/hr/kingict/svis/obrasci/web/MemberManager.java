package hr.kingict.svis.obrasci.web;

import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.svis.obrasci.param.Const;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;

public class MemberManager {
	protected IApplicationContext ctxA = null;
	
	public Boolean authenticate(String username, String password) throws DatabaseException, SQLException{
		boolean isLogged = false;
		ITransaction trx = null;
		List parm = new ArrayList();
		ResultSet rs = null;
		List<Integer> roleAll = new ArrayList<Integer>();
		
		StringBuffer hexString = new StringBuffer();
    	
    	try {
    		
    		this.ctxA = ApplicationContextFactory.getCurrentContext();//framework init
    		
    		MessageDigest algorithm = MessageDigest.getInstance("MD5");
    		algorithm.reset();
    		algorithm.update(password.getBytes());
    		byte messageDigest[] = algorithm.digest();
    	            
    		for (int i=0;i<messageDigest.length;i++) {
    			//hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
    			
    			String hex=Integer.toHexString(0xff & messageDigest[i]);
    		    if(hex.length()==1) hexString.append('0');
    		    hexString.append(hex);
    		}	
			parm.add(username.toLowerCase());
    		parm.add(username.toUpperCase());
    		parm.add(hexString.toString().toUpperCase());
    		
    		trx = ctxA.getDatabaseManager().createTransaction("sys");
			rs =  trx.executePreparedQueryById("test", "Login", parm);
    	
			UserBean ub = new UserBean();
			Integer tmpRola = null;
			int step = 0;
			String org_type = null;
			
			boolean isFirst = true;
			HashMap<Integer, String> hm = new HashMap<Integer, String>();
			
			while(rs.next()){
				
				if (isFirst) {
					ub.setIdKorisnik(rs.getInt(1));
					ub.setIdOrganizacije(rs.getInt(2));
					ub.setIme(rs.getString(3));
					ub.setPrezime(rs.getString(4));
					ub.setUsername(rs.getString(5));
					ub.setOib(rs.getString(6));
					ub.setTel(rs.getString(11));
					ub.setFax(rs.getString(12));
					ub.setEmail(rs.getString(13));
					
					isFirst = false;
					step = 1;
					
					System.out.println("ID: " + rs.getInt(1));
	        		System.out.println("ID ORG: " + rs.getInt(2));
	        		System.out.println("Ime: " + rs.getString(3));
	        		System.out.println("Prezime: " + rs.getString(4));
	        		System.out.println("Username: " + rs.getString(5));
	        		System.out.println("OIB: " + rs.getString(6));
	        		
	        		org_type = rs.getString("org_type");
				}
				
				System.out.println("ROLA: " + rs.getString(8));
				tmpRola = rs.getInt(7);
				
				roleAll.add(tmpRola);
			}
		
			rs.close();
			
			
			//provjera koja rola
			for (int i=0; i<roleAll.size();i++){
				if (roleAll.get(i).intValue() == Const.ROLA_ADMIN){
					//hm.put(rs.getInt(7), rs.getString(8));
	
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().getSessionMap().put("rola", roleAll.get(i).intValue());
					context.getExternalContext().getSessionMap().put("certadmin", true);	
					
					isLogged = true;
					
					break;
				}
				else if (roleAll.get(i).intValue() == Const.ROLA_UPRAVA_1.intValue() || roleAll.get(i).intValue() == Const.ROLA_ADMIN_DOKUMENTI.intValue()){
					FacesContext context = FacesContext.getCurrentInstance();
					
					if (!org_type.equals(Const.KOD_UPRAVA)){
						break;
					} else {
						context.getExternalContext().getSessionMap().put("rola", roleAll.get(i).intValue());
						context.getExternalContext().getSessionMap().put("certadmin", false);
						//hm.put(rs.getInt(7), rs.getString(8));
					}			
					
					isLogged = true;
					
					//break;
				}
				else if (roleAll.get(i).intValue() == Const.ROLA_UPRAVA_2.intValue() || roleAll.get(i).intValue() == Const.ROLA_ADMIN_CERTIFIKATI.intValue()){
					
					//hm.put(rs.getInt(7), rs.getString(8));
					
					FacesContext context = FacesContext.getCurrentInstance();
					//context.getExternalContext().getSessionMap().put("rola", roleAll.get(i).intValue());
					
					if (tmpRola.intValue() == Const.ROLA_ADMIN_CERTIFIKATI.intValue())
						context.getExternalContext().getSessionMap().put("certadmin", true);
					else 
						context.getExternalContext().getSessionMap().put("certadmin", false);
					
					isLogged = true;
					
					//break;
				}
				else if (roleAll.get(i).intValue() == Const.ROLA_DVI.intValue()) {
					System.out.println("DVI...");
					
					//hm.put(rs.getInt(7), rs.getString(8));
					
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().getSessionMap().put("rola", roleAll.get(i).intValue());
					context.getExternalContext().getSessionMap().put("certadmin", false);
					
					isLogged = true;
					
					break;
				}
				else if (roleAll.get(i).intValue() == Const.ROLA_INSPEKTOR.intValue()){
					System.out.println("INSPEKTOR...");
					
					//hm.put(rs.getInt(7), rs.getString(8));
					
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().getSessionMap().put("rola", roleAll.get(i).intValue());
					context.getExternalContext().getSessionMap().put("certadmin", false);
					
					isLogged = true;
					
					break;
				}
			}
			
			
			//provjera za tiskaru
			if (isLogged == false && step == 1){
			
				parm.clear();
				parm.add(ub.getIdOrganizacije());
	    		
				rs =  trx.executePreparedQueryById("test", "CheckIfTiskara", parm);
				
				while (rs.next()){
					
					hm.put(Const.ROLA_TISKARA, "Tiskara");
					
					isLogged = true;
					step = 2;
				}
				
				if (step == 2){ 
					System.out.println("Logiran kao tiskara");
				
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().getSessionMap().put("rola", Const.ROLA_TISKARA.intValue());
					context.getExternalContext().getSessionMap().put("certadmin", false);
				}
			}
			
			rs.close();
			
			//provjera za organizaciju
			if (isLogged == false && step >= 1){
				
				int rola = 0;
				
				for (int i=0; i<roleAll.size();i++){
					
					if (roleAll.get(i).intValue() == Const.ROLA_VANJSKI_DOKUMENTI.intValue()) {
						if (rola == 1) rola = Const.ROLA_ORG_CERT_DOK.intValue();
						else rola = Const.ROLA_ORG_DOK.intValue();
						
						isLogged = true;
						step = 3;
					}
					else if (roleAll.get(i).intValue() == Const.ROLA_VANJSKI_CERTIFIKATI.intValue()) {
						if (rola == 2) rola = Const.ROLA_ORG_CERT_DOK.intValue();
						else rola = Const.ROLA_ORG_CERT.intValue();
						
						isLogged = true;
						step = 3;
					}
					
					hm.put(roleAll.get(i).intValue(), roleAll.get(i).intValue() == Const.ROLA_ORG_DOK.intValue() ? "Pristup dokumentima" : (roleAll.get(i).intValue()== Const.ROLA_ORG_CERT.intValue() ? "Pristup certifikatima" :"Pristup cert+dok"));
					
					System.out.println("Rola: " + rola);

				}
			
				if (step == 3) {
					System.out.println("Logiran kao vet org");
					
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().getSessionMap().put("rola", rola);
					context.getExternalContext().getSessionMap().put("certadmin", false);
				}
				else {//check za objekt
					parm.clear();
					parm.add(ub.getIdKorisnik());

					rs =  trx.executePreparedQueryById("test", "isLoginObjekt", parm);
					
					if (rs.next()){
						System.out.println("Logiran kao OBJEKT...");
						hm.put(Const.ROLA_OBJEKT, "Pristup dokumentima");
						ub.setIdOrganizacije(rs.getInt(1));//id_objekta
						isLogged = true;
						
						FacesContext context = FacesContext.getCurrentInstance();
						context.getExternalContext().getSessionMap().put("rola", Const.ROLA_OBJEKT);
						context.getExternalContext().getSessionMap().put("certadmin", false);
						
						step = 4;
					}
				}
				
			}
			
			rs.close();
			
			ub.setRola(hm);
			
			System.out.println("Korak provjere:" + step);
			System.out.println("Looged: " + isLogged);
			
			FacesContext context = FacesContext.getCurrentInstance();
	    	context.getExternalContext().getSessionMap().put("user", ub);
		}
    	catch (SQLException e){
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
			
			isLogged = false;
		}
		finally {
			if (trx != null) trx.close();
			if (rs != null) rs.close();
		}
		return isLogged;
	}
	
}
