package com.example.localadministrator.runtime;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhy on 2016/1/12.
 */
public class FIleUtils {
	public List<String> getImagePath() {
		// image list
		List<String> imagePathList = new ArrayList<String>();
		// get the path of the image file   File.separator(/)
		String state = Environment.getExternalStorageState();
		System.out.println(state);
		String filePath = "storage/emulated/0/DCIM/Camera";
		// get all the files in the path;
		File fileAll = new File(filePath);
		System.out.println(fileAll);
		File[] files = fileAll.listFiles();
		// push all the file into list,and filter the image file
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (checkIsImageFile(file.getPath())) {
				imagePathList.add(file.getPath());
			}
		}
		System.out.println(files.length);
		// return the image list
		return imagePathList;
	}

	/**
	 * check the extension name to get the images
	 * @param fName  file name
	 * @return
	 */
	//@SuppressLint("DefaultLocale")
	private boolean checkIsImageFile(String fName) {
		boolean isImageFile = false;
		// get the extension name;
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();
		if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
				|| FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
			isImageFile = true;
		} else {
			isImageFile = false;
		}
		return isImageFile;
	}
}
