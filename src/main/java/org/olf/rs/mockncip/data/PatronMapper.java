package org.olf.rs.mockncip.data;

import java.util.List;

import org.olf.rs.mockncip.models.Patron;

public interface PatronMapper {
	
	public Patron getPatronByBarcode(String barcode);
	public List<Patron> getAllPatrons();

}
