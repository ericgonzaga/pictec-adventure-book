import { Routes } from "@angular/router";
import { BooksComponent } from "./components/books/books.component";
import { GameComponent } from "./components/game/game.component";

export const routes: Routes = [
  { path: "", redirectTo: "books", pathMatch: "full" },
  { path: "books", component: BooksComponent },
  { path: "game/:bookId", component: GameComponent },
  { path: "**", redirectTo: "books" },
];
