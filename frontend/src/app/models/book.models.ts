export const BOOK_DIFFICULTIES = ["EASY", "MEDIUM", "HARD"] as const;
export type BookDifficulty = (typeof BOOK_DIFFICULTIES)[number];
export type SectionType = "BEGIN" | "NODE" | "END";
export type ConsequenceType = "LOSE_HEALTH" | "GAIN_HEALTH";

export interface BookMetadataInput {
  description?: string;
  category?: string;
  difficulty: BookDifficulty;
}

export interface BookMetadata {
  id: string;
  title: string;
  author: string;
  description?: string;
  category?: string;
  difficulty: BookDifficulty;
  contentKey: string;
  createdAt: string;
}

export interface Consequence {
  type: ConsequenceType;
  value: number;
  text: string;
}

export interface BookSectionOption {
  description: string;
  gotoId: string;
  consequence?: Consequence;
}

export interface BookSection {
  id: string;
  text: string;
  type: SectionType;
  options: BookSectionOption[];
}
