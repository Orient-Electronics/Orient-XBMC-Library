package com.orient.lib.xbmc.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.orient.lib.xbmc.XBMC;

public class XMLUtils {

	/**
	 * Takes a node input and returns the first child that has a tag as
	 * specified the tag string.
	 * 
	 * @param node
	 *            The element to parse
	 * @param tag
	 *            Node name to find
	 * 
	 * @return Element | null
	 */
	public static Element getFirstChildElement(Node node, String tag) {

		if (node == null)
			return null;
		
		// search for node
		Node child = node.getFirstChild();
		while (child != null) {
			if ((child.getNodeType() == Node.ELEMENT_NODE)
					&& (child.getNodeName().equals(tag))) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}

		// not found
		return null;

	}

	/**
	 * Takes a node input and returns the first child Element.
	 * 
	 * @param node
	 *            The element to parse
	 * @return Element | null
	 */
	public static Element getFirstChildElement(Node node) {

		// search for node
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}

		// not found
		return null;

	}

	/**
	 * Returns value of the first child of a given element.
	 * 
	 * @param node
	 *            The element to parse
	 * @return String | null
	 */
	public static String getFirstChildValue(Node node) {
		Node el = node.getFirstChild();

		if (el == null)
			return null;

		return el.getNodeValue();
	}

	/**
	 * Returns value of the first child of a given element that matches the
	 * given node name.
	 * 
	 * @param node
	 *            The element to parse
	 * @param tag
	 *            Node name to find
	 * @return String | null
	 */
	public static String getFirstChildValue(Node node, String tag) {
		Element el = getFirstChildElement(node, tag);

		if (el == null)
			return null;

		return el.getNodeValue();
	}

	/**
	 * Takes a node and returns it's next sibling Element.
	 * 
	 * @param node
	 *            The node to parse.
	 * 
	 * @return Element | null
	 */
	public static Element getNextSiblingElement(Node node) {

		// search for node
		Node sibling = node.getNextSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) sibling;
			}
			sibling = sibling.getNextSibling();
		}

		// not found
		return null;

	}

	/**
	 * Takes a node and returns it's first sibling Element that has a given
	 * tag/node name.
	 * 
	 * @param node
	 *            The node to parse.
	 * @param tag
	 *            The node name to find.
	 * 
	 * @return Element | null
	 */
	public static Element getNextSiblingElement(Node node, String tag) {

		// search for node
		Node sibling = node.getNextSibling();
		while (sibling != null) {
			if ((sibling.getNodeType() == Node.ELEMENT_NODE)
					&& (sibling.getNodeName() == tag)) {
				return (Element) sibling;
			}
			sibling = sibling.getNextSibling();
		}

		// not found
		return null;

	}

	/**
	 * Returns an attribute's value of a given element.
	 * 
	 * @param element
	 *            The element to parse
	 * @param attributeName
	 *            The name of the attribute to check
	 * 
	 * @return String | null
	 */
	public static String getAttribute(Element element,
			String attributeName) {
		NamedNodeMap attrs = element.getAttributes();

		Node attrNode = attrs.getNamedItem(attributeName);
		String value = null;

		if (attrNode != null) {
			value = attrNode.getNodeValue();
		}

		return value;
	}

	/**
	 * Returns an attribute's value of a given element.
	 * 
	 * @param element
	 *            The element to parse
	 * @param attributeName
	 *            The name of the attribute to check
	 * @param defaultValue
	 *            Value to return if result is null
	 * 
	 * @return String | null
	 */
	public static String getAttribute(Element element,
			String attributeName, String defaultValue) {
		String result = getAttribute(element, attributeName);

		if (result == null)
			return defaultValue;

		return result;
	}

	/**
	 * Coverts XML node to string representation.
	 * 
	 * @param node
	 * @return String | null
	 */
	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
			return null;
		}
		return sw.toString();
	}

	/**
	 * Takes path of a XML file and converts it into a Document object.
	 * 
	 * @param path
	 *            Path to the XML file
	 * @return Document | null
	 */
	public static Document getDocument(String path) {
		return getDocumentFromString(FileUtils.getContents(path));

//		if (XBMC.getInstance().isAndroid()) {
//			return getDocumentFromString(FileUtils.getContents(path));
//		}
//		
//		File fXmlFile = new File(path);
//
//		if (!fXmlFile.exists())
//			return null;
//
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder;
//		Document doc = null;
//
//		try {
//			dBuilder = dbFactory.newDocumentBuilder();
//			doc = dBuilder.parse(fXmlFile);
//		} catch (SAXException | IOException | ParserConfigurationException e1) {
//			e1.printStackTrace();
//			return null;
//		}
//
//		return doc;
	}

	/**
	 * Takes an XML String and converts it into a Document Object.
	 * 
	 * @param xmlStr
	 * @return Document | null
	 */
	public static Document getDocumentFromString(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Returns value of the first child of a given element.
	 * 
	 * @param Element
	 *            The element to parse
	 * @return int | 0
	 */
	public static int getFirstChildValue_int(Element element) {
		int value = 0;

		String string = getFirstChildValue(element);

		if (string != null)
			value = Integer.parseInt(string);

		return value;
	}

	/**
	 * Returns value of the first child of a given element.
	 * 
	 * @param Element
	 *            The element to parse
	 * @return float | 0
	 */
	public static float getFirstChildValue_float(Element element) {
		float value = 0;

		String string = getFirstChildValue(element);

		if (string != null)
			value = Float.parseFloat(string);

		return value;
	}
	
	/**
	 * Returns an array of values filled with the values of all nodes.
	 * 
	 * @param Element
	 *            The element to parse
	 * @return ArrayList
	 */
	public static ArrayList<String> getChildrenValues(Element element) {
		ArrayList<String> array = new ArrayList<String>();
		
		Element item = getFirstChildElement(element);
		
		while (item != null) {
			array.add(item.getNodeValue());
			item = getNextSiblingElement(item);
		}
		
		return array;
	}
	
	/**
	 * Returns an array of values filled with the values of all nodes specified by
	 * a given node name.
	 * 
	 * @param Element
	 *            The element to parse
	 * @return ArrayList
	 */
	public static ArrayList<String> getChildrenValues(Element element, String tag) {
		ArrayList<String> array = new ArrayList<String>();
		
		Element item = getFirstChildElement(element, tag);
		
		while (item != null) {
			array.add(item.getNodeValue());
			item = getNextSiblingElement(item, tag);
		}
		
		return array;
	}
}
