package orient.lib.xbmc.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orient.lib.xbmc.addons.Scraper;

public class ScraperParser {

	private Scraper scraper;
	private String searchStringEncoding;
	private boolean isNoop;

	private String filename;
	private Document document;
	private Element rootElement;

	public static final int MAX_SCRAPER_BUFFERS = 20;
	public String[] params = new String[MAX_SCRAPER_BUFFERS];

	public ScraperParser() {
		rootElement = null;
		document = null;
		searchStringEncoding = "UTF-8";
		scraper = null;
		isNoop = true;
	}

	public boolean isNoop() { 
		return isNoop; 
	};
	
	public String getFilename() { 
		return filename; 
	}
	
	/**
	 * Append nodes of the given document to the current document.
	 * 
	 * @param doc The document to append
	 */
	public void appendDocument(Document doc) {
		Node node = XMLUtils.getFirstChildElement(doc.getDocumentElement());

		while (node != null) {
			Node newNode = document.importNode(node, true);
			rootElement.appendChild(newNode);
			node = node.getNextSibling();
		}
	}

	/*
	 * "this is a string !!!CLEAN!!!<i>with</i>!!!CLEAN!!! a tag" will be
	 * converted to "this is a string with a tag"
	 * 
	 * TODO Add FIXCHARS
	 */
	private String clean(String strDirty) {

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
		index1 = 0;
		token = "!!!FIXCHARS!!!";

		while ((index1 = strDirty.indexOf(token)) >= 0) {

			if ((index2 = strDirty.indexOf(token, index1 + token.length())) >= 0) {

				String before, after;

				before = strDirty.substring(0, index1);
				after = strDirty.substring(index2 + token.length());

				strBuffer = strDirty.substring(index1 + token.length(), index2);

				// trim
				StringEscapeUtils.unescapeXml(strBuffer);
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

				// encode
				strBuffer = orient.lib.xbmc.URL.Encode(strBuffer);

				//				try {
				//					strBuffer = URLEncoder.encode(strBuffer, "UTF-8");
				//				} catch (UnsupportedEncodingException e) {
				//					e.printStackTrace();
				//				}

				strDirty = before + strBuffer + after;
			} else
				break;

		}

		return strDirty;
	}

	public void clear() {
		rootElement = null;
		document = null;
		filename = "";
	}

	/**
	 * Its all in the name.
	 */
	private void clearBuffers() {
		for (int i = 0; i < MAX_SCRAPER_BUFFERS; ++i)
			params[i] = null;
	}

	public Element firstChildScraperElement(Node node) {
		Element child = XMLUtils.getFirstChildElement(node);

		while (child != null && !child.getNodeName().equals("RegExp")
				&& !child.getNodeName().equals("XSLT")) {
			child = XMLUtils.getNextSiblingElement(child);
		}

		return child;
	}

