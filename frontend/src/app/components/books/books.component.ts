import { Component, OnInit, inject, output, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { BOOK_DIFFICULTIES, BookDifficulty, BookMetadata, BookMetadataInput } from "../../models/book.models";
import { BookService } from "../../services/book.service";
import { Router } from "@angular/router";

@Component({
  selector: "app-books",
  standalone: true,
  imports: [FormsModule],
  templateUrl: "./books.component.html",
})
export class BooksComponent implements OnInit {
  private bookService = inject(BookService);
  private router = inject(Router);

  books = signal<BookMetadata[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  filterQuery = "";
  filterCategory = "";
  filterDifficulty: BookDifficulty | "" = "";

  selectedFile: File | null = null;
  newMetadata: BookMetadataInput = {
    difficulty: "EASY",
    category: "",
    description: "",
  };
  difficulties = BOOK_DIFFICULTIES;

  ngOnInit() {
    this.loadBooks();
  }

  loadBooks() {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.bookService
      .listBooks({
        query: this.filterQuery,
        category: this.filterCategory,
        difficulty: this.filterDifficulty,
      })
      .subscribe({
        next: (data) => {
          this.books.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.detail || "Failed to load books.");
          this.loading.set(false);
        },
      });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  onCreateBook() {
    if (!this.selectedFile) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.bookService.insertBook(this.selectedFile, this.newMetadata).subscribe({
      next: () => {
        this.selectedFile = null;
        this.newMetadata = { difficulty: "EASY", category: "", description: "" };
        this.loadBooks();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Failed to insert book.");
        this.loading.set(false);
      },
    });
  }

  onDeleteBook(id: string) {
    this.errorMessage.set(null);
    this.bookService.deleteBook(id).subscribe({
      next: () => this.loadBooks(),
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Failed to delete book.");
      },
    });
  }

  onPlayBook(bookId: string) {
    this.router.navigate(["/game", bookId]);
  }
}
