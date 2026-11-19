import { Component, effect, inject, input, output, signal } from "@angular/core";
import { PlayState } from "../../models/game.models";
import { GameService } from "../../services/game.service";
import { Router } from "@angular/router";

@Component({
  selector: "app-game",
  standalone: true,
  templateUrl: "./game.component.html",
})
export class GameComponent {
  private gameService = inject(GameService);
  private router = inject(Router);

  readonly userId = "550e8400-e29b-41d4-a716-446655440000";

  bookId = input.required<string>();

  playState = signal<PlayState | null>(null);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const currentBookId = this.bookId();
      if (currentBookId) {
        this.initGameFlow(currentBookId);
      }
    });
  }

  private initGameFlow(bookId: string) {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.gameService.resumeGame(this.userId, bookId).subscribe({
      next: (existingState) => {
        if (existingState) {
          this.playState.set(existingState);
          this.loading.set(false);
        } else {
          this.startNewGame(bookId);
        }
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Resume game failed.");
        this.loading.set(false);
      },
    });
  }

  private startNewGame(bookId: string) {
    this.gameService.startGame(this.userId, bookId).subscribe({
      next: (newState) => {
        this.playState.set(newState);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Start game failed.");
        this.loading.set(false);
      },
    });
  }

  onChooseOption(optionIndex: number) {
    const current = this.playState();
    if (!current) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.gameService.chooseOption(this.userId, current.gameSession.id, optionIndex, current.gameSession.version).subscribe({
      next: (updatedState) => {
        this.playState.set(updatedState);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Choose option failed.");
        this.loading.set(false);
      },
    });
  }

  onRestart() {
    const current = this.playState();
    if (!current) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.gameService.restartGame(this.userId, current.gameSession.id).subscribe({
      next: (restartedState) => {
        this.playState.set(restartedState);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Restart game failed.");
        this.loading.set(false);
      },
    });
  }

  reloadCurrentSession() {
    const current = this.playState();
    if (!current) {
      this.initGameFlow(this.bookId());
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.gameService.getGame(this.userId, current.gameSession.id).subscribe({
      next: (freshState) => {
        this.playState.set(freshState);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || "Failed to reload session.");
        this.loading.set(false);
      },
    });
  }

  exitGame() {
    this.router.navigate(["/books"]);
  }
}