	/**
	 * Fills an array with defvalue, and invert the value of those items
	 * mentioned by attribute e.g. "1,3" will invert index 0 and 2. Basically
	 * its creating a mapping array.
	 */
	private boolean[] getBufferParams(String attribute, boolean defvalue) {

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

	public Scraper getScraper() {
		return scraper;
	} 

	public String getSearchStringEncoding() {
		return searchStringEncoding;
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
	private String insertToken(String str, int buf, String token) {

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
	 * Takes an file path of an XML file and calls LoadFromXML to initialize
	 * this object.
	 */
	public boolean load(String strXMLFile) {
		clear();

		document = XMLUtils.getDocument(strXMLFile);

		if (document == null)
			return false;

		filename = strXMLFile;
		return loadFromXML();
	}

	/**
	 * Parses m_document to initialize this object and set encoding settings.
	 */
	private boolean loadFromXML() {

		if (document == null)
			return false;

		rootElement = document.getDocumentElement();
		String strValue = rootElement.getTagName();

		if (strValue == "scraper") {
			NodeList children = rootElement.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {

				Node currentNode = children.item(i);

				String nodeName = currentNode.getNodeName();

				if (nodeName.equals("CreateSearchUrl")
						|| nodeName.equals("CreateArtistSearchUrl")
						|| nodeName.equals("CreateAlbumSearchUrl")) {

					isNoop = false;
					NamedNodeMap attrs = currentNode.getAttributes();
					String nodeEncoding = null;

					if (attrs.getNamedItem("SearchStringEncoding") != null)
						nodeEncoding = attrs.getNamedItem("SearchStringEncoding").toString();

					if (nodeEncoding == null || searchStringEncoding != nodeEncoding)
						searchStringEncoding = "UTF-8";
				}

			}

			return true;
		}

		document = null;
		rootElement = null;
		return false;
	}

//	/**
//	 * Purpose and working unknown yet.
//	 */
//	public void convertJSON(String string) {
//
//		Pattern pattern = Pattern.compile("\\\\u([0-f]{4})");
//		Matcher matcher = pattern.matcher(string);
//
//		int count = 0;
//		while (matcher.find()) {
//			count++;
//			System.out.println("found: " + count + " : " + matcher.start()
//					+ " - " + matcher.end());
//		}
//	}

	public Element nextSiblingScraperElement(Node node) {
		Element child = XMLUtils.getNextSiblingElement(node);
		
		while (child != null && !child.getNodeName().equals("RegExp")
				&& !child.getNodeName().equals("XSLT")) {
			child = XMLUtils.getNextSiblingElement(child);
		}

		return child;
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
	public String parse(String strTag, Scraper scraper) {

		this.scraper = scraper;

		Element pChildElement = XMLUtils.getFirstChildElement(rootElement, strTag);
		if (pChildElement == null) {
			return "";
		}

		String szDest = XMLUtils.getAttribute(pChildElement, "dest");
		int iResult = 1;

		if (szDest != null)
			iResult = Integer.parseInt(szDest);


		Element pChildStart = firstChildScraperElement(pChildElement);
		parseNext(pChildStart);

		String temp = params[iResult - 1];


		String szClearBuffers = XMLUtils.getAttribute(pChildElement, "clearbuffers");
		if (szClearBuffers == null || szClearBuffers == "no")
			clearBuffers();



		return temp;
	}

	/**
	 * Parse a given expression. Used on the <RegExp> element of the scraper.
	 *  <br />
	 *  This function does all the heavy lifting.
	 */
	private String parseExpression(String input, String dest, Element element,
			boolean bAppend) {

		//		String dest = ""; // return;

		String strOutput = XMLUtils.getAttribute(element, "output");		
		Element pExpression = XMLUtils.getFirstChildElement(element, "expression");

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

		strExpression = replaceBuffers(strExpression);
		strOutput = replaceBuffers(strOutput);

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
		boolean bClean[] = getBufferParams(XMLUtils.getAttribute(pExpression, "noclean"), true);
		boolean bTrim[] = getBufferParams(XMLUtils.getAttribute(pExpression, "trim"), false);
		boolean bFixChars[] = getBufferParams(XMLUtils.getAttribute(pExpression, "fixchars"), false);
		boolean bEncode[] = getBufferParams(XMLUtils.getAttribute(pExpression, "encode"), false);


		for (int iBuf = 0; iBuf < MAX_SCRAPER_BUFFERS; ++iBuf) {

			if (bClean[iBuf])
				strOutput = insertToken(strOutput, iBuf + 1, "!!!CLEAN!!!");

			if (bTrim[iBuf])
				strOutput = insertToken(strOutput, iBuf + 1, "!!!TRIM!!!");

			if (bFixChars[iBuf])
				strOutput = insertToken(strOutput, iBuf + 1, "!!!FIXCHARS!!!");

			if (bEncode[iBuf])
				strOutput = insertToken(strOutput, iBuf + 1, "!!!ENCODE!!!");
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
			params[iCompare - 1].toLowerCase(Locale.US);

		// regex
		String curInput = input;
		Matcher matcher = pattern.matcher(curInput);

		while (matcher.find()) {

			if (!bAppend) {			
				dest = "";
				bAppend = true;
			}
			else if (dest == null)
				dest = "";

			String strCurOutput = strOutput;

			// optional
			if (iOptional > -1) // check that required param is there
			{

				//				String temp = "\\" + iOptional;

				// TODO
			}

			// get replace string here
			String result = strCurOutput;

			for (int groupId = 1; groupId < matcher.groupCount() + 1; ++groupId) {

				String exp = "(\\\\" + groupId + ")";
				Pattern pattern2 = Pattern.compile(exp);
				Matcher matcher2 = pattern2.matcher(result);

				String replacement = matcher.group(groupId).replace("$", "\\$");

				//					if (matcher2.find())
				result = matcher2.replaceAll(replacement);
			}

			if (result.length() > 0)
			{
				String strResult = result;

				strResult = clean(strResult);
				strResult = replaceBuffers(strResult);

				if (iCompare > -1)
				{
					String strResultNoCase = strResult.toLowerCase(Locale.US);

					if (strResultNoCase.indexOf(params[iCompare-1]) >= 0)
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

	/**
	 * Parses a given element. This function is used recursively,
	 * to parse all (recognized) elements, children first.
	 */
	private void parseNext(Element element) {
		Element pReg = element;

		while (pReg != null) {

			// If there is another recognized child, parse it first
			Element pChildReg = firstChildScraperElement(pReg);

			if (pChildReg != null)
				parseNext(pChildReg);
			else {
				pChildReg = XMLUtils.getFirstChildElement(pReg, "clear");

				if (pChildReg != null)
					parseNext(pChildReg);
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
				strInput = replaceBuffers(strInput);
			} else
				strInput = params[0];

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

				if (scraper != null && scraper.hasSettings())
					strSetting = scraper.getSetting(szConditional);

				bExecute = bInverse != (strSetting.equals("true"));
			}

			if (bExecute) {
				if (iDest - 1 < MAX_SCRAPER_BUFFERS && iDest - 1 > -1) {
					if (pReg.getNodeName() == "XSLT")
						parseXSLT(strInput, params[iDest - 1], pReg, bAppend);
					else
						params[iDest - 1] = parseExpression(strInput, params[iDest - 1], pReg, bAppend);
				}
			}
			pReg = nextSiblingScraperElement(pReg);
		}
	}

	/** \brief Parse an 'XSLT' declaration from the scraper
	 * This allow us to transform an inbound XML document using XSLT
	 * to a different type of XML document, ready to be output direct
	 * to the album loaders or similar
	 * 
	 * @param input the input document
	 * @param dest the output destation for the conversion
	 * @param element the current XML element
	 * @param bAppend append or clear the buffer
	 * 
	 * TODO implement
	 */
	private void parseXSLT(String input, String dest, Element element,
			boolean bAppend) {
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
	private String replaceBuffers(String strDest) {

		// insert buffers
		for (int i = 0; i < MAX_SCRAPER_BUFFERS; i++) {

			// create strings like $$1, $$2
			// String oldStr = String.format("$$%1$s", i + 1);
			String oldStr = "$$" + (i + 1);

			if (params[i] != null)
				strDest = strDest.replace(oldStr, params[i]);
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
				strDest = strDest.replace(var, scraper.getSetting(name));
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

	public void setScraper(Scraper m_scraper) {
		this.scraper = m_scraper;
	}
}
