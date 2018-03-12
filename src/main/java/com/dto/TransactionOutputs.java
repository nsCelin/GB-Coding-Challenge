package com.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TransactionOutputs {
	
	List<SuccessTransaction> sTransaction = new ArrayList<SuccessTransaction>();
	List<SkippedTransaction> fTransaction = new ArrayList<SkippedTransaction>();
	
	private int successCount;
	private int failCount;
	private int totalAccounts;
	private float totalCredits;
	private float totaldebits;
	
	public TransactionOutputs()
	{ }
	
	public void setTransaction(Transaction details, String type)
	{
		this.totalAccounts = this.totalAccounts +1;
		
		if(type == "Success")
		{
			sTransaction.add((SuccessTransaction)details);
			this.successCount = this.successCount +1;
		}		
		else if(type == "Failed")
		{
			fTransaction.add((SkippedTransaction)details);
			this.failCount = this.failCount+1;
		}
			
	}
	
	public int getSuccessCount()
	{
		return successCount;
	}
	
	public int getFailCount()
	{
		return failCount;
	}
	
	
	public int getTotalAccounts()
	{
		return this.totalAccounts;
	}
	
	public void setTotalAccounts(int totalAccounts)
	{
		this.totalAccounts = totalAccounts;
	}
	
	public float getTotalCredits()
	{
		return totalCredits;
	}
	
	public void setTotalCredits(float totalCredits)
	{
		this.totalCredits = this.totalCredits + totalCredits;
	}
	
	public float getTotalDebits()
	{
		return totaldebits;
	}
	
	public void setTotalDebits(float totaldebits)
	{
		this.totaldebits = this.totaldebits + totaldebits;
	}

}
