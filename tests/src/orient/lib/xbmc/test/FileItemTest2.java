package orient.lib.xbmc.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
 

import orient.lib.xbmc.FileItem;

import junit.runner.Version;


@RunWith(value = Parameterized.class)
public class FileItemTest2 {

	private String file;
	private boolean use_folder;
	private String base;
	  
	
	
	public FileItemTest2(String file, boolean use_folder, String base) {
		super();
		this.file = file;
		this.use_folder = use_folder;
		this.base = base;
		
		System.out.println("JUnit version is: " + Version.id());
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{ "c:\\dir\\filename.avi", false, "c:\\dir\\filename.avi" },
				{ "c:\\dir\\filename.avi", true,  "c:\\dir\\" },
				{ "/dir/filename.avi", false, "/dir/filename.avi" },
				{ "/dir/filename.avi", true,  "/dir/" },
				{ "smb://somepath/file.avi", false, "smb://somepath/file.avi" },
				{ "smb://somepath/file.avi", true, "smb://somepath/" },
				{ "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi", false, "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi" },
				{ "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi", true,  "/path/to/movie_name/" },
				{ "/home/user/TV Shows/Dexter/S1/1x01.avi", false, "/home/user/TV Shows/Dexter/S1/1x01.avi" },
				{ "/home/user/TV Shows/Dexter/S1/1x01.avi", true, "/home/user/TV Shows/Dexter/S1/" },
				{ "rar://g%3a%5cmultimedia%5cmovies%5cSphere%2erar/Sphere.avi", true, "g:\\multimedia\\movies\\" },
				{ "/home/user/movies/movie_name/video_ts/VIDEO_TS.IFO", false, "/home/user/movies/movie_name/" },
				{ "/home/user/movies/movie_name/video_ts/VIDEO_TS.IFO", true, "/home/user/movies/movie_name/" },
				{ "/home/user/movies/movie_name/BDMV/index.bdmv", false, "/home/user/movies/movie_name/" },
				{ "/home/user/movies/movie_name/BDMV/index.bdmv", true, "/home/user/movies/movie_name/" }};
		return Arrays.asList(data);
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		FileItem f = new FileItem(base, use_folder);
		
		assertEquals(f.isFolder(), use_folder);
	}

}
