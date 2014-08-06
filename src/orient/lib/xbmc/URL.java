package orient.lib.xbmc;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import orient.lib.xbmc.utils.URIUtils;

public class URL {

	protected int m_iPort;
	protected String m_strHostName;
	protected String m_strShareName;
	protected String m_strDomain;
	protected String m_strUserName;
	protected String m_strPassword;
	protected String m_strFileName;
	protected String m_strProtocol;
	protected String m_strFileType;
	protected String m_strOptions;
	protected String m_strProtocolOptions;
	protected UrlOptions m_options;
	protected UrlOptions m_protocolOptions;


	public URL(String url) {
		parse(url);
	}


	/** TODO uses erase
	 * 
	 * @param url
	 */
	public void parse(String url) {
		reset();

		// start by validating the path
		String strURL = Util.validatePath(url);

		// strURL can be one of the following:
		// format 1: protocol://[username:password]@hostname[:port]/directoryandfile
		// format 2: protocol://file
		// format 3: drive:directoryandfile
		//
		// first need 2 check if this is a protocol or just a normal drive & path
		if (strURL == null || strURL.isEmpty()) return ;
		if (strURL.equals("?")) return;

		// form is format 1 or 2
		// format 1: protocol://[domain;][username:password]@hostname[:port]/directoryandfile
		// format 2: protocol://file

		// decode protocol
		int iPos = strURL.indexOf("://");
		if (iPos == -1)
		{
			// This is an ugly hack that needs some work.
			// example: filename /foo/bar.zip/alice.rar/bob.avi
			// This should turn into zip://rar:///foo/bar.zip/alice.rar/bob.avi
			iPos = 0;
			boolean is_apk = (strURL.indexOf(".apk/", iPos) > -1);
			while (true)
			{
				if (is_apk)
					iPos = strURL.indexOf(".apk/", iPos);
				else
					iPos = strURL.indexOf(".zip/", iPos);

				int extLen = 3;
				if (iPos == -1)
				{
					/* set filename and update extension*/
					setFileName(strURL);
					return ;
				}
				iPos += extLen + 1;
				String archiveName = strURL.substring(0, iPos);
				File file = new File(archiveName);

				if (file.isDirectory())

					archiveName = Encode(archiveName);
				if (is_apk)
				{
					parse("apk://" + archiveName + "/" + strURL.substring(iPos + 1));
				}
				else
				{
					parse("zip://" + archiveName + "/" + strURL.substring(iPos + 1));
				}
				return;
			}

		}

		else
		{
			SetProtocol(strURL.substring(0, iPos));
			iPos += 3;
		}

		// virtual protocols
		// why not handle all format 2 (protocol://file) style urls here?
		// ones that come to mind are iso9660, cdda, musicdb, etc.
		// they are all local protocols and have no server part, port number, special options, etc.
		// this removes the need for special handling below.
		if (
				m_strProtocol.equals("stack") ||
				m_strProtocol.equals("virtualpath") ||
				m_strProtocol.equals("multipath") ||
				m_strProtocol.equals("filereader") ||
				m_strProtocol.equals("special")
				)
		{
			setFileName(strURL.substring(iPos));
			return;
		}

		// check for username/password - should occur before first /
		if (iPos == -1) iPos = 0;

		// for protocols supporting options, chop that part off here
		// maybe we should invert this list instead?
		int iEnd = strURL.length();
		String sep = null;

		//TODO fix all Addon paths
		String strProtocol2 = GetTranslatedProtocol();
		if(m_strProtocol.equals("rss") ||
				m_strProtocol.equals("rar") ||
				m_strProtocol.equals("addons") ||
				m_strProtocol.equals("image") ||
				m_strProtocol.equals("videodb") ||
				m_strProtocol.equals("musicdb") ||
				m_strProtocol.equals("androidapp"))
			sep = "?";
		else
			if(strProtocol2.equals("http")
					|| strProtocol2.equals("https")
					|| strProtocol2.equals("plugin")
					|| strProtocol2.equals("addons")
					|| strProtocol2.equals("hdhomerun")
					|| strProtocol2.equals("rtsp")
					|| strProtocol2.equals("apk")
					|| strProtocol2.equals("zip"))
				sep = "?;#|";
			else if(strProtocol2.equals("ftp")
					|| strProtocol2.equals("ftps"))
				sep = "?;|";

		if(sep != null)
		{
			int iOptions = strURL.indexOf(sep, iPos);
			if (iOptions != -1)
			{
				// we keep the initial char as it can be any of the above
				int iProto = strURL.indexOf("|",iOptions);
				if (iProto != -1)
				{
					SetProtocolOptions(strURL.substring(iProto+1));
					SetOptions(strURL.substring(iOptions,iProto));
				}
				else
					SetOptions(strURL.substring(iOptions));
				iEnd = iOptions;
			}
		}

		int iSlash = strURL.indexOf("/", iPos);
		if(iSlash >= iEnd)
			iSlash = -1; // was an invalid slash as it was contained in options

		if( !m_strProtocol.equals("iso9660") )
		{
			int iAlphaSign = strURL.indexOf("@", iPos);
			if (iAlphaSign != -1 && iAlphaSign < iEnd && (iAlphaSign < iSlash || iSlash == -1))
			{
				// username/password found
				String strUserNamePassword = strURL.substring(iPos, iAlphaSign);

				// first extract domain, if protocol is smb
				if (m_strProtocol.equals("smb"))
				{
					int iSemiColon = strUserNamePassword.indexOf(";");

					if (iSemiColon != -1)
					{
						m_strDomain = strUserNamePassword.substring(0, iSemiColon);
						strUserNamePassword.substring(0, iSemiColon + 1);
					}
				}

				// username:password
				int iColon = strUserNamePassword.indexOf(":");
				if (iColon != -1)
				{
					m_strUserName = strUserNamePassword.substring(0, iColon);
					m_strPassword = strUserNamePassword.substring(iColon + 1);
				}
				// username
				else
				{
					m_strUserName = strUserNamePassword;
				}

				iPos = iAlphaSign + 1;
				iSlash = strURL.indexOf("/", iAlphaSign);

				if(iSlash >= iEnd)
					iSlash = -1;
			}
		}

		// detect hostname:port/
		if (iSlash == -1)
		{
			String strHostNameAndPort = strURL.substring(iPos, iEnd);
			int iColon = strHostNameAndPort.indexOf(":");
			if (iColon != -1)
			{
				m_strHostName = strHostNameAndPort.substring(0, iColon);
				m_iPort = Integer.parseInt(strHostNameAndPort.substring(iColon + 1));
			}
			else
			{
				m_strHostName = strHostNameAndPort;
			}

		}
		else
		{
			String strHostNameAndPort = strURL.substring(iPos, iSlash); 
			int iColon = strHostNameAndPort.indexOf(":");
			if (iColon != -1)
			{
				m_strHostName = strHostNameAndPort.substring(0, iColon);
				m_iPort = Integer.parseInt(strHostNameAndPort.substring(iColon + 1));
			}
			else
			{
				m_strHostName = strHostNameAndPort;
			}
			iPos = iSlash + 1;
			if (iEnd > iPos)
			{
				m_strFileName = strURL.substring(iPos, iEnd);

				iSlash = m_strFileName.indexOf("/");
				if(iSlash == -1)
					m_strShareName = m_strFileName;
				else
					m_strShareName = m_strFileName.substring(0, iSlash);
			}
		}

		// iso9960 doesnt have an hostname;-)
		if (m_strProtocol.equals("iso9660")
				|| m_strProtocol.equals("musicdb")
				|| m_strProtocol.equals("videodb")
				|| m_strProtocol.equals("sources")
				|| m_strProtocol.equals("pvr")
				|| StringUtils.startsWith(m_strProtocol, "mem"))
		{
			if (m_strHostName != "" && m_strFileName != "")
			{
				m_strFileName = String.format("%s/%s", m_strHostName, m_strFileName);
				m_strHostName = "";
			}
			else
			{
				if (!m_strHostName.isEmpty() && strURL.charAt(iEnd-1)=='/')
					m_strFileName = m_strHostName + "/";
				else
					m_strFileName = m_strHostName;
				m_strHostName = "";
			}
		}

		m_strFileName = StringUtils.replace(m_strFileName, "\\", "/");

		/* update extension */
		setFileName(m_strFileName);

		/* decode urlencoding on this stuff */
		if(URIUtils.protocolHasEncodedHostname(m_strProtocol))
		{
			m_strHostName = Decode(m_strHostName);
			SetHostName(m_strHostName);
		}

		m_strUserName = Decode(m_strUserName);
		m_strPassword = Decode(m_strPassword);
	}


