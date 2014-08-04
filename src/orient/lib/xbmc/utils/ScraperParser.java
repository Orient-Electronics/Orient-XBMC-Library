package orient.lib.xbmc.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orient.lib.xbmc.addons.Scraper;


public class ScraperParser {

	private Scraper m_scraper;
	private String m_SearchStringEncoding;
	private boolean m_isNoop;

	private String m_strFile;
	private Document m_document;
	private Element m_pRootElement;

	public static final int MAX_SCRAPER_BUFFERS = 20;
	public String[] m_param = new String[MAX_SCRAPER_BUFFERS];

	public ScraperParser() {
		m_SearchStringEncoding = "UTF-8";
		m_scraper = null;
		m_isNoop = true;
	}

	public void Clear() {
		m_strFile = "";
	}

	/**
	 * Takes an file path of an XML file and calls LoadFromXML to initialize
	 * this object.
	 */
	public boolean Load(String strXMLFile) {
		Clear();

		m_document = XMLUtils.getDocument(strXMLFile);
		
		if (m_document == null)
			return false;
		
		m_strFile = strXMLFile;
		return LoadFromXML();
	}

	/**
	 * Parses m_document to initialize this object and set encoding settings.
	 */
	public boolean LoadFromXML() {

		if (m_document == null)
			return false;

		m_pRootElement = m_document.getDocumentElement();
		String strValue = m_pRootElement.getTagName();

		if (strValue == "scraper") {
			NodeList children = m_pRootElement.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {

				Node currentNode = children.item(i);

				String nodeName = currentNode.getNodeName();

				if (nodeName.equals("CreateSearchUrl")
						|| nodeName.equals("CreateArtistSearchUrl")
						|| nodeName.equals("CreateAlbumSearchUrl")) {

					m_isNoop = false;
					NamedNodeMap attrs = currentNode.getAttributes();
					String nodeEncoding = null;

					if (attrs.getNamedItem("SearchStringEncoding") != null)
						nodeEncoding = attrs.getNamedItem("SearchStringEncoding").toString();

					if (nodeEncoding == null || m_SearchStringEncoding != nodeEncoding)
						m_SearchStringEncoding = "UTF-8";
				}

			}

			return true;
		}

		m_document = null;
		m_pRootElement = null;
		return false;
	}

