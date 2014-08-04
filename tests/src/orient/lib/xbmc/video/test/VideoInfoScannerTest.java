package orient.lib.xbmc.video.test;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import orient.lib.xbmc.FileItem;
import orient.lib.xbmc.Settings;
import orient.lib.xbmc.addons.Scraper;
import orient.lib.xbmc.video.VideoInfoScanner;

@RunWith(JUnitParamsRunner.class)
public class VideoInfoScannerTest {

	private String assetsPath;
	
	public VideoInfoScannerTest() {
		Settings settings = Settings.getInstance();
		assetsPath = settings.getTestAssetsDirPath();
	}
	
	@SuppressWarnings("unused")
	private Object[] nfoSearch() {
		return $(
				// Fix this test case with correct values
				$( false, assetsPath + "Testing Data\\Movies - Flat\\movie.nfo", assetsPath + "Testing Data\\Movies - Flat\\Battleship (2012)cd1\\somefile.avi" ),
				
				$( true, assetsPath + "Testing Data\\Movies - Flat\\movie.nfo", assetsPath + "Testing Data\\Movies - Flat\\Battleship (2012).avi" ),
				$( true, assetsPath + "Testing Data\\Movies - Flat\\movie.nfo", assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo", assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller].nfo", "stack://" + assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part1.avi , " + assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part2.avi" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd1.nfo", "stack://" + assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd1.mkv , " + assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd2.mkv" ),
				$( false, assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)\\somefilename.nfo", assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)cd1\\somefilename.avi" )
				);
	}
	
	@Test
	@Parameters(method = "nfoSearch")
	public void getNFOFile(boolean grabAny, String expectedResult, String path) {

		FileItem item = new FileItem(path, false);
		
		VideoInfoScanner v = new VideoInfoScanner();
		String actualResult = v.getNFOFile(item, grabAny);
		
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void retrieveInfoForMovie(){
		String filePath = assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part1.avi";
		FileItem fileItem = new FileItem(filePath, false);
		
		Scraper scraper = new Scraper("metadata.themoviedb.org");
		
		VideoInfoScanner scanner = new VideoInfoScanner();
		scanner.retrieveInfoForMovie(fileItem, true, scraper, true, null);
		
		fail();
	}
}