	void SetProtocol(String strProtocol)
	{
	  m_strProtocol = strProtocol;
	  m_strProtocol = StringUtils.lowerCase(m_strProtocol);
	}
	
	public void SetHostName(String strHostName) {
		m_strHostName = strHostName;
	}


	void SetUserName(String strUserName)
	{
		m_strUserName = strUserName;
	}

	void SetPassword(String strPassword)
	{
		m_strPassword = strPassword;
	}


	public void SetOptions(String strOptions) {
		m_strOptions = null;
		m_options = null;

		if( strOptions.length() > 0)
		{
			if(strOptions.charAt(0) == '?' ||
					strOptions.charAt(0) == '#' ||
					strOptions.charAt(0) == ';' ||
					strOptions.indexOf("xml") != -1)
			{
				m_strOptions = strOptions;
				m_options.AddOptions(m_strOptions);
			}

		}
	}

	public void SetProtocolOption(String key, String value)
	{
		m_protocolOptions.AddOption(key, value);
		m_strProtocolOptions = m_protocolOptions.GetOptionsString(false);
	}

	public void SetProtocolOptions(String strOptions) {
		m_strProtocolOptions = null;
		m_protocolOptions = null;
		if (strOptions.length() > 0)
		{
			if (strOptions.charAt(0) == '|')
				m_strProtocolOptions = strOptions.substring(1);
			else
				m_strProtocolOptions = strOptions;
			m_protocolOptions.AddOptions(m_strProtocolOptions);
		}
	}

