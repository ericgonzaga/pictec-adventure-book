import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { BookDifficulty, BookMetadata, BookMetadataInput } from "../models/book.models";
import { environment } from "../../environments/environment.prd";

@Injectable({ providedIn: "root" })
export class BookService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/books`;

  listBooks(filters?: { query?: string; category?: string; difficulty?: BookDifficulty | "" }): Observable<BookMetadata[]> {
    let params = new HttpParams();
    if (filters?.query) params = params.set("query", filters.query);
    if (filters?.category) params = params.set("category", filters.category);
    if (filters?.difficulty) params = params.set("difficulty", filters.difficulty);

    return this.http.get<BookMetadata[]>(this.apiUrl, { params });
  }

  getBook(id: string): Observable<BookMetadata> {
    return this.http.get<BookMetadata>(`${this.apiUrl}/${id}`);
  }

  insertBook(file: File, metadata: BookMetadataInput): Observable<BookMetadata> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("metadata", new Blob([JSON.stringify(metadata)], { type: "application/json" }));

    return this.http.post<BookMetadata>(this.apiUrl, formData);
  }

  deleteBook(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
