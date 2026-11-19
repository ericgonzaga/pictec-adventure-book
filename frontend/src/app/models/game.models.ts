import { BookSection } from "./book.models";

export type GameStatus = "IN_PROGRESS" | "FINISHED" | "DEAD" | "ABANDONED";

export interface GameSession {
  id: string;
  userId: string;
  bookId: string;
  currentBookSectionId: string;
  health: number;
  status: GameStatus;
  updatedAt: string;
  version: number;
  inProgress: boolean;
}

export interface PlayState {
  gameSession: GameSession;
  section: BookSection;
}
