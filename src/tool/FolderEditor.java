package tool;

import java.io.File;

public class FolderEditor {
	public static void deleteDirectory(String directoryToBeDeleted) {
		File file = new File(directoryToBeDeleted);      
	    String[] myFiles;    
	    if(file.isDirectory()){
	    	myFiles = file.list();
            for (int i=0; i<myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]); 
                myFile.delete();
            }
	    }
	}
}

