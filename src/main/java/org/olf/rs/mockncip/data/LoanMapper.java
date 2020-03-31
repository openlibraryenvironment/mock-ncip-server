package org.olf.rs.mockncip.data;

import java.util.List;

import org.olf.rs.mockncip.models.Loan;

public interface LoanMapper {
	
	public Loan getLoanById(String id);
	public Loan getOpenLoanByItemBarcode(String itemBarcode);
	public void insertNewLoan(Loan loan);
	public void closeLoan(Loan loan);
	public List<Loan> getAllLoans();
	public List<Loan> getOpenLoans();
	public List<Loan> getOpenLoansByPatron(String patronBarcode);

}
