package com.orient.lib.xbmc.filesystem;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.filesystem.StackDirectory;

@RunWith(JUnitParamsRunner.class)
public class StackDirectoryTest {

	@SuppressWarnings("unused")
	private Object[] stackPaths() {
		return $(
				$( "stack://file1 , file2 , file3 , file4", "file1" ),
				$( "stack://c:folder/file1 , c:folder/file2 , c:folder/file3 , c:folder/file4", "c:folder/file1" ),
				$( "stack://c:folder/file1", "c:folder/file1" ),
				$( "c:folder/file1 , c:folder/file2", "c:folder/file1" ),
				$( "c:fol,,der/file1 , c:folder/file2", "c:fol,der/file1" ),
				$( "c:folder/fil,,e1 , c:folder/file2", "c:folder/fil,e1" ),
				$( "", "" )
				);
	}
	
	@Test
	@Parameters(method = "stackPaths")
	public void getFirstStackedFile(String path, String result) {
		assertEquals(result, StackDirectory.getFirstStackedFile(path));
	}
	
	@Test
	public void getStackedTitlePath() {
		String path = "stack://C:\\Users\\Abdul Rehman\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part1.avi , C:\\Users\\Abdul Rehman\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part2.avi";
		
		String result = StackDirectory.getStackedTitlePath(path);
		
		assertEquals("C:\\Users\\Abdul Rehman\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller].avi", result);
	}
}
