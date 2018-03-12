package com.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CheckContentHashOfInputFile {
	
	Logger LOGGER  = LoggerFactory.getLogger(CheckContentHashOfInputFile.class);
	
	String fileName;
	String hashedName;
	String fullpath;
	String environemnt;

	/*Retrieving the checksum for the input file, to avoid
	 * duplicate processing.*/
	public void getContent(String fullpath, String name) throws NoSuchAlgorithmException, IOException
	{
		LOGGER.info("Inside Checksum Method");
		MessageDigest md = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(fullpath);
		try
		{
			this.fileName = name;
		    this. fullpath = fullpath;
		    byte[] dataBytes = new byte[1024];

		    int nread = 0;

		    while ((nread = fis.read(dataBytes)) != -1) {
		      md.update(dataBytes, 0, nread);
		    };

		    byte[] mdbytes = md.digest();

		    //convert the byte to hex format
		    StringBuffer sb = new StringBuffer("");
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    
		    this.hashedName = sb.toString();

		}
		finally {
		  if (fis!=null) {
		        fis.close();
		    }
		}   	    
	    LOGGER.info("Hashed Name" + hashedName);
	}
	
	/*Writing the FileName and HashedKey for the file.*/
	public void SetFile(String environment)
	{
		LOGGER.info("Writing the csv to the environment.");
		this.environemnt = environment;
		try {
			FileWriter result = new FileWriter(new File(environment, "FileHashDetails"  + ".csv"));
			result.append(hashedName);
			result.append(",");
			result.append(fileName);
			result.flush();
			result.close();
		} catch (IOException e) {
			LOGGER.error("Writing the csv to the environment :" + e.getMessage());
		}
	}
	
	/*Retrieving the hash details from csv file and then checking if it already exists in csv.*/
	public String ReadCSV()
	{
		LOGGER.info("Reading the csv from the environment to check if the hashedKey exists."
				+ "Inside ReadCSV file");
		
		BufferedReader br = null;
		try {
			String hashFile = this.environemnt + File.separator +"FileHashDetails.csv";
			if(System.getProperty("os.name").startsWith("Windows"))
			{
				hashFile = hashFile.replace("\\", "//");
			}	
			br = new BufferedReader(new FileReader(hashFile));
		} catch (FileNotFoundException e1) {
			LOGGER.info("Read CSV Exception" + e1.getMessage());
		}
	    String line =  null;
	    HashMap<String,String> map = new HashMap<String, String>();

	    try {
			while((line=br.readLine())!=null){
			    String str[] = line.split(",");
			        map.put(str[0], str[1]);
			    }
		} catch (IOException e) {        
			LOGGER.info("IOExceptione" + e.getMessage());
		}	    
	    
	     return map.get( this.hashedName);
	 }
		
}
		

