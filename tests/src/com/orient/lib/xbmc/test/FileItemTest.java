package com.orient.lib.xbmc.test;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.FileItem;

@RunWith(JUnitParamsRunner.class)
public class FileItemTest {

	@SuppressWarnings("unused")
	private Object[] baseMovies() {
		return $(
				$( "c:\\dir\\filename.avi", false, "c:\\dir\\filename.avi" ),
				$( "c:\\dir\\filename.avi", true,  "c:\\dir\\" ),
				$( "/dir/filename.avi", false, "/dir/filename.avi" ),
				$( "/dir/filename.avi", true,  "/dir/" ),
				$( "smb://somepath/file.avi", false, "smb://somepath/file.avi" ),
				$( "smb://somepath/file.avi", true, "smb://somepath/" ),
				$( "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi", false, "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi" ),
				$( "stack:///path/to/movie_name/cd1/some_file1.avi , /path/to/movie_name/cd2/some_file2.avi", true,  "/path/to/movie_name/" ),
				$( "/home/user/TV Shows/Dexter/S1/1x01.avi", false, "/home/user/TV Shows/Dexter/S1/1x01.avi" ),
				$( "/home/user/TV Shows/Dexter/S1/1x01.avi", true, "/home/user/TV Shows/Dexter/S1/" ),
				$( "rar://g%3a%5cmultimedia%5cmovies%5cSphere%2erar/Sphere.avi", true, "g:\\multimedia\\movies\\" ),
				$( "/home/user/movies/movie_name/video_ts/VIDEO_TS.IFO", false, "/home/user/movies/movie_name/" ),
				$( "/home/user/movies/movie_name/video_ts/VIDEO_TS.IFO", true, "/home/user/movies/movie_name/" ),
				$( "/home/user/movies/movie_name/BDMV/index.bdmv", false, "/home/user/movies/movie_name/" ),
				$( "/home/user/movies/movie_name/BDMV/index.bdmv", true, "/home/user/movies/movie_name/" )
				);
	}
	
	@Test
	@Parameters({
		"special://home/userdata, application/octet-stream",
		"file.zip, application/zip",
		"special://home/userdata/file.json, application/json"
		})
	public void fillInMimeType(String path, String result) throws Exception {
		FileItem f = new FileItem(path, true);
		
		// Call private method
//		Method method = f.getClass().getDeclaredMethod("fillInMimeType");
//		method.setAccessible(true);
//		method.invoke(f);
		
		// Get field value to check
		Field field = f.getClass().getDeclaredField("mimetype");
		field.setAccessible(true);
		String value = (String) field.get(f);

		assertEquals(result, value);
	}
	
	@Test
	@Parameters({"index.bdmv, true", "index.bdm, false"})
	public void isBDFile(String path, boolean result) {
		FileItem f = new FileItem(path, true);
		assertEquals(result, f.isBDFile());
	}
	
	@Test
	@Parameters({"file.img, true", "file.zip, false"})
	public void isDVDImage(String path, boolean result) {
		FileItem f = new FileItem(path, true);
		assertEquals(result, f.isDVDImage());
	}
	
	@Test
	@Parameters(method = "baseMovies")
	public void isFolder(String file, boolean use_folder, String base) throws Exception {
		FileItem f = new FileItem(file, use_folder);
		assertEquals(use_folder, f.isFolder());
	}
	
	@Test
	@Parameters({"index.nfo, true", "index.bdm, false"})
	public void isNFO(String path, boolean result) {
		FileItem f = new FileItem(path, true);
		assertEquals(result, f.isNFO());
	}
	
	@Test
	@Parameters({
		"index.nfo, false", 
		"file.img, false",
		"index.bdmv, true",
		"video_ts.ifo, true",
		"index.bdm, false"})
	public void isOpticalMediaFile(String path, boolean result) {
		FileItem f = new FileItem(path, true);
		assertEquals(result, f.isOpticalMediaFile());
	}
	
	@Test
	@Parameters({
		"video.mpeg, true", 
		"video.jpeg, false",
		"video.ogg, true",
		"video.3gp, true",
		"index.bdm, false"
		})
	public void isVideo(String path, boolean result) {
		FileItem f = new FileItem(path, true);
		assertEquals(result, f.isVideo());
	}
}