	public void SetPort(int port)
	{
		m_iPort = port;
	}


	public void setFileName(String strFileName) {
		m_strFileName = strFileName;
		m_strFileType = null;

		if (strFileName == null)
			return;
		
		int slash = m_strFileName.lastIndexOf("\\");
		
		if (slash == -1)
			slash = m_strFileName.lastIndexOf("/");
		
		
		int period = m_strFileName.lastIndexOf('.');
		if(period != -1 && (slash == -1 || period > slash))
			m_strFileType = m_strFileName.substring(period+1);

		if (m_strFileType == null)
			return;
				
		m_strFileType = m_strFileType.trim();
		m_strFileType = StringUtils.lowerCase(m_strFileType);

	}


	public void reset() {
		m_iPort = 0;
		m_strHostName = null;
		m_strShareName = null;
		m_strDomain = null;
		m_strUserName = null;
		m_strPassword = null;
		m_strFileName = null;
		m_strProtocol = null;
		m_strFileType = null;
		m_strOptions = null;
		m_strProtocolOptions = null;
	}



	public  boolean HasPort() 
	{
		return (m_iPort != 0);
	}

	public  int GetPort() 
	{
		return m_iPort;
	}


	public  String GetHostName() 
	{
		return m_strHostName;
	}

	public  String  GetShareName() 
	{
		return m_strShareName;
	}

	public  String GetDomain() 
	{
		return m_strDomain;
	}

	public  String GetUserName() 
	{
		return m_strUserName;
	}

	public  String GetPassWord() 
	{
		return m_strPassword;
	}

	public  String GetFileName() 
	{
		return m_strFileName;
	}

	public  String GetProtocol() 
	{
		return m_strProtocol;
	}

	public  String GetTranslatedProtocol() 
	{
		return TranslateProtocol(m_strProtocol);
	}

	public  String GetFileType() 
	{
		return m_strFileType;
	}

	public  String GetOptions()
	{
		return m_strOptions;
	}

	public String GetProtocolOptions()
	{
		return m_strProtocolOptions;
	}

	public String GetFileNameWithoutPath()
	{
		// *.zip and *.rar store the actual zip/rar path in the hostname of the url
		if ((m_strProtocol.equals("rar")  ||
				m_strProtocol.equals("zip")  ||
				m_strProtocol.equals("apk")) &&
				m_strFileName.isEmpty())
			return URIUtils.getFileName(m_strHostName);

		// otherwise, we've already got the filepath, so just grab the filename portion
		String file = m_strFileName;
		file = URIUtils.removeSlashAtEnd(file);
		return URIUtils.getFileName(file);
	}





	// TODO has problems
	char GetDirectorySeparator() 
	{
		
		if (SystemUtils.IS_OS_WINDOWS && IsLocal())
			return '\\';
		else
			return '/';
	}

	public String Get() 
	{

		if (m_strProtocol == null || m_strProtocol == "")
			return m_strFileName;

		String strURL;

		strURL = GetWithoutFilename();
		
		if (m_strFileName != null)
			strURL += m_strFileName;

		if( m_strOptions != null && !m_strOptions.isEmpty() )
			strURL += m_strOptions;
		if (m_strProtocolOptions != null && !m_strProtocolOptions.isEmpty())
			strURL += "|"+m_strProtocolOptions;

		return strURL;
	}

