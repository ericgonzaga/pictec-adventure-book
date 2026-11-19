import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { PlayState } from "../models/game.models";
import { environment } from "../../environments/environment";

@Injectable({ providedIn: "root" })
export class GameService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/games`;

  startGame(userId: string, bookId: string): Observable<PlayState> {
    const params = new HttpParams().set("userId", userId).set("bookId", bookId);
    return this.http.post<PlayState>(this.apiUrl, null, { params });
  }

  resumeGame(userId: string, bookId: string): Observable<PlayState | null> {
    const params = new HttpParams().set("userId", userId).set("bookId", bookId);
    return this.http.get<PlayState | null>(`${this.apiUrl}/in-progress`, { params });
  }

  getGame(userId: string, gameSessionId: string): Observable<PlayState> {
    const params = new HttpParams().set("userId", userId);
    return this.http.get<PlayState>(`${this.apiUrl}/${gameSessionId}`, { params });
  }

  restartGame(userId: string, gameSessionId: string): Observable<PlayState> {
    const params = new HttpParams().set("userId", userId);
    return this.http.post<PlayState>(`${this.apiUrl}/${gameSessionId}/restart`, null, { params });
  }

  chooseOption(userId: string, gameSessionId: string, optionIndex: number, version: number): Observable<PlayState> {
    const params = new HttpParams().set("userId", userId).set("version", version);
    return this.http.post<PlayState>(`${this.apiUrl}/${gameSessionId}/options/${optionIndex}`, null, { params });
  }
}
