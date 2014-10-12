package hr.kingict.svis.obrasci.param;

import java.util.Date;

public class Const {
	public final static Integer UNESENO = 1;
	
	public final static Integer PARCIJALNO_DODIJELJENO = 2;
	public final static Integer DODIJELJENO = 3;
	
	public final static Integer PARCIJALNO_ISPORUČENO = 4;
	public final static Integer ISPORUČENO = 5;
	
	public static final Integer PARCIJALNO_PRIMLJENO = 6;
	public final static Integer PRIMLJENO = 7;
	
	public final static Integer VRACENO = 8;
	public final static Integer PRIMITAK_VRACENO = 9;
	
	public final static Integer STORNO =10;
	
	public final static Integer ROLA_UPRAVA_1 = 1228;//rola pristup obrasci
	public final static Integer ROLA_UPRAVA_2 = 1200;//rola pristup certifikati

	public final static Integer ROLA_ADMIN_DOKUMENTI = 1226;
	public final static Integer ROLA_ADMIN_CERTIFIKATI = 1227;
	
	public final static Integer ROLA_VANJSKI_DOKUMENTI = 1228;
	public final static Integer ROLA_VANJSKI_CERTIFIKATI = 1200;
	
	//pristup organizacija s grupom
	public final static Integer ROLA_ORG_DOC_ACCESS = 1104;
	
	public final static Integer ROLA_ORG_CERT = 1;
	public final static Integer ROLA_ORG_DOK = 2;
	public final static Integer ROLA_ORG_CERT_DOK = 12;
	public final static Integer ROLA_TISKARA = 3;
	public final static Integer ROLA_UPRAVA = 4;
	public final static Integer ROLA_OBJEKT = 5;
	
	public final static Integer ROLA_INSPEKTOR = 34;
	public final static Integer ROLA_DVI = 1044;//1101
	public final static Integer ROLA_ADMIN = 1012;
	
	public final static String KOD_UPRAVA = "VD";
	public final static String KOD_TISKARA = "PRINT";
	
	public final static int TYPE_VET_ORG = 1;
	public final static int TYPE_TISKARA = 2;
	
	public final static String SEARCH_MORE = "%";
	public final static String BLANK = "";
	
	public final static String DATE_PATTERN = "dd.MM.yyyy";
	public final static Date START_DATE = new Date(113,0,1);
	
	public final static String PATTERN = "^0+(?!$)";
	
	public final static Integer PAGE_START = 0;
	public final static Integer PAGE_END = 20;
	
	public final static String CERT_OZNAKA_SERIJE = "S";
	public final static String CERT_SERIJA = "000001";
	
	public static final Boolean TEST_OBRACUN_GOD = true;
	public static final Integer POCETNI_BROJ_RACUNA = 1;
	
	public final static String TMP_DIRECTORY = "C://SVIS//";

	public final static Integer CODE_OBJEKT = 2;
	public final static Integer CODE_ORG = 1;
	public final static Integer CODE_DEFAULT = 0;
	
	public final static String TEXT_OBJEKT = "Objekti";
	public final static String TEXT_ORG = "Organizacije";
	
	//obavezno promijeniti kod deploya DataSource i zastavicu TEST (u reportu se ne ispisuje testni primjerak)
	public final static String DATASOURCE = "svisDS";
	public final static Boolean TEST = false;
	
	//za barkod management
	public final static Integer NALJEPNICE = 1;
	public final static Integer EPRUVETE = 2;
	
	public final static String BARKOD = "BARKOD-151046";
	public final static String STAR = "*";
}