package com.items;


import java.text.DecimalFormat;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import com.dto.SkippedTransaction;
import com.dto.SuccessTransaction;
import com.dto.Transaction;
import com.dto.TransactionOutputs;

public class TransactionItemProcessor implements ItemProcessor<Transaction, TransactionOutputs> {
	
	@Autowired
	private TransactionOutputs transactionOutputs;	
	

	/*Methods written to pass the success transactions, failure transactions,
	 * number of accounts, total credits and total debits to TransactionOutputs,
	 * which is passed to TransactionWriter.java*/
	@Override
    public TransactionOutputs process(Transaction transaction) throws Exception {
		
		String account_num = transaction.getCustomer_Account();
		String amount = transaction.getTransaction_Amount();
		int totalAccounts = 0;
		
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		
		totalAccounts = totalAccounts+1;
		if(containsOnlyNumbers(account_num))
		{
			SuccessTransaction successTransaction = new SuccessTransaction();
			successTransaction.setCustomer_Account(account_num);
			successTransaction.setTransaction_Amount(amount);
			
			if(amount != null && Float.parseFloat(amount) < 0)
				transactionOutputs.setTotalDebits(Float.parseFloat(amount));
			else if (amount != null && Float.parseFloat(amount) >= 0)
				transactionOutputs.setTotalCredits(Float.parseFloat(amount));
			
			transactionOutputs.setTransaction(successTransaction, "Success");
		}
		else
		{
			SkippedTransaction failedTransaction = new SkippedTransaction();
			failedTransaction.setCustomer_Account(account_num);
			failedTransaction.setTransaction_Amount(amount);
			transactionOutputs.setTransaction(failedTransaction, "Failed");
		}
				
		return transactionOutputs;
        
    }
	
	 private boolean containsOnlyNumbers(String str) {
	        
	        //It can't contain only numbers if it's null or empty...
	        if (str == null || str.length() == 0)
	            return false;
	        
	        for (int i = 0; i < str.length(); i++) {
	 
	            //If we find a non-digit character we return false.
	            if (!Character.isDigit(str.charAt(i)))
	                return false;
	        }
	        
	        return true;
	    }
	
	   
	
}
