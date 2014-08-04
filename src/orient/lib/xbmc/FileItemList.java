package orient.lib.xbmc;

import java.util.ArrayList;

/**
 * Represents a list of files.
 * 
 * @author Abdul Rehman
 * @see FileItem
 */
public class FileItemList {
	
	private ArrayList<FileItem> items;

	public void add(FileItem item) {
		items.add(item);
	}

	public FileItem get(int index) {
		return items.get(index);
	}
	
	public ArrayList<FileItem> getItems() {
		return items;
	}
	
	public int size() {
		return items.size();
	}
	
	/** 
	 * By default we stack all items (files and folders) in a FileItemList.
	 * 
	 * @see stackFiles, stackFolders
	 */
	public void stack() {
		stack(true);
	}
	
	/** 
	 * By default we stack all items (files and folders) in a FileItemList.
	 * 
	 * @param stackFiles whether to stack all items or just collapse folders
	 * @see stackFiles, stackFolders
	 */
	public void stack(boolean stackFiles) {
		
	}
	
	/**
	 * stack files in a FileItemList
	 * TODO implement this
	 * 
	 * @see Stack
	 */
	@SuppressWarnings("unused")
	private void stackFiles() {
		
	}
}
