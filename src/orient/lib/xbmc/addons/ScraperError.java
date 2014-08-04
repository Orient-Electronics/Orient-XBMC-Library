package orient.lib.xbmc.addons;

public class ScraperError extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Parameterless Constructor
	public ScraperError() {
	}

	// Constructor that accepts a message
	public ScraperError(String message) {
		super(message);
	}
	
	// Constructor that accepts a message
	public ScraperError(String title, String message) {
		super(title + ": " + message);
	}
}