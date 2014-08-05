package orient.lib.xbmc.utils.test;

import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;

import orient.lib.xbmc.utils.URIUtils;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

@RunWith(ZohhakRunner.class)
public class URIUtilsTest {

	@TestWith({ 
		"C:\\xbmc-dev, system/library/video/inprogressshows.xml, C:\\xbmc-dev\\system\\library\\video\\inprogressshows.xml" ,
		"special://xbmc/system/library/video/tvshows/, index.xml, special://xbmc/system/library/video/tvshows/index.xml" ,
		"library://video/, playlists.xml, library://video/playlists.xml" ,
	})
	public void addFileToFolder(String basePath, String fullFilenNmeToAdd, String result) {
		assertEquals(result, URIUtils.addFileToFolder(basePath, fullFilenNmeToAdd));
	}
	
	@TestWith({ 
		"http://mirrors.xbmc.org/addons/helix/addons.xml|Encoding=gzip, http://mirrors.xbmc.org/addons/helix/|Encoding=gzip",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\cache\\cookies.dat, C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\cache\\",
		"special://masterprofile/library/video, special://masterprofile/library/",
		"C:/aaa/bbb/ccc/ddd/test.java, C:/aaa/bbb/ccc/ddd/",
		"http://www.foo.com/bar/voo.mar , http://www.foo.com/bar/" 
	})
	public void getDirectory(String url, String result) {
		assertEquals(result, URIUtils.getDirectory(url));
	}
	
	
	@TestWith({ 
		"xbmc/system/keymaps/appcommand.xml, appcommand.xml",
		"xbmc/system/keymaps/joystick.AppleRemote.xml, joystick.AppleRemote.xml",
		"joystick.AppleRemote.xml, joystick.AppleRemote.xml",
	})
	public void getFileName(String path, String result) {
		assertEquals(result, URIUtils.getFileName(path));
	}
	
	@TestWith({ 
		"library://video/movies/, library://video/",
		"library://video/, library://",
		"library://, null",
	})
	// TODO add stack
	// TODO add file path
	public void getParentPath(String path, String result) {
		assertEquals(result, URIUtils.getParentPath(path));
	}
	
	@TestWith({ 
		"special://home/, .rss, false",
		"special://home/, .img|.iso|.nrg, false",
		"sample.iso, .img|.iso|.nrg, true",
		"sample.iso, img|iso|nrg, true",
		"special://home/sample.iso, .img|.iso|.nrg, true",
	})
	public void hasExtension(String filename, String extensions, boolean result) {
		assertEquals(result, URIUtils.hasExtension(filename, extensions));
	}
	
	@TestWith({ 
		"rar://home/test, true",
		"rar://home/test.rar, true",
		"rar://home/test.abc, true",
		"rar://home/, true",
		"file://home/test, false",
		"file://home/test.rar, false",
		"file://home/test.abc, false",
		"file://home/, false",
	})
	public void isInRAR(String path, boolean result)
	{
		assertEquals(result, URIUtils.isInRAR(path));
	}

	@TestWith({ 
		"stack://home/test, true",
		"stack:foo, true",
		"stack:foo.bar, true",
		"stack://home/foo.bar, true",
		"file://home/test, false",
		"file://home/test.rar, false",
		"file://home/test.abc, false",
		"file://home/, false",
	})
	public void isStack(String path, boolean result)
	{
		assertEquals(result, URIUtils.isStack(path));
	}
	
	@TestWith({ 
		"foo.bar, foo",
		"stack://home/foo.bar, stack://home/foo",
		"file://home/test, file://home/test",
		"file://home/test.rar, file://home/test",
		"file://home/, file://home/",
	})
	public void removeExtension(String path, String result)
	{
		assertEquals(result, URIUtils.removeExtension(path));
	}
	
	@TestWith({
		"special://masterprofile/, special://masterprofile",
		"special://masterprofile/RssFeeds.xml, special://masterprofile/RssFeeds.xml",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\userdata\\, C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\userdata",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\userdata\\favourites.xml, C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\userdata\\favourites.xml",
	})
	public void removeSlashAtEnd(String path, String result)
	{
		assertEquals(result, URIUtils.removeSlashAtEnd(path));
	}
	
	@TestWith({ 
		"foo.bar, baz, foo.baz",
		"stack://home/foo.bar, baz, stack://home/foo.baz",
		"special://masterprofile/Thumbnails/5/5fa3d44e.jpg, .dds, special://masterprofile/Thumbnails/5/5fa3d44e.dds",
	})
	public void replaceExtension(String path, String newExtension, String result)
	{
		assertEquals(result, URIUtils.replaceExtension(path, newExtension));
	}
	
	@TestWith({ 
		"library://video/files.xml/, library://video/files.xml/, ",
		"stack://home/foo.bar, stack://home/, foo.bar",
		"c:file.avi, c:, file.avi"
	})
	public void split(String path, String folder, String file)
	{
		String response[] = URIUtils.split(path);
		
		boolean result = false;
		
		if (response[0].equals(folder) && response[1].equals(file))
			result = true;

		assertEquals(true, result);
	}
}