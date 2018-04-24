package com.guohuai.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class CsvExport {
	/**
     * 导出
     * 
     * @param file csv文件(路径+文件名)，csv文件不存在会自动创建
     * @param dataList 数据
     * @return
     */
    public static boolean exportCsv(File file,List<String> header , List<List<String>> dataList){
        boolean isSucess=false;
        
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out,"GBK");
            bw =new BufferedWriter(osw);
            if(header!=null && !header.isEmpty()){
            	for(String heads:header){
            		bw.append(heads).append(",");
            	}
            	bw.append("\r");
            }
            if(dataList!=null && !dataList.isEmpty()){
            	for(int i=0;i<dataList.size();i++){
            		List<String> dates=dataList.get(i);
                		for(String data : dates){
                			if(data==null){
                				data="";
                			}else{
                				data=data.replace(",", "，");
                			}
                			bw.append(data).append(",");
                		}
                		bw.append("\r");
            	}
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        }
        
        return isSucess;
    }
     public static void deleteFiles(String filePath) {
    	  File file = new File(filePath);
    	  if (file.exists()) {
    	   File[] files = file.listFiles();
    	   for (int i = 0; i < files.length; i++) {
    	    if (files[i].isFile()) {
    	     files[i].delete();
    	    }
    	   }
    	  }
    	 }
}