	String GetWithoutUserDetails(boolean redact) 
	{
		return null;
//		String strURL;
//
//		if (m_strProtocol.equals("stack"))
//		{
//			StackDirectory dir = new StackDirectory();
//			
//			ArrayList<FileItem> items = dir.getDirectory(this);
//			
//			ArrayList<String> newItems;
//			for (int i=0;i<items.size();++i)
//			{
//				URL url = new URL(items.get(0).getPath());
//				items.get(i).setPath(url.GetWithoutUserDetails(redact));
//				newItems.add(items.get(i).getPath());
//			}
//			dir.constructStackPath(newItems, strURL);
//			return strURL;
//		}
//
//		 int sizeneed = m_strProtocol.length()
//				+ m_strDomain.length()
//				+ m_strHostName.length()
//				+ m_strFileName.length()
//				+ m_strOptions.length()
//				+ m_strProtocolOptions.length()
//				+ 10;
//
//		if (redact)
//			sizeneed += StringUtils.length("USERNAME:PASSWORD@");
//
//
//		if (m_strProtocol == "")
//			return m_strFileName;
//
//		strURL = m_strProtocol;
//		strURL += "://";
//
//		if (redact && !m_strUserName.isEmpty())
//		{
//			strURL += "USERNAME";
//			if (!m_strPassword.isEmpty())
//			{
//				strURL += ":PASSWORD";
//			}
//			strURL += "@";
//		}
//
//		if (!m_strHostName.isEmpty())
//		{
//			String strHostName;
//
//			if (URIUtils.protocolHasParentInHostname(m_strProtocol))
//				strHostName = new URL(m_strHostName).GetWithoutUserDetails();
//			else
//				strHostName = m_strHostName;
//
//			if (URIUtils.protocolHasEncodedHostname(m_strProtocol))
//				strURL += Encode(strHostName);
//			else
//				strURL += strHostName;
//
//			if ( HasPort() )
//			{
//				strURL += "".format(":%i", m_iPort);
//			}
//			strURL += "/";
//		}
//		strURL += m_strFileName;
//
//		if( m_strOptions.length() > 0 )
//			strURL += m_strOptions;
//		if( m_strProtocolOptions.length() > 0 )
//			strURL += "|"+m_strProtocolOptions;
//
//		return strURL;
	}

	String GetWithoutFilename() 
	{
		if (m_strProtocol == null || m_strProtocol == "")
			return "";

		 
		String strURL = m_strProtocol;
		strURL += "://";

		if (m_strDomain != null && m_strDomain != "")
		{
			strURL += m_strDomain;
			strURL += ";";
		}

		if (m_strUserName != null && m_strUserName != "")
		{
			strURL += Encode(m_strUserName);
			if (m_strPassword != "")
			{
				strURL += ":";
				strURL += Encode(m_strPassword);
			}
			strURL += "@";
		}
		else if (m_strDomain != null && m_strDomain != "")
			strURL += "@";

		if (m_strHostName != null && m_strHostName != "")
		{
			if( URIUtils.protocolHasEncodedHostname(m_strProtocol) )
				strURL += Encode(m_strHostName);
			else
				strURL += m_strHostName;
			if (HasPort())
			{
				String strPort = String.format("%i", m_iPort);
				strURL += ":";
				strURL += strPort;
			}
			strURL += "/";
		}

		return strURL;
	}

	String GetRedacted() 
	{
		return GetWithoutUserDetails(true);
	}

	String GetRedacted( String path)
	{
		return new URL(path).GetRedacted();
	}

	boolean IsLocal() 
	{
		return (IsLocalHost() || (m_strProtocol != null && m_strProtocol.isEmpty()));
	}

	boolean IsLocalHost() 
	{
		return m_strHostName != null && (m_strHostName.equals("localhost") || m_strHostName.equals("127.0.0.1"));
	}

	boolean IsFileOnly( String url)
	{
		return url.indexOf("/\\") == -1;
	}

	boolean IsFullPath( String url)
	{
		if (url.length() > 0 && url.charAt(0) == '/') return true;     //   /foo/bar.ext
		if (url.indexOf("://") != -1) return true;                 //   foo://bar.ext
		if (url.length() > 1 && url.charAt(1) == ':') return true; //   c:\\foo\\bar\\bar.ext
		if (StringUtils.startsWith(url, "\\\\")) return true;    //   \\UNC\path\to\file
		return false;
	}

