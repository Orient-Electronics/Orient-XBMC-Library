package orient.lib.xbmc.addons;

public class ScraperError extends Exception {
	
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