package orient.lib.xbmc.video.test;

import org.w3c.dom.Document;

import orient.lib.xbmc.utils.XMLUtils;
import orient.lib.xbmc.video.VideoInfoTag;
import junit.framework.TestCase;

public class VideoInfoTagTest extends TestCase {

	VideoInfoTag video = new VideoInfoTag();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	public void testLoadMovie() {
		
		String path = "C:\\Users\\Abdul Rehman\\Dropbox\\Documents\\Programming\\Android\\Workspace\\ScraperTest\\assets\\Tests";
		String fileName = "Battleship (2012) [SD] [PG-13] [voted 5.4] [Sci-Fi]part1.nfo";
		
		Document doc = XMLUtils.getDocument(path + "\\" + fileName);
		
		assertTrue(video.load(doc.getDocumentElement(), true, false));
	}
	
	public void testLoadShow() {
		
		String path = "C:\\Users\\Abdul Rehman\\Dropbox\\Documents\\Programming\\Android\\Workspace\\ScraperTest\\assets\\Tests";
		String fileName = "tvshow.nfo";
		
		Document doc = XMLUtils.getDocument(path + "\\" + fileName);
		
		assertTrue(video.load(doc.getDocumentElement(), true, false));
	}

}