	public static String Decode( String strURLData)
	//modified to be more accomodating - if a non hex value follows a % take the characters directly and don't raise an error.
	// However % characters should really be escaped like any other non safe character (www.rfc-editor.org/rfc/rfc1738.txt)
	{
		
		if (strURLData == null)
			return null;
		
		String strResult = "";

		/* result will always be less than source */

		for ( int i = 0; i < strURLData.length(); ++i)
		{
			char kar = strURLData.charAt(i);
			if (kar == '+') strResult += ' ';
			else if (kar == '%')
			{
				if (i < strURLData.length() - 2)
				{
					String strTmp;
					strTmp = strURLData.substring(i + 1, (i + 1) + 2);
					int dec_num=-1;
					
//					sscanf(strTmp, "%x", (int)dec_num);
					dec_num = Integer.parseInt(strTmp, 16);
					
					if (dec_num<0 || dec_num>255)
						strResult += kar;
					else
					{
						strResult += (char)dec_num;
						i += 2;
					}
				}
				else
					strResult += kar;
			}
			else strResult += kar;
		}

		return strResult;
	}

	public static String Encode( String strURLData)
	{
		String strResult = "";


		for (int i = 0; i < strURLData.length(); ++i)
		{
			 char kar = strURLData.charAt(i);

			// Don't URL encode "-_.!()" according to RFC1738
			// TODO: Update it to "-_.~" after Gotham according to RFC3986
			if (CharUtils.isAsciiAlphanumeric(kar) || kar == '-' || kar == '.' || kar == '_' || kar == '!' || kar == '(' || kar == ')')
				strResult += kar;
			else
				strResult += String.format("%%%02x", (int) kar); // TODO: Change to "%%%02.2X" after Gotham
//			strResult += String.format("%%%02.2x", ( int)(( char)kar)); // TODO: Change to "%%%02.2X" after Gotham
		}

		return strResult;
	}

	public String TranslateProtocol( String prot)
	{
		if (prot.equals("shout") 
				|| prot.equals("daap")
				|| prot.equals("dav")
				|| prot.equals("tuxbox")
				|| prot.equals("rss"))
			return "http";

		if (prot.equals("davs"))
			return "https";

		return prot;
	}

	void GetOptions(Map<String, String> options) 
	{
		// TODO
//		CUrlOptions::UrlOptions optionsMap = m_options.GetOptions();
//		for (CUrlOptions::UrlOptions::_iterator option = optionsMap.begin(); option != optionsMap.end(); option++)
//			options[option->first] = option->second.asString();
	}

	boolean HasOption( String key) 
	{
		return m_options.HasOption(key);
	}

	boolean GetOption( String key, String value) 
	{
		//TODO implement
//		Object valueObj;
//		if (!m_options.GetOption(key, valueObj))
//			return false;
//
//		value = valueObj.asString();
		return true;
	}

	String GetOption( String key) 
	{
		String value = null;
		if (!GetOption(key, value))
			return "";

		return value;
	}

	void SetOption( String key,  String value)
	{
		m_options.AddOption(key, value);
		SetOptions(m_options.GetOptionsString(true));
	}

	void RemoveOption( String key)
	{
		m_options.RemoveOption(key);
		SetOptions(m_options.GetOptionsString(true));
	}

	void GetProtocolOptions(Map<String, String> options) 
	{
		// TODO 
//		CUrlOptions::UrlOptions optionsMap = m_protocolOptions.GetOptions();
//		for (CUrlOptions::UrlOptions::_iterator option = optionsMap.begin(); option != optionsMap.end(); option++)
//			options[option->first] = option->second.asString();
	}

	public boolean HasProtocolOption( String key) 
	{
		return m_protocolOptions.HasOption(key);
	}

	public boolean GetProtocolOption( String key, String value) 
	{
		// TODO implement
//		CVariant valueObj;
//		if (!m_protocolOptions.GetOption(key, valueObj))
//			return false;
//
//		value = valueObj.asString();
		return true;
	}

	public String GetProtocolOption(String key) 
	{
		String value = null;
		if (!GetProtocolOption(key, value))
			return "";

		return value;
	}


	public void RemoveProtocolOption( String key)
	{
		m_protocolOptions.RemoveOption(key);
		m_strProtocolOptions = m_protocolOptions.GetOptionsString(false);
	}

}
