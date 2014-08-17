package com.orient.lib.xbmc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;

import com.orient.lib.xbmc.utils.XMLUtils;

public abstract class InfoTag {

	protected String xmlDateFormat = "yyyy-MM-dd";
	
	protected Map<String, String> xmlTagMapping = new HashMap<String, String>();

	public InfoTag() {
		super();

		initXmlTagMapping();
		reset();
	}
	
	/**
	 * Called after XML parsing is complete.
	 * @param element 
	 */
	protected void afterParseXML(Element element) {
		
	}

	/**
	 * Returns the variable type of a given member of the class.
	 * 
	 * @param fieldName
	 *            The field to check
	 * @return Field Type
	 */
	protected String getFieldType(String fieldName) throws NoSuchFieldException {
		Field f = getClass().getDeclaredField(fieldName);

		return f.getType().getSimpleName();
	}

	/**
	 * Get the type name of a generic type field.
	 * 
	 * @param fieldName
	 * @return
	 */
	protected String getGenericTypeName(String fieldName) {

		Field field;
		try {
			field = getClass().getDeclaredField(fieldName);
			Type type = field.getGenericType();

			if (type instanceof ParameterizedType) {

				ParameterizedType pType = (ParameterizedType) type;
				Type[] arr = pType.getActualTypeArguments();

				for (Type tp : arr) {
					Class<?> clzz = (Class<?>) tp;
					System.out.println(clzz.getName());

					return clzz.getSimpleName();
				}

			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getXmlDateFormat() {
		return xmlDateFormat;
	}

	public Map<String, String> getXmlTagMapping() {
		return xmlTagMapping;
	}

	protected abstract void initXmlTagMapping();

	/**
	 * Load information to a infotag from an XML element There are three types
	 * of tags supported:
	 * 
	 * <ol>
	 * <li>Single-value tags, such as &lt;title&gt;. These are set if available,
	 * else are left untouched.</li>
	 * <li>Additive tags, such as &lt;set&gt; or &lt;genre&gt;. These are
	 * appended to or replaced (if available) based on the value of the
	 * prioritise parameter. In addition, a clear attribute is available in the
	 * XML to clear the current value prior to appending.</li>
	 * <li>Image tags such as &lt;thumb&gt; and &lt;fanart&gt;. If the
	 * prioritise value is specified, any additional values are prepended to the
	 * existing values.</li>
	 * </ol>
	 * 
	 * @param element
	 *            the root XML element to parse.
	 * @param append
	 *            whether information should be added to the existing tag, or
	 *            whether it should be reset first.
	 * @param prioritise
	 *            if appending, whether additive tags should be prioritised
	 *            (i.e. replace or prepend) over existing values. Defaults to
	 *            false.
	 * 
	 * @see ParseNative
	 */
	public boolean loadXML(Element element, boolean append, boolean prioritise) {
		if (element == null)
			return false;

		if (!append)
			reset();

		try {
			parseXML(element, prioritise);
		} catch (IllegalAccessException | IllegalArgumentException
				| NoSuchFieldException | NullPointerException | ParseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Used by the parseXML method. Processes any incoming array items and adds
	 * them to their respective arrays.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 */
	@SuppressWarnings("unchecked")
	protected void onParseXMLArrayItem(String fieldName, Element valueEl) {
		String genericType = getGenericTypeName(fieldName);

		if (genericType.equals("String")) {

			ArrayList<String> arr;
			Field f;

			try {
				f = getClass().getDeclaredField(fieldName);
				f.setAccessible(true);

				if (f.get(this) == null) {
					arr = new ArrayList<String>();
				} else {
					arr = (ArrayList<String>) f.get(this);
				}

				String item = XMLUtils.getFirstChildValue(valueEl);

				if (item == null || item.isEmpty())
					return;
					
				arr.add((String) item);
				
				getClass().getDeclaredField(fieldName).set(this, arr);

			} catch (NoSuchFieldException | IllegalAccessException
					| IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Used by the parseXML method. Performs any further pre-processing on the
	 * incoming value if required.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 * @throws ParseException 
	 */
	protected Date onParseXMLItemDate(Element el) throws ParseException {
		String dateFormatAttr = XMLUtils.getAttribute(el, "format");
		String dateFormat = (dateFormatAttr != null) ? dateFormatAttr : xmlDateFormat;
		
		String dateStr = XMLUtils.getFirstChildValue(el);

		Date date = null;
		
		if (dateStr != null) {
			date = new SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateStr);
		}
		
		return date;
	}

	/**
	 * Used by the parseXML method. Performs any further pre-processing on the
	 * incoming value if required.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 */
	protected float onParseXMLItemFloat(Element el) {
		return XMLUtils.getFirstChildValue_float(el);
	}

	/**
	 * Used by the parseXML method. Performs any further pre-processing on the
	 * incoming value if required.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 */
	protected int onParseXMLItemInt(Element el) {
		return XMLUtils.getFirstChildValue_int(el);
	}

	/**
	 * Used by the parseXML method. Performs processing on any unrecognized 
	 * types i.e. Any type other than String, int, float, date, ArrayList
	 * 
	 * @param el
	 *            The element to extract the value from
	 */
	protected void onParseXMLItemOther(Element el) {
	}

	/**
	 * Used by the parseXML method. Performs any further pre-processing on the
	 * incoming value if required.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 */
	protected String onParseXMLItemString(Element el) {
		return XMLUtils.getFirstChildValue(el);
	}
	
	/**
	 * Parse our native XML format for info. See Load for a description of the
	 * available tag types.
	 * 
	 * @param element
	 *            the root XML element to parse.
	 * @param prioritise
	 *            whether additive tags should be replaced (or prepended) by the
	 *            content of the tags, or appended to.
	 * @throws IllegalAccessException
	 *             , IllegalArgumentException, NoSuchFieldException,
	 *             ParseException, NullPointerException
	 * @see Load
	 */
	protected void parseXML(Element element, boolean prioritise)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException, ParseException, NullPointerException {

		Element child = XMLUtils.getFirstChildElement(element);

		while (child != null) {

			String tag = child.getNodeName();

			String memberName;
			String fieldType;

			if (xmlTagMapping.containsKey(tag)) {

				memberName = xmlTagMapping.get(tag);
				fieldType = getFieldType(memberName);

				if (fieldType.equals("String")) {
					setField(memberName, onParseXMLItemString(child));
					
				} else if (fieldType.equals("int")) {
					setField(memberName, onParseXMLItemInt(child));
					
				} else if (fieldType.equals("float")) {
					setField(memberName, onParseXMLItemFloat(child));
					
				} else if (fieldType.equals("Date")) {
					setField(memberName, onParseXMLItemDate(child));
					
				} else if (fieldType.equals("ArrayList")) {
					onParseXMLArrayItem(memberName, child);
				} else {
					onParseXMLItemOther(child);
				}
			}

			child = XMLUtils.getNextSiblingElement(child);
		}

		// Post Processing
		afterParseXML(element);
	}
	
	public abstract void reset();
	
	/**
	 * Dynamically set a member of this class.
	 * 
	 * @param fieldName
	 *            The field to set
	 * @param value
	 *            The value to set
	 */
	protected void setField(String fieldName, Date value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).set(this, value);
	}

	/**
	 * Dynamically set a member of this class.
	 * 
	 * @param fieldName
	 *            The field to set
	 * @param value
	 *            The value to set
	 */
	protected void setField(String fieldName, float value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).setFloat(this, value);
	}

	/**
	 * Dynamically set a member of this class.
	 * 
	 * @param fieldName
	 *            The field to set
	 * @param value
	 *            The value to set
	 */
	protected void setField(String fieldName, int value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).setInt(this, value);
	}

	/**
	 * Dynamically set a member of this class.
	 * 
	 * @param fieldName
	 *            The field to set
	 * @param value
	 *            The value to set
	 */
	protected void setField(String fieldName, String value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).set(this, value);
	}

	public void setXmlDateFormat(String xmlDateFormat) {
		this.xmlDateFormat = xmlDateFormat;
	}

	public void setXmlTagMapping(Map<String, String> xmlTagMapping) {
		this.xmlTagMapping = xmlTagMapping;
	}

}