	/**
	 * Formats a string with certain tokens and replaces them with their
	 * respected values. The tokens can be: <br /><br />
	 * 
	 * 1. Buffer e.g. $$1 <br />
	 * 2. Setting e.g. $INFO[foo] <br />
	 * 3. Localization String e.g. $LOCALIZE[foo] <br />
	 * 
	 * @param strDest The string to process.
	 * @return processed string
	 */
	public String ReplaceBuffers(String strDest) {

		// insert buffers
		for (int i = MAX_SCRAPER_BUFFERS - 1; i >= 0; i--) {

			// create strings like $$1, $$2
			// String oldStr = String.format("$$%1$s", i + 1);
			String oldStr = "$$" + (i + 1);

			if (m_param[i] != null)
				strDest = strDest.replace(oldStr, m_param[i]);
			else
				strDest = strDest.replace(oldStr, ""); // put empty string if param is buffer is null
		}

		// insert settings
		int indexStart = strDest.indexOf("$INFO[");
		while (indexStart >= 0) {
			int indexEnd = strDest.indexOf("]", indexStart);

			if (indexEnd < 0)
				continue;

			String name = strDest.substring(indexStart + 6, indexEnd);
			String var = "$INFO[" + name + "]";

			try {
				strDest = strDest.replace(var, m_scraper.getSetting(name));
			}
			catch (NullPointerException e) {
				// if setting not found
				strDest = strDest.replace(var, "");
			}

			indexStart = strDest.indexOf("$INFO[");
		}

		// insert localize strings
		indexStart = strDest.indexOf("$LOCALIZE[");
		while (indexStart >= 0) {
			int indexEnd = strDest.indexOf("]", indexStart);

			if (indexEnd < 0)
				continue;

			String name = strDest.substring(indexStart + 6, indexEnd);
			String var = "$LOCALIZE[" + name + "]";

			// TODO fetch actual string
			strDest = strDest.replace(var, name);

			indexStart = strDest.indexOf("$LOCALIZE[");
		}

		// Other fixes
		strDest = strDest.replace("\\n", "\n");

		return strDest;
	}
	
//	private String _getElementAttribute(Element element, String attributeName) {
//		NamedNodeMap attrs = element.getAttributes();
//
//		Node attrNode = attrs.getNamedItem(attributeName);
//		String value = null;
//
//		if (attrNode != null) {
//			value = attrNode.getNodeValue();
//		}
//		
//		return value;
//	}

	
	/**
	 * Parse a given expression. Used on the <RegExp> element of the scraper.
	 *  <br />
	 *  This function does all the heavy lifting.
	 */
	public String ParseExpression(String input, String dest, Element element,
			boolean bAppend) {
		
//		String dest = ""; // return;

		String strOutput = XMLUtils.getAttribute(element, "output");		
		Element pExpression = FirstChildElement(element, "expression");

		if (pExpression == null)
			return dest;

		// Case Sensitive
		boolean bInsensitive = true;
		String sensitive = XMLUtils.getAttribute(element, "cs");

		if (sensitive != null)
			if (sensitive == "yes")
				bInsensitive = false; // match case sensitive

		// UTF-8
//		String strUtf8 = XMLHelper.getAttribute(element, "	");

		// Expression
		String strExpression;

		if (pExpression.getFirstChild() != null)
			strExpression = pExpression.getFirstChild().getNodeValue();
		else
			strExpression = "(.*)";

		strExpression = ReplaceBuffers(strExpression);
		strOutput = ReplaceBuffers(strOutput);

		// Regex
		Pattern pattern;

		if (bInsensitive)
			pattern = Pattern.compile(strExpression, Pattern.CASE_INSENSITIVE);
		else
			pattern = Pattern.compile(strExpression);

		if (pattern == null)
			return dest;

		// Cleaning

		boolean bRepeat = false;
		String szRepeat = XMLUtils.getAttribute(pExpression, "repeat");

		if (szRepeat != null && szRepeat.equals("yes")) {
			bRepeat = true;
		}

		String szClear = XMLUtils.getAttribute(pExpression, "clear");

		if (szClear != null && szClear.equals("yes")) {
			dest = ""; // clear no matter if regexp fails
		}

		// Buffers
		boolean bClean[] = GetBufferParams(XMLUtils.getAttribute(pExpression, "noclean"), true);
		boolean bTrim[] = GetBufferParams(XMLUtils.getAttribute(pExpression, "trim"), false);
		boolean bFixChars[] = GetBufferParams(XMLUtils.getAttribute(pExpression, "fixchars"), false);
		boolean bEncode[] = GetBufferParams(XMLUtils.getAttribute(pExpression, "encode"), false);


		for (int iBuf = 0; iBuf < MAX_SCRAPER_BUFFERS; ++iBuf) {

			if (bClean[iBuf])
				strOutput = InsertToken(strOutput, iBuf + 1, "!!!CLEAN!!!");

			if (bTrim[iBuf])
				strOutput = InsertToken(strOutput, iBuf + 1, "!!!TRIM!!!");

			if (bFixChars[iBuf])
				strOutput = InsertToken(strOutput, iBuf + 1, "!!!FIXCHARS!!!");

			if (bEncode[iBuf])
				strOutput = InsertToken(strOutput, iBuf + 1, "!!!ENCODE!!!");
		}

		// optional
		int iOptional = -1;
		String optional = XMLUtils.getAttribute(element, "optional");

		if (optional != null)
			iOptional = Integer.parseInt(optional);

		// compare
		int iCompare = -1;
		String compare = XMLUtils.getAttribute(element, "compare");

		if (optional != null)
			iCompare = Integer.parseInt(compare);

		if (iCompare > -1)
			m_param[iCompare - 1].toLowerCase(Locale.US);

		// regex
		String curInput = input;
		Matcher matcher = pattern.matcher(curInput);

//		if (!matcher.find())
//			return dest;
//		
//		int i = matcher.start();
//		while (i > -1 && (i < (int)curInput.length() || curInput.length() == 0)) {
		while (matcher.find()) {

			if (!bAppend) {			
				dest = "";
				bAppend = true;
			}

			String strCurOutput = strOutput;
			
			// optional
			if (iOptional > -1) // check that required param is there
		      {
				
//				String temp = "\\" + iOptional;

		        // TODO
		      }
			
			
			
			// Next? 
			
			/*
			// nasty hack #1 - & means \0 in a replace string
			strCurOutput = strCurOutput.replace("&","!!!AMPAMP!!!");
			
			int end = matcher.end() - 1;
			
			if (end > strCurOutput.length())
				end = strCurOutput.length();
			
			String result = strCurOutput.substring(matcher.start(), end);
			*/
			
			// get replace string here
			String result = strCurOutput;
			
//			matcher.reset();
			
//			Pattern _pattern = pattern = Pattern.compile(strExpression);
//			Matcher _matcher = _pattern.matcher(curInput);

//			if (_matcher.find()) {
				for (int groupId = 1; groupId < matcher.groupCount() + 1; ++groupId) {

					String exp = "(\\\\" + groupId + ")";
					Pattern pattern2 = Pattern.compile(exp);
					Matcher matcher2 = pattern2.matcher(result);

					String replacement = matcher.group(groupId).replace("$", "\\$");
					
//					if (matcher2.find())
						result = matcher2.replaceAll(replacement);
				}
//			}
					
			if (result.length() > 0)
			{
				String strResult = result;
//				result = result.replace("!!!AMPAMP!!!","&");

				strResult = Clean(strResult);
				strResult = ReplaceBuffers(strResult);
				
				if (iCompare > -1)
				{
					String strResultNoCase = strResult.toLowerCase(Locale.US);

					if (strResultNoCase.indexOf(m_param[iCompare-1]) >= 0)
						dest += strResult;
				}
				else
					dest += strResult;
			}
			
			if (!bRepeat)
				break;
		}
		
		return dest;

	}

