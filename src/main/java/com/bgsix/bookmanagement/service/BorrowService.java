package com.bgsix.bookmanagement.service;

import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bgsix.bookmanagement.dto.BorrowedBookDTO;
import com.bgsix.bookmanagement.enums.BorrowStatus;
import com.bgsix.bookmanagement.model.Book;
import com.bgsix.bookmanagement.model.Borrow;
import com.bgsix.bookmanagement.repository.BookRepository;
import com.bgsix.bookmanagement.repository.BorrowRepository;

@Service
public class BorrowService {

	final double fineRatePerDay = 2.0;

	@Autowired
	private BorrowRepository borrowRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private UserService userService;

	public BorrowedBookDTO returnBook(Long borrowId) {

		Borrow borrow = borrowRepository.findByBorrowId(borrowId);

		Book book = bookRepository.findByBookId(borrow.getBookId());

		// Fine calculation
		LocalDate today = LocalDate.now();
		if (today.isAfter(borrow.getDueDate())) {
			long overdueDays = ChronoUnit.DAYS.between(borrow.getDueDate(), today);
			double fine = overdueDays * fineRatePerDay;
			borrow.setFine(fine);
		} else {
			borrow.setStatus(BorrowStatus.RETURNED);
			book.setQuantity(book.getQuantity() + 1);
			bookRepository.save(book);
		}

		BorrowedBookDTO borrowedBookDTO = new BorrowedBookDTO(borrow, book.getTitle(), book.getAuthor());
		borrowedBookDTO.setBorrowId(borrowId);
		borrowRepository.save(borrow);

		return borrowedBookDTO;

	}

	public BorrowedBookDTO getBorrowedBook(Long borrowId) {
		Borrow borrow = borrowRepository.findByBorrowId(borrowId);

		Book book = bookRepository.findByBookId(borrow.getBookId());

		BorrowedBookDTO borrowedBookDTO = new BorrowedBookDTO(borrow, book.getTitle(), book.getAuthor());

		return borrowedBookDTO;
	}


	public List<BorrowedBookDTO> getBorrowedBooks() {
		List<Borrow> borrows = borrowRepository.findAll();

		List<BorrowedBookDTO> borrowedBooks = borrows.stream()
				.sorted((borrow1, borrow2) -> borrow2.getBorrowId().compareTo(borrow1.getBorrowId())).map(borrow -> {
					Book book = bookRepository.findByBookId(borrow.getBookId());

					// Fine calculation
					LocalDate today = LocalDate.now();
					if (today.isAfter(borrow.getDueDate())) {
						long overdueDays = ChronoUnit.DAYS.between(borrow.getDueDate(), today);
						double fine = overdueDays * fineRatePerDay;
						borrow.setFine(fine);
					} else {
						bookRepository.save(book);
					}

					borrowRepository.save(borrow);

					BorrowedBookDTO borrowedBookDTO = new BorrowedBookDTO(borrow, book.getTitle(), book.getAuthor());
					return borrowedBookDTO;
				}).toList();

		return borrowedBooks;
	}

	public List<BorrowedBookDTO> getBorrowedBooksForUser(Long userId) {
		List<Borrow> borrows = borrowRepository.findByUserId(userId);

		List<BorrowedBookDTO> borrowedBooks = borrows.stream()
				.sorted((borrow1, borrow2) -> borrow2.getBorrowId().compareTo(borrow1.getBorrowId())).map(borrow -> {
					Book book = bookRepository.findByBookId(borrow.getBookId());

					// Fine calculation
					LocalDate today = LocalDate.now();
					if (today.isAfter(borrow.getDueDate())) {
						long overdueDays = ChronoUnit.DAYS.between(borrow.getDueDate(), today);
						double fine = overdueDays * fineRatePerDay;
						borrow.setFine(fine);
					} else {
						bookRepository.save(book);
					}

					borrowRepository.save(borrow);

					BorrowedBookDTO borrowedBookDTO = new BorrowedBookDTO(borrow, book.getTitle(), book.getAuthor());
					return borrowedBookDTO;
				}).toList();

		return borrowedBooks;
	}

	public BorrowedBookDTO payFine(Long borrowId) {
		Borrow borrow = borrowRepository.findByBorrowId(borrowId);
		Book book = bookRepository.findByBookId(borrow.getBookId());

		book.setQuantity(book.getQuantity() + 1);
		borrow.setStatus(BorrowStatus.RETURNED);

		bookRepository.save(book);
		borrowRepository.save(borrow);
		return getBorrowedBook(borrowId);
	}
}
