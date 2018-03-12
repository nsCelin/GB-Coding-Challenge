package com.dto;

import org.springframework.stereotype.Component;

@Component
public class Transaction {

	private String Customer_Account;
	private String Transaction_Amount;
	
	public Transaction()
	{ }
	
	public Transaction(String Customer_Account, String Transaction_Amount)
	{
		this.Customer_Account = Customer_Account;
        this.Transaction_Amount = Transaction_Amount;
	}
	
	public void setCustomer_Account(String Customer_Account) {
        this.Customer_Account = Customer_Account;
    }

    public String getCustomer_Account() {
        return Customer_Account;
    }

    public String getTransaction_Amount() {
        return Transaction_Amount;
    }

    public void setTransaction_Amount(String Transaction_Amount) {
        this.Transaction_Amount = Transaction_Amount;
    }
}
