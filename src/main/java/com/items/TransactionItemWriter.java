package com.items;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import com.dto.TransactionOutputs;

public class TransactionItemWriter implements ItemWriter<TransactionOutputs>{
	
	private String environment;
	
	public TransactionItemWriter(String environment)
	{
		this.environment = environment;
	}

	/*This method is used to write the report to $TRANSACTION_PROCESSING/reports folder.*/
	@Override
	public void write(List<? extends TransactionOutputs> transaction) throws Exception {		
		
		Logger LOGGER  = LoggerFactory.getLogger(TransactionItemWriter.class);		
		LOGGER.info("Inside TransactionWriter");
		
		DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");		
		FileWriter result = new FileWriter(new File(environment, "finance_customer_transactions_report-" + df.format(new Date()) + ".csv"));		

		TransactionOutputs output = transaction.get(0);		
		
		result.append("Total Accounts : " + output.getTotalAccounts());		
		result.append('\n');
		result.flush();
		result.append("Total Credits : " + output.getTotalCredits());
		result.append('\n');
		result.flush();
		result.append("Total Debits : " + output.getTotalDebits());
		result.append('\n');
		result.flush();
		result.append("Total Skipped : " + output.getFailCount());
		result.append('\n');
		result.flush();
		result.close();
		
		/*FileWriter success = new FileWriter("Success-" + df.format(new Date()) + ".csv"); 
	      FileWriter failed = new FileWriter("Failed-" + df.format(new Date()) + ".csv"); 
		  result.append("Total Success : " + output.getSuccessCount());*/
	}

}