	// TODO implement
	public void ParseXSLT(String input, String dest, Element element,
			boolean bAppend) {

	}

	public Element FirstChildScraperElement(Node node) {

		// search for node
		Node child = node.getFirstChild();
		while (child != null) {
			if ((child.getNodeType() == Node.ELEMENT_NODE)
					&& (child.getNodeName() == "RegExp" || child.getNodeName() == "XSLT")) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}

		// not found
		return null;

	}

	public Element FirstChildElement(Node node, String tag) {

		// search for node
		Node child = node.getFirstChild();
		while (child != null) {
			if ((child.getNodeType() == Node.ELEMENT_NODE)
					&& (child.getNodeName() == tag)) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}

		// not found
		return null;

	} 

	public Element NextSiblingScraperElement(Node node) {

		// search for node
		Node sibling = node.getNextSibling();
		while (sibling != null) {
			if ((sibling.getNodeType() == Node.ELEMENT_NODE)
					&& (sibling.getNodeName() == "RegExp" || sibling
							.getNodeName() == "XSLT")) {
				return (Element) sibling;
			}
			sibling = sibling.getNextSibling();
		}

		// not found
		return null;

	} // getNextSiblingElement(Node):Element

	/**
	 * Parses a given element. This function is used recursively,
	 * to parse all (recognized) elements, children first.
	 */
	public void ParseNext(Element element) {
		Element pReg = element;

		while (pReg != null) {

			// If there is another recognized child, parse it first
			Element pChildReg = FirstChildScraperElement(pReg);

			if (pChildReg != null)
				ParseNext(pChildReg);
			else {
				pChildReg = FirstChildElement(pReg, "clear");

				if (pChildReg != null)
					ParseNext(pChildReg);
			}

			// Process tag Element attributes

			// dest
			String szDest = XMLUtils.getAttribute(pReg, "dest");
			int iDest = 1;
			boolean bAppend = false;

			if (szDest != null) {
				// Check for plus sign i.e. dest="5+"
				if (szDest.charAt(szDest.length() - 1) == '+') {
					bAppend = true;
					szDest = szDest.substring(0, szDest.length() - 1);
				}

				iDest = Integer.parseInt(szDest);
			}

			// input
			String szInput = XMLUtils.getAttribute(pReg, "input");
			String strInput = null;
			
			if (szInput != null) {
				strInput = szInput;
				strInput = ReplaceBuffers(strInput);
			} else
				strInput = m_param[0];

			// conditional
			String szConditional = XMLUtils.getAttribute(pReg, "conditional");
			
			boolean bExecute = true;

			if (szConditional != null) {
				boolean bInverse = false;
				if (szConditional.charAt(0) == '!') {
					bInverse = true;
					szConditional = szConditional.substring(1); // remove '!'
				}

				// TODO implemented but untested, may cause breakage
				 String strSetting = "";
				
				 if (m_scraper != null && m_scraper.hasSettings())
					 strSetting = m_scraper.getSetting(szConditional);
				
				 bExecute = bInverse != (strSetting.equals("true"));
			}

			if (bExecute) {
				if (iDest - 1 < MAX_SCRAPER_BUFFERS && iDest - 1 > -1) {
					if (pReg.getNodeName() == "XSLT")
						ParseXSLT(strInput, m_param[iDest - 1], pReg, bAppend);
					else
						m_param[iDest - 1] = ParseExpression(strInput, m_param[iDest - 1], pReg, bAppend);
				}
			}
			pReg = NextSiblingScraperElement(pReg);
		}
	}

