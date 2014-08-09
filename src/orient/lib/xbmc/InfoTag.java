package orient.lib.xbmc.video;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

public abstract class InfoTag {

	protected Map<String, String> xmlTagMapping = new HashMap<String, String>();

	public InfoTag() {
		super();
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
			parseNative(element, prioritise);
		} catch (IllegalAccessException | IllegalArgumentException
				| NoSuchFieldException | NullPointerException | ParseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
	protected abstract void parseNative(Element element, boolean prioritise)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException, ParseException, NullPointerException;

	public void reset() {

	}

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

}