	/**
	 * Takes a function/tag name i.e. CreateSearchUrl performs that action
	 * as specified by the corresponding scrapper xml file and returns the 
	 * result. <br /> <br />
	 * 
	 * Pre-requisites: <br />
	 * It requires scrapper xml file to be loaded using the Load method. <br /> <br />
	 * 
	 * CreateSearchUrl <br />
	 * m_param[0] = file name (cleaned) <br />
	 * m_param[1] year (if available) <br />
	 * */
	public String Parse(String strTag, Scraper scraper) {

		m_scraper = scraper;
		
		Element pChildElement = XMLUtils.getFirstChildElement(m_pRootElement, strTag);
		if (pChildElement == null) {
			return "";
		}

		String szDest = XMLUtils.getAttribute(pChildElement, "dest");
		int iResult = 1;

		if (szDest != null)
			iResult = Integer.parseInt(szDest);
		

		Element pChildStart = FirstChildScraperElement(pChildElement);
		ParseNext(pChildStart);

		String temp = m_param[iResult - 1];


		String szClearBuffers = XMLUtils.getAttribute(pChildElement, "clearbuffers");
		if (szClearBuffers == null || szClearBuffers == "no")
			ClearBuffers();

		return temp;
	}

	/*
	 * "this is a string !!!CLEAN!!!<i>with</i>!!!CLEAN!!! a tag" will be
	 * converted to "this is a string with a tag"
	 * 
	 * TODO Add FIXCHARS
	 */
	public String Clean(String strDirty) {

		int index1, index2;
		String token, strBuffer;

		// Clean
		index1 = 0;
		token = "!!!CLEAN!!!";

		while ((index1 = strDirty.indexOf(token)) >= 0) {

			if ((index2 = strDirty.indexOf(token, index1 + token.length())) >= 0) {

				String before, after;

				before = strDirty.substring(0, index1);
				after = strDirty.substring(index2 + token.length());

				strBuffer = strDirty.substring(index1 + token.length(), index2);

				// remove tags
				strBuffer = strBuffer.replaceAll("\\<.*?>", "").trim();

				strDirty = before + strBuffer + after;
			} else
				break;

		}

		// Trim
		index1 = 0;
		token = "!!!TRIM!!!";

		while ((index1 = strDirty.indexOf(token)) >= 0) {

			if ((index2 = strDirty.indexOf(token, index1 + token.length())) >= 0) {

				String before, after;

				before = strDirty.substring(0, index1);
				after = strDirty.substring(index2 + token.length());

				strBuffer = strDirty.substring(index1 + token.length(), index2);

				// trim
				strBuffer = strBuffer.trim();

				strDirty = before + strBuffer + after;
			} else
				break;
		}

		// Fixchars
		// ?? not sure what this is doing!!!
		// its only trimming right now
		index1 = 0;
		token = "!!!FIXCHARS!!!";

		while ((index1 = strDirty.indexOf(token)) >= 0) {

			if ((index2 = strDirty.indexOf(token, index1 + token.length())) >= 0) {

				String before, after;

				before = strDirty.substring(0, index1);
				after = strDirty.substring(index2 + token.length());

				strBuffer = strDirty.substring(index1 + token.length(), index2);

				// trim
				strBuffer = strBuffer.trim();

				strDirty = before + strBuffer + after;
			} else
				break;
		}

		// Encode
		index1 = 0;
		token = "!!!ENCODE!!!";

		while ((index1 = strDirty.indexOf(token)) >= 0) {

			if ((index2 = strDirty.indexOf(token, index1 + token.length())) >= 0) {

				String before, after;

				before = strDirty.substring(0, index1);
				after = strDirty.substring(index2 + token.length());

				strBuffer = strDirty.substring(index1 + token.length(), index2);

				// trim
				try {
					strBuffer = URLEncoder.encode(strBuffer, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				strDirty = before + strBuffer + after;
			} else
				break;

		}

		return strDirty;
	}

	/**
	 * Purpose and working unknown yet.
	 */
	public void ConvertJSON(String string) {

		Pattern pattern = Pattern.compile("\\\\u([0-f]{4})");
		Matcher matcher = pattern.matcher(string);

		int count = 0;
		while (matcher.find()) {
			count++;
			System.out.println("found: " + count + " : " + matcher.start()
					+ " - " + matcher.end());
		}
	}

	/**
	 * Its all in the name.
	 */
	public void ClearBuffers() {
		for (int i = 0; i < MAX_SCRAPER_BUFFERS; ++i)
			m_param[i] = null;
	}

	/**
	 * Fills an array with defvalue, and invert the value of those items
	 * mentioned by attribute e.g. "1,3" will invert index 0 and 2. Basically
	 * its creating a mapping array.
	 */
	public boolean[] GetBufferParams(String attribute, boolean defvalue) {

		// Initializing result array with defvalue in all indexes
		boolean[] result = new boolean[MAX_SCRAPER_BUFFERS];
		Arrays.fill(result, defvalue);

		// Split attribute string to get array indexes
		// Invert the value of those indexes
		if (attribute != null) {
			String[] tokens = attribute.split(",");

			for (int i = 0; i < tokens.length; ++i) {
				int index = Integer.parseInt(tokens[i]) - 1;

				if (index < MAX_SCRAPER_BUFFERS)
					result[index] = !defvalue;
			}
		}

		return result;
	}

	/**
	 * Adds padding to buffer variable in the given string.
	 * 
	 * Example: <br />
	 * 
	 * - strOutput = "foo\\5bar" where \ is escaped, so the string is parsed as "foo\5bar" <br />
	 * - buf = 5  <br />
	 * - token = $$$
	 *  <br /> <br />
	 *  
	 * so the result will be: "foo$$$\5$$$bar"
	 * 
	 * @param str The string to modify
	 * @param buf The buffer variable to pad
	 * @param token The padding string
	 */
	public String InsertToken(String str, int buf, String token) {

		String temp = String.format("\\%d", buf);

		int indexStart = str.indexOf(temp, 0);

		while (indexStart >= 0) {

			str = str.substring(0, indexStart) + token
					+ str.substring(indexStart, str.length());

			indexStart = indexStart + token.length() + temp.length();
			str = str.substring(0, indexStart) + token
					+ str.substring(indexStart, str.length());

			indexStart = str.indexOf(temp, indexStart);
		}

		return str;
	}

	/**
	 * Append nodes of the given document to the current document.
	 * 
	 * @param doc The document to append
	 */
	public void AppendDocument(Document doc) {
		Node node = XMLUtils.getFirstChildElement(doc.getDocumentElement());
		
		while (node != null) {
			Node newNode = m_document.importNode(node, true);
			m_pRootElement.appendChild(newNode);
			node = node.getNextSibling();
		}
	}
	
	public String GetSearchStringEncoding() {
		return m_SearchStringEncoding;
	}

	public Scraper getScraper() {
		return m_scraper;
	}

	public void setScraper(Scraper m_scraper) {
		this.m_scraper = m_scraper;
	}
	
